package com.neverwinterdp.wa;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.neverwinterdp.netty.http.client.AsyncHttpClient;
import com.neverwinterdp.netty.http.client.DumpResponseHandler;
import com.neverwinterdp.scribengin.LocalScribenginCluster;
import com.neverwinterdp.scribengin.dataflow.Dataflow;
import com.neverwinterdp.scribengin.dataflow.DataflowSubmitter;
import com.neverwinterdp.scribengin.shell.ScribenginShell;
import com.neverwinterdp.vm.VMConfig;
import com.neverwinterdp.vm.client.VMClient;
import com.neverwinterdp.vm.client.VMSubmitter;
import com.neverwinterdp.wa.dataflow.WADataflowBuilder;
import com.neverwinterdp.wa.event.BrowserInfo;
import com.neverwinterdp.wa.event.WebEvent;
import com.neverwinterdp.wa.event.generator.Browsers;
import com.neverwinterdp.wa.event.generator.WebEventGenerator;
import com.neverwinterdp.wa.event.generator.GeneratorServer;
import com.neverwinterdp.wa.gripper.GripperServer;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class WAUnitTest {
  GripperServer          server;
  LocalScribenginCluster localScribenginCluster;
  ScribenginShell        shell;

  @Before
  public void setup() throws Exception {
    String BASE_DIR = "build/working";
    System.setProperty("app.home",   BASE_DIR + "/scribengin");
    System.setProperty("vm.app.dir", BASE_DIR + "/scribengin");
    
    localScribenginCluster = new LocalScribenginCluster(BASE_DIR) ;
    localScribenginCluster.clean(); 
    localScribenginCluster.useLog4jConfig("classpath:scribengin/log4j/vm-log4j.properties");  
    localScribenginCluster.start();
    
    shell = localScribenginCluster.getShell();
    
    server = new GripperServer();
    server.start();
  }
  
  @After
  public void teardown() throws Exception {
    server.shutdown();
    localScribenginCluster.shutdown();
  }
  
  @Test
  public void test() throws Exception {
    int NUM_OF_MESSAGES = 10000;
    GeneratorServer generatorServer = new GeneratorServer();
    generatorServer.setNumOfMessages(NUM_OF_MESSAGES);
    generatorServer.start();
    
    WADataflowBuilder dflBuilder = new WADataflowBuilder() ;
    Dataflow<WebEvent, WebEvent> dfl = dflBuilder.buildDataflow();
    
    try {
      new DataflowSubmitter(shell.getScribenginClient(), dfl).submit().waitForDataflowRunning(60000);
    } catch (Exception ex) {
      shell.execute("registry dump");
      throw ex;
    }
    
    dflBuilder.runMonitor(shell, NUM_OF_MESSAGES);
  }
}