package com.neverwinterdp.storage.hdfs.source;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;

import com.neverwinterdp.storage.StorageConfig;
import com.neverwinterdp.storage.hdfs.HDFSStorageRegistry;
import com.neverwinterdp.storage.source.Source;
import com.neverwinterdp.storage.source.SourcePartition;

public class HDFSSource implements Source {
  private HDFSStorageRegistry storageRegistry ;
  private FileSystem          fs;
  private HDFSSourcePartition partition;
  
  public HDFSSource(HDFSStorageRegistry storageRegistry, FileSystem fs) {
    this.storageRegistry = storageRegistry;
    this.fs = fs;
    partition = new HDFSSourcePartition(storageRegistry, fs);
  }
  
  @Override
  public StorageConfig getStorageConfig() { return storageRegistry.getStorageConfig(); }

  @Override
  public SourcePartition getLatestSourcePartition() throws Exception {
    return partition;
  }

  @Override
  public List<? extends SourcePartition> getSourcePartitions() throws Exception {
    List<SourcePartition> holder = new ArrayList<>();
    holder.add(partition);
    return holder;
  }
}
