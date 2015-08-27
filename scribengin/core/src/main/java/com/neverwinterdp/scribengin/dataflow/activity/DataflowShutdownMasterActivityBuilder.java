package com.neverwinterdp.scribengin.dataflow.activity;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.neverwinterdp.registry.Registry;
import com.neverwinterdp.registry.activity.Activity;
import com.neverwinterdp.registry.activity.ActivityBuilder;
import com.neverwinterdp.registry.activity.ActivityCoordinator;
import com.neverwinterdp.registry.activity.ActivityExecutionContext;
import com.neverwinterdp.registry.activity.ActivityStep;
import com.neverwinterdp.registry.activity.ActivityStepBuilder;
import com.neverwinterdp.registry.activity.ActivityStepExecutor;
import com.neverwinterdp.scribengin.activity.ScribenginActivityStepWorkerService;
import com.neverwinterdp.scribengin.dataflow.DataflowDescriptor;
import com.neverwinterdp.scribengin.dataflow.DataflowLifecycleStatus;
import com.neverwinterdp.scribengin.dataflow.registry.DataflowRegistry;
import com.neverwinterdp.scribengin.service.ScribenginService;

public class DataflowShutdownMasterActivityBuilder extends ActivityBuilder {
  public Activity build(String dataflowPath) {
    Activity activity = new Activity();
    activity.setDescription("Shutdown Dataflow Master Activity");
    activity.setType("dataflow-shutdown-master");
    activity.attribute("dataflow.path", dataflowPath);
    activity.withCoordinator(ShutdownDataflowMasterActivityCoordinator.class);
    activity.withActivityStepBuilder(ShutdownDataflowMasterActivityStepBuilder.class) ;
    return activity;
  }
  
  @Singleton
  static public class ShutdownDataflowMasterActivityStepBuilder implements ActivityStepBuilder {
    @Override
    public List<ActivityStep> build(Activity activity, Injector container) throws Exception {
      List<ActivityStep> steps = new ArrayList<>() ;
      steps.add(new ActivityStep().
          withType("dataflow-shutdown-master").
          withExecutor(ShutdownDataflowMasterStepExecutor.class));
      return steps;
    }
  }
  
  @Singleton
  static public class ShutdownDataflowMasterActivityCoordinator extends ActivityCoordinator {
    @Inject
    ScribenginActivityStepWorkerService activityStepWorkerService;
   
    @Override
    protected <T> void execute(ActivityExecutionContext context, Activity activity, ActivityStep step) throws Exception {
      activityStepWorkerService.exectute(context, activity, step);
    }
  }
  
  @Singleton
  static public class ShutdownDataflowMasterStepExecutor implements ActivityStepExecutor {
    @Inject
    private Registry registry ;
    
    @Inject
    private ScribenginService scribenginService;
    
    @Override
    public void execute(ActivityExecutionContext context, Activity activity, ActivityStep step) throws Exception {
      System.err.println("ShutdownDataflowMasterStepExecutor: execute().............");
      String dataflowPath = activity.attribute("dataflow.path");
      DataflowRegistry dataflowRegistry = new DataflowRegistry(registry, dataflowPath) ;
      while(dataflowRegistry.getMasterRegistry().countDataflowMasters() > 0) {
        Thread.sleep(500);
        System.err.println("Wait for all the dataflow master shutdown");
      }
      dataflowRegistry.setStatus(DataflowLifecycleStatus.TERMINATED);
      //Node statusNode = registry.get(dataflowPath);
      //Node dataflowNode = statusNode.getParentNode() ;
      DataflowDescriptor dataflowDescriptor = dataflowRegistry.getDataflowDescriptor();
      scribenginService.moveToHistory(dataflowDescriptor) ;
    }
  }
}