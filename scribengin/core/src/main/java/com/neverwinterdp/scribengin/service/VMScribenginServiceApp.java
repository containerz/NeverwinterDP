package com.neverwinterdp.scribengin.service;

import java.util.Map;

import org.slf4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.mycila.guice.ext.closeable.CloseableInjector;
import com.mycila.guice.ext.closeable.CloseableModule;
import com.mycila.guice.ext.jsr250.Jsr250Module;
import com.neverwinterdp.es.log.OSMonitorLoggerService;
import com.neverwinterdp.module.AppServiceModule;
import com.neverwinterdp.module.MycilaJmxModuleExt;
import com.neverwinterdp.os.RuntimeEnv;
import com.neverwinterdp.registry.RefNode;
import com.neverwinterdp.registry.Registry;
import com.neverwinterdp.registry.RegistryConfig;
import com.neverwinterdp.registry.election.LeaderElection;
import com.neverwinterdp.registry.election.LeaderElectionListener;
import com.neverwinterdp.scribengin.event.ScribenginShutdownEventListener;
import com.neverwinterdp.util.log.LoggerFactory;
import com.neverwinterdp.vm.VMApp;
import com.neverwinterdp.vm.VMConfig;
import com.neverwinterdp.vm.client.VMClient;

public class VMScribenginServiceApp extends VMApp {
  private Logger logger ;
  private LeaderElection election ;
  private Injector  appContainer ;
  private ScribenginService scribenginService;
  
  public ScribenginService getScribenginService() { return this.scribenginService ; }
  
  @Override
  public void run() throws Exception {
    logger = getVM().getLoggerFactory().getLogger(VMScribenginServiceApp.class);
    Registry registry = getVM().getVMRegistry().getRegistry();
    getVM().getVMRegistry().getRegistry().createIfNotExist(ScribenginService.LEADER_PATH) ;
    RefNode masterVMRef = new RefNode(getVM().getDescriptor().getRegistryPath()) ;
    election = new LeaderElection(getVM().getVMRegistry().getRegistry(), ScribenginService.LEADER_PATH, masterVMRef) ;
    election.setListener(new MasterLeaderElectionListener());
    election.start();
    
    ScribenginShutdownEventListener shutdownListener = new ScribenginShutdownEventListener(registry) {
      @Override
      public void onShutdownEvent() { terminate(TerminateEvent.Shutdown); }
    };

    try {
      waitForTerminate();
    } catch(InterruptedException ex) {
    } finally {
      if(appContainer != null) {
        appContainer.getInstance(CloseableInjector.class).close();
      }
      if(election != null && election.getLeaderId() != null) {
        election.stop();
      }
    }
  }
 
  class MasterLeaderElectionListener implements LeaderElectionListener {
    @Override
    public void onElected() {
      try {
        final Registry registry = getVM().getVMRegistry().getRegistry();
        AppServiceModule module = new AppServiceModule(getVM().getDescriptor().getVmConfig().getProperties()) {
          @Override
          protected void configure(Map<String, String> properties) {
            bindInstance(RegistryConfig.class, registry.getRegistryConfig());
            try {
              bindInstance(RuntimeEnv.class, getVM().getRuntimeEnv());
              bindInstance(LoggerFactory.class, getVM().getLoggerFactory());
              bindType(Registry.class, registry.getClass().getName());
              bindInstance(VMConfig.class, getVM().getDescriptor().getVmConfig());
              bindInstance(VMClient.class, new VMClient(registry));
            } catch (ClassNotFoundException e) {
              logger.error("Initialize AppModule Error", e);
            }
          };
        };
        Module[] modules = {
          new CloseableModule(),new Jsr250Module(), 
          new MycilaJmxModuleExt(getVM().getDescriptor().getVmConfig().getName()), 
          module
        };
        appContainer = Guice.createInjector(Stage.PRODUCTION, modules);
        
        //TODO: fix to use module
        //appContainer.getInstance(OSMonitorLoggerService.class);
        
        scribenginService = appContainer.getInstance(ScribenginService.class);
        RefNode refNode = new RefNode() ;
        refNode.setPath(getVM().getDescriptor().getRegistryPath());
        registry.setData(ScribenginService.LEADER_PATH, refNode);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
}