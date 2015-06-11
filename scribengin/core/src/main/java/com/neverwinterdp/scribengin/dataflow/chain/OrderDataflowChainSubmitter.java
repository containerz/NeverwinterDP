package com.neverwinterdp.scribengin.dataflow.chain;

import java.util.ArrayList;
import java.util.List;

import com.neverwinterdp.registry.Node;
import com.neverwinterdp.scribengin.ScribenginClient;
import com.neverwinterdp.scribengin.dataflow.DataflowDescriptor;
import com.neverwinterdp.scribengin.dataflow.DataflowSubmitter;
import com.neverwinterdp.scribengin.service.ScribenginService;

public class OrderDataflowChainSubmitter extends DataflowChainSubmitter {
  private List<DataflowSubmitter> submitters = new ArrayList<>();
  
  public OrderDataflowChainSubmitter(ScribenginClient client, String dataflowHome, DataflowChainConfig config) {
    super(client, dataflowHome, config);
  }
  
  public void submit(long timeout) throws Exception {
    long stopTime = System.currentTimeMillis() + timeout;
    long remainTime = timeout;
    for(DataflowDescriptor sel : config.getDescriptors()) {
      DataflowSubmitter submitter = doSubmit(client, dataflowHome, sel) ;
      submitters.add(submitter);
      remainTime = stopTime - System.currentTimeMillis();
      if(remainTime < 0) {
        throw new InterruptedException("Cannot finish to submit within " + timeout + "ms") ;
      }
    }
  }
  
  protected DataflowSubmitter doSubmit(ScribenginClient client, String dataflowHome, DataflowDescriptor descriptor) throws Exception {
    try {
      DataflowSubmitter submitter = new DataflowSubmitter(client, dataflowHome, descriptor) ;
      submitter.submit();
      submitter.waitForRunning(45000);
      return submitter;
    } catch(Exception ex ) {
      Node dataflowNode = client.getRegistry().get(ScribenginService.getDataflowPath(descriptor.getId()));
      dataflowNode.dump(System.err);
      throw ex ;
    }
  }
  
  public void waitForTerminated(long timeout) throws Exception {
    long stopTime = System.currentTimeMillis() + timeout;
    long remainTime = timeout;
    for(DataflowSubmitter submitter : submitters) {
      submitter.waitForTerminated(remainTime);
      remainTime = stopTime - System.currentTimeMillis();
    }
  }
}