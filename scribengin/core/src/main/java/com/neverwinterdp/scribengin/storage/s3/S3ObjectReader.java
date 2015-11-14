package com.neverwinterdp.scribengin.storage.s3;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import com.amazonaws.services.s3.model.S3Object;

public class S3ObjectReader implements Closeable {
  private ObjectInputStream objIs;
  private byte[]            current = null ;
  
  public S3ObjectReader(S3Object s3Object) throws IOException {
    InputStream is = s3Object.getObjectContent();
    objIs = new ObjectInputStream(new BufferedInputStream(is, 1024 * 1024));
  }

  public byte[] next() { return current; }
  
  
  public boolean hasNext() throws IOException {
    if(objIs.available() > 0) {
      current = new byte[objIs.readInt()];
      objIs.readFully(current);
      return true;
    }
    return false;
  }

  @Override
  public void close() throws IOException {
    objIs.close();
  }
  
  static int getInt(byte[] b, int off) {
    return ((b[off + 3] & 0xFF)      ) +
           ((b[off + 2] & 0xFF) <<  8) +
           ((b[off + 1] & 0xFF) << 16) +
           ((b[off    ]       ) << 24);
  }
}
