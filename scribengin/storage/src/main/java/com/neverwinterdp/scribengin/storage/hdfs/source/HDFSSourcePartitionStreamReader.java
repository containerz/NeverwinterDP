package com.neverwinterdp.scribengin.storage.hdfs.source;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;

import com.neverwinterdp.scribengin.storage.Record;
import com.neverwinterdp.scribengin.storage.PartitionStreamConfig;
import com.neverwinterdp.scribengin.storage.hdfs.SegmentStorageReader;
import com.neverwinterdp.scribengin.storage.source.SourcePartitionStreamReader;

/**
 * @author Tuan Nguyen
 */
public class HDFSSourcePartitionStreamReader extends SegmentStorageReader<Record> implements SourcePartitionStreamReader {
  public HDFSSourcePartitionStreamReader(String name, FileSystem fs, PartitionStreamConfig descriptor) throws FileNotFoundException, IllegalArgumentException, IOException {
    super(name, fs, descriptor.getLocation(), Record.class);
  }
}