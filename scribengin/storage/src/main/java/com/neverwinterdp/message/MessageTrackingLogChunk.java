package com.neverwinterdp.message;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.zip.DataFormatException;

import com.neverwinterdp.util.io.IOUtil;

public class MessageTrackingLogChunk {
  static DecimalFormat ID_FORMAT = new DecimalFormat("00000000");
  
  private String name;
  private int     chunkId;
  private int     chunkSize;
  private int     trackingProgress;
  private int     trackingNoLostTo;
  private int     trackingLostCount;
  private int     trackingDuplicatedCount;
  private int     trackingCount;
  private boolean complete = false;

  transient private BitSet bitSet;

  public MessageTrackingLogChunk() {}
  
  public MessageTrackingLogChunk(String name, int chunkId, int chunkSize) {
    this.name      = name ;
    this.chunkId   =  chunkId;
    this.chunkSize = chunkSize;
    this.bitSet    = new BitSet(chunkSize);
  }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public int getChunkId() { return chunkId; }
  public void setChunkId(int chunkId) { this.chunkId = chunkId; }

  public int getChunkSize() { return chunkSize; }
  public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize;}

  public int getTrackingProgress() { return trackingProgress; }
  public void setTrackingProgress(int trackingProgress) {
    this.trackingProgress = trackingProgress;
  }

  public int getTrackingNoLostTo() { return trackingNoLostTo; }
  public void setTrackingNoLostTo(int trackingNoLostTo) {
    this.trackingNoLostTo = trackingNoLostTo;
  }

  public int getTrackingLostCount() { return trackingLostCount; }
  public void setTrackingLostCount(int trackingLostCount) {
    this.trackingLostCount = trackingLostCount;
  }

  public int getTrackingDuplicatedCount() { return trackingDuplicatedCount; }
  public void setTrackingDuplicatedCount(int trackingDuplicatedCount) {
    this.trackingDuplicatedCount = trackingDuplicatedCount;
  }

  public int getTrackingCount() { return trackingCount; }
  public void setTrackingCount(int trackingCount) {
    this.trackingCount = trackingCount;
  }

  public boolean isComplete() { return complete;}
  public void setComplete(boolean complete) {
    this.complete = complete;
  }

  public byte[] getBitSetData() throws IOException {
    return IOUtil.compress(bitSet.toByteArray()) ;
  }
  
  public void setBitSetData(byte[] data) throws IOException, DataFormatException {
    byte[] bitData = IOUtil.decompress(data);
    bitSet = BitSet.valueOf(bitData);
  }
  
  synchronized public int log(MessageTracking mTracking, MessageTrackingLog log) {
    if(mTracking.getChunkId() != chunkId) {
      throw new RuntimeException("The chunk id is not matched, chunkId = " + chunkId + ", message chunk id = " + mTracking.getChunkId());
    }
    int idx = mTracking.getTrackingId();
    if(idx > chunkSize) {
      throw new RuntimeException("The tracking id is greater than the chunk size" + chunkSize);
    }
    
    
    if(idx > trackingProgress) trackingProgress = idx;
    if(bitSet.get(idx)) trackingDuplicatedCount++ ;
    bitSet.set(idx, true);
    trackingCount++ ;
    return trackingCount;
  }
  
  synchronized public void update() {
    int lostCount = 0;
    int noLostTo = -1;
    for(int i = 0; i < trackingProgress; i++) {
      if(!bitSet.get(i)) {
        if(noLostTo < 0) noLostTo = i;
        lostCount++ ;
      }
    }
    if(noLostTo < 0) trackingNoLostTo = trackingProgress;
    trackingLostCount = lostCount;
  }
  
  public String toChunkIdName() { return toChunkIdName(chunkId); }
  
  final static public String toChunkIdName(int chunkId) {
    return "chunk-" + ID_FORMAT.format(chunkId);
  }
}