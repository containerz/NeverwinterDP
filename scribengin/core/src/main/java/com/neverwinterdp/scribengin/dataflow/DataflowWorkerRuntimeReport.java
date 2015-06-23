package com.neverwinterdp.scribengin.dataflow;

import java.util.ArrayList;
import java.util.List;

import com.neverwinterdp.registry.ErrorCode;
import com.neverwinterdp.registry.Node;
import com.neverwinterdp.registry.Registry;
import com.neverwinterdp.registry.RegistryException;
import com.neverwinterdp.scribengin.dataflow.worker.DataflowTaskExecutorDescriptor;
import com.neverwinterdp.scribengin.dataflow.worker.DataflowWorkerStatus;

public class DataflowWorkerRuntimeReport {
  private String                               worker;
  private DataflowWorkerStatus                 status;
  private List<DataflowTaskExecutorDescriptor> executors;

  public DataflowWorkerRuntimeReport() {
  }
  
  public DataflowWorkerRuntimeReport(Registry registry, String workerPath) throws RegistryException {
    Node workerNode = registry.get(workerPath);
    worker = workerNode.getName();
    status = registry.getDataAs(workerPath + "/status", DataflowWorkerStatus.class);
    executors = new ArrayList<>();
    List<String> executorIds = null;
    try {
      executorIds = registry.getChildren(workerPath + "/executors") ;
      for(int i = 0; i < executorIds.size(); i++) {
        String executorId = executorIds.get(i);
        executors.add(registry.getDataAs(workerPath + "/executors/" + executorId, DataflowTaskExecutorDescriptor.class));
      }
    } catch(RegistryException ex) {
      if(ex.getErrorCode() == ErrorCode.NoNode) return;
      throw ex;
    }
  }

  public String getWorker() { return worker; }
  public void setWorker(String worker) { this.worker = worker; }

  public DataflowWorkerStatus getStatus() { return status; }
  public void setStatus(DataflowWorkerStatus status) { this.status = status; }

  public List<DataflowTaskExecutorDescriptor> getExecutors() { return executors; }
  public void setExecutors(List<DataflowTaskExecutorDescriptor> executors) { this.executors = executors; }
}