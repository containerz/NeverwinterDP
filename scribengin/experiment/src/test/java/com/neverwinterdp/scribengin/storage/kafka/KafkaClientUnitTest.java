package com.neverwinterdp.scribengin.storage.kafka;


import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.neverwinterdp.kafka.tool.KafkaTool;
import com.neverwinterdp.kafka.tool.ZKTool;
import com.neverwinterdp.scribengin.storage.kafka.sink.KafkaSink;
import com.neverwinterdp.scribengin.storage.sink.SinkPartitionStream;
import com.neverwinterdp.scribengin.storage.sink.SinkPartitionStreamWriter;
import com.neverwinterdp.kafka.tool.server.KafkaCluster;

public class KafkaClientUnitTest {
  static {
    System.setProperty("log4j.configuration", "file:src/test/resources/test-log4j.properties");
  }

  private KafkaCluster cluster;

  @Before
  public void setUp() throws Exception {
    cluster = new KafkaCluster("./build/cluster", 1, 1);
    cluster.start();
    Thread.sleep(2000);
  }
  
  @After
  public void tearDown() throws Exception {
    cluster.shutdown();
  }

  @Test
  public void testKafkaSink() throws Exception {
    KafkaStorage storage = new KafkaStorage("writer", cluster.getZKConnect(), "hello");
    KafkaSink sink = (KafkaSink) storage.getSink();
    SinkPartitionStream stream = sink.newStream();
    SinkPartitionStreamWriter writer = stream.getWriter();
    for(int i = 0; i < 10; i++) {
    }
    writer.close();
    
    KafkaTool client = new KafkaTool("test", "127.0.0.1:2181");
    client.connect();
    ZooKeeper zkClient = client.getZookeeper();
    ZKTool.dump(zkClient, "/brokers");
    client.close();
  }
}
