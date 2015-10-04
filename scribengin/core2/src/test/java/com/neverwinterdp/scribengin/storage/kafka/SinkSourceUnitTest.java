package com.neverwinterdp.scribengin.storage.kafka;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.neverwinterdp.scribengin.storage.Record;
import com.neverwinterdp.scribengin.storage.kafka.sink.KafkaSink;
import com.neverwinterdp.scribengin.storage.kafka.source.KafkaSource;
import com.neverwinterdp.scribengin.storage.sink.SinkPartitionStream;
import com.neverwinterdp.scribengin.storage.sink.SinkPartitionStreamWriter;
import com.neverwinterdp.scribengin.storage.source.SourcePartitionStream;
import com.neverwinterdp.scribengin.storage.source.SourcePartitionStreamReader;
import com.neverwinterdp.kafka.tool.server.KafkaCluster;

public class SinkSourceUnitTest {
  static {
    System.setProperty("log4j.configuration", "file:src/test/resources/test-log4j.properties");
  }

  private KafkaCluster cluster;

  @Before
  public void setUp() throws Exception {
    cluster = new KafkaCluster("./build/cluster", 1, 1);
    cluster.setNumOfPartition(5);
    cluster.start();
    Thread.sleep(3000);
  }
  
  @After
  public void tearDown() throws Exception {
    cluster.shutdown();
  }

  @Test
  public void testKafkaSource() throws Exception {
    String zkConnect = cluster.getZKConnect();
    System.out.println("zkConnect = " + zkConnect);
    String TOPIC = "hello.topic" ;
    KafkaStorage storage = new KafkaStorage("hello", cluster.getZKConnect(), TOPIC);
    KafkaSink sink = (KafkaSink) storage.getSink();
    
    SinkPartitionStream stream = sink.newStream();
    SinkPartitionStreamWriter writer = stream.getWriter();
    for(int i = 0; i < 10; i++) {
      String hello = "Hello " + i ;
      Record dataflowMessage = new Record("key-" + i, hello.getBytes());
      writer.append(dataflowMessage);
    }
    writer.close();
    
    KafkaSource source = new KafkaSource("hello", cluster.getZKConnect(), TOPIC);
    SourcePartitionStream[] streams = source.getStreams();
    Assert.assertEquals(5, streams.length);
    for(int i = 0; i < streams.length; i++) {
      System.out.println("Stream id: " + streams[i].getDescriptor().getPartitionId());
      SourcePartitionStreamReader reader = streams[i].getReader("kafka");
      Record dataflowMessage = null;
      while((dataflowMessage = reader.next(1000)) != null) {
        System.out.println("Record: " + new String(dataflowMessage.getData()));
      }
    }
  }
}