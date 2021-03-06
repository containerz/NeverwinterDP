package com.neverwinterdp.storage;

import com.neverwinterdp.storage.sink.Sink;
import com.neverwinterdp.storage.source.Source;

abstract public class Storage {
  private StorageConfig storageConfig;
  
  public Storage(StorageConfig storageDescriptor) {
    this.storageConfig = storageDescriptor ;
  }
  
  public StorageConfig getStorageConfig() { return storageConfig; }
  
  public void setStorageConfig(StorageConfig config) { storageConfig = config; }
  
  abstract public void refresh() throws Exception ; 
  
  abstract public boolean exists() throws Exception ;
  
  abstract public void drop() throws Exception ;
  abstract public void create() throws Exception;
  
  abstract public Sink getSink() throws Exception ;
  abstract public Source getSource() throws Exception ;
}
