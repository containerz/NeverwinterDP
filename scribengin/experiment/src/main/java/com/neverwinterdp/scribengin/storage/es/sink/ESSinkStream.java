package com.neverwinterdp.scribengin.storage.es.sink;

import com.neverwinterdp.scribengin.storage.PartitionDescriptor;
import com.neverwinterdp.scribengin.storage.sink.SinkPartitionStream;
import com.neverwinterdp.scribengin.storage.sink.SinkPartitionStreamWriter;

public class ESSinkStream implements SinkPartitionStream {
  private PartitionDescriptor descriptor;
  
  public ESSinkStream(PartitionDescriptor descriptor) {
    this.descriptor = descriptor;
  }
  
  @Override
  public PartitionDescriptor getDescriptor() { return descriptor; }

  @Override
  public void delete() throws Exception {
  }

  @Override
  public SinkPartitionStreamWriter getWriter() throws Exception { 
    return new ESStreamWriter(descriptor); 
  }

  @Override
  public void optimize() throws Exception {
  }
}
