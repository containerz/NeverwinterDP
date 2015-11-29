package com.neverwinterdp.nstorage.hdfs;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.neverwinterdp.nstorage.NStorageReaderDescriptor;
import com.neverwinterdp.nstorage.NStorageRegistry;
import com.neverwinterdp.nstorage.SegmentDescriptor;
import com.neverwinterdp.nstorage.SegmentReader;

public class HDFSSegmentReader extends SegmentReader {
  private FileSystem        fs;
  private String            storageLocation;
  private String            fullPath;
  private FSDataInputStream dataIs;
  
  public HDFSSegmentReader(NStorageRegistry registry, NStorageReaderDescriptor readerDescriptor, SegmentDescriptor segment, 
                           FileSystem fs, String storageLoc) throws IllegalArgumentException, IOException {
    super(registry, readerDescriptor, segment);
    
    this.fs = fs;
    this.storageLocation = storageLoc;
    fullPath = storageLocation + "/" + segment.getName() + ".dat";
    dataIs  = fs.open(new Path(fullPath)) ;
  }

  @Override
  protected byte[] dataNextRecord() throws IOException {
    int size    = dataIs.readInt();
    byte[] data = new byte[size];
    dataIs.readFully(data);
    return data;
  }
}