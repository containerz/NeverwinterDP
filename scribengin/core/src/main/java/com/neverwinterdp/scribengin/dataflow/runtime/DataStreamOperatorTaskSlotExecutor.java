package com.neverwinterdp.scribengin.dataflow.runtime;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.registry.RegistryException;
import com.neverwinterdp.registry.task.TaskExecutorDescriptor;
import com.neverwinterdp.registry.task.dedicated.DedicatedTaskContext;
import com.neverwinterdp.registry.task.dedicated.TaskExecutorEvent;
import com.neverwinterdp.registry.task.dedicated.TaskSlotExecutor;
import com.neverwinterdp.scribengin.dataflow.DataStreamOperator;
import com.neverwinterdp.scribengin.dataflow.DataStreamOperatorDescriptor;
import com.neverwinterdp.scribengin.dataflow.DataStreamOperatorReport;
import com.neverwinterdp.scribengin.dataflow.DataStreamType;
import com.neverwinterdp.scribengin.dataflow.registry.DataflowRegistry;
import com.neverwinterdp.scribengin.dataflow.runtime.worker.WorkerService;

public class DataStreamOperatorTaskSlotExecutor extends TaskSlotExecutor<DataStreamOperatorDescriptor>{
  private WorkerService                                      workerService;
  private DedicatedTaskContext<DataStreamOperatorDescriptor> taskContext;
  private DataStreamOperatorDescriptor                       dsOperatorDescriptor;
  private DataStreamOperator                                 operator;
  private DataStreamOperatorRuntimeContext                   context;

  private long startTime         = 0;
  private long lastFlushTime     = System.currentTimeMillis();
  private long lastNoMessageTime = lastFlushTime;

  public DataStreamOperatorTaskSlotExecutor(WorkerService service, DedicatedTaskContext<DataStreamOperatorDescriptor> taskContext) throws Exception {
    super(taskContext);
    this.workerService = service;
    this.taskContext   = taskContext;
    this.dsOperatorDescriptor = taskContext.getTaskDescriptor(false);
    
    startTime = System.currentTimeMillis();
    DataflowRegistry dRegistry = workerService.getDataflowRegistry();
    DataStreamOperatorReport report = dRegistry.getTaskRegistry().getTaskReport(dsOperatorDescriptor);
    report.incrAssignedCount();
    dRegistry.getTaskRegistry().save(dsOperatorDescriptor, report);
    TaskExecutorDescriptor taskExecutorDescriptor = taskContext.getTaskExecutorDescriptor();
    context = new DataStreamOperatorRuntimeContext(workerService, taskExecutorDescriptor, dsOperatorDescriptor, report);
    dRegistry.getTaskRegistry().save(dsOperatorDescriptor, report);
    
    Class<DataStreamOperator> opType = (Class<DataStreamOperator>) Class.forName(dsOperatorDescriptor.getOperator());
    operator = opType.newInstance();
    operator.onInit(context);
  }
  
  public DedicatedTaskContext<DataStreamOperatorDescriptor> getTaskContext() { return taskContext; }
  
  public DataStreamOperatorDescriptor getDataStreamOperatorDescriptor() { return dsOperatorDescriptor ; }
  
  public boolean isComplete() { return context.isComplete() ; }
  
  @Override
  public void onShutdown() throws Exception {
    context.commit();
    context.close();
  }
  
  @Override
  public void onEvent(TaskExecutorEvent event) throws Exception {
    if("StopInput".equals(event.getName())) {
      InputDataStreamContext inputContext = context.getInputDataStreamContext();
      if(inputContext.getDataStreamType() == DataStreamType.Input) {
        inputContext.stopInput();
        System.err.println("DataStreamOperatorTaskSlotExecutor: event StopInput");
      }
    }
  }
  
  @Override
  public long executeSlot() throws InterruptedException, Exception {
    if(context.getInputDataStreamContext().isStopInput()) return 0l;
    startTime = System.currentTimeMillis();
    DataStreamOperatorReport report = context.getReport();
    int recCount = 0;
    try {
      while(!isInterrupted()) {
        Message message = context.nextMessage(500);
        if(message == null) break ;

        recCount++;
        report.incrProcessCount();
        operator.process(context, message);
      } //end while
      if(isSimulateKill()) {
        System.err.println("DataStreamOperatorTaskSlotExecutor: detect simulate kill for " + dsOperatorDescriptor.getOperatorName());
        return 0;
      }
      
      long currentTime = System.currentTimeMillis();
      if(recCount == 0) {
        if(lastNoMessageTime < 0) lastNoMessageTime = currentTime;
        report.setAssignedWithNoMessageProcess(report.getAssignedWithNoMessageProcess() + 1);
        report.setLastAssignedWithNoMessageProcess(report.getLastAssignedWithNoMessageProcess() + 1);
      } else {
        report.setLastAssignedWithNoMessageProcess(0);
        lastNoMessageTime = -1;
      }
      long runtime = currentTime - startTime;
      report.addAccRuntime(currentTime - startTime);
      if(recCount > 0) {
        operator.onPreCommit(context);
        context.commit();
        operator.onPostCommit(context);
      }
      return runtime;
    } catch(InterruptedException ex) {
      System.err.println("DataStreamOperatorTaskSlotExecutor: Catched an interrupt exception");
      throw ex ;
    } catch(RegistryException error) {
      throw error;
    } catch(Exception error) {
      System.err.println("DataStreamOperatorTaskSlotExecutor: Catched a task exception and rollback");
      error.printStackTrace();
      rollback(error);
      return System.currentTimeMillis() - startTime;
    } catch(Throwable t) {
      System.err.println("DataStreamOperatorTaskSlotExecutor: Catch an unknown error Throwable");
      t.printStackTrace();
      throw t;
    }
  }
  
  void rollback(Exception error) throws Exception {
    context.rollback();
    DataStreamOperatorReport report = context.getReport();
    report.setAssignedHasErrorCount(report.getAssignedHasErrorCount() + 1);
    workerService.getLogger().error("DataflowTask Error", error);
  }
}