package com.neverwinterdp.kafka.consumer;


import java.util.List;

import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.neverwinterdp.kafka.producer.DefaultKafkaWriter;
import com.neverwinterdp.kafka.tool.KafkaTool;
import com.neverwinterdp.kafka.tool.server.KafkaCluster;
import com.neverwinterdp.util.io.FileUtil;

public class KafkaPartitionReaderUnitTest {
  static {
    System.setProperty("log4j.configuration", "file:src/test/resources/log4j.properties");
  }

  private KafkaCluster cluster;

  @Before
  public void setUp() throws Exception {
    FileUtil.removeIfExist("./build/cluster", false);
    
    cluster = new KafkaCluster("./build/cluster", 1, 1);
    cluster.setNumOfPartition(5);
    cluster.start();
    Thread.sleep(2000);
  }
  
  @After
  public void tearDown() throws Exception {
    cluster.shutdown();
  }

  @Test
  public void testReader() throws Exception {
    String NAME = "test";
    DefaultKafkaWriter writer = new DefaultKafkaWriter(NAME, cluster.getKafkaConnect());
    for(int i = 0; i < 10; i++) {
      String hello = "Hello " + i;
      writer.send("hello", 0, "key-" + i, hello, 5000);
    }
    writer.close();
    
    readFromPartition(NAME, 0, 1, 1000/*maxWait*/);
    readFromPartition(NAME, 0, 2, 1000/*maxWait*/);
    readFromPartition(NAME, 0, 3, 1000/*maxWait*/);
    
    readFromPartition(NAME, 0, 10, 1000/*maxWait*/);
    
    readFromPartition(NAME, 0, 10, 5000/*maxWait*/);
  }
  
  private void readFromPartition(String consumerName, int partition, int maxRead, long maxWait) throws Exception {
    System.out.println("Read partition = " + partition + ", maxRead = " + maxRead + ", maxWait = " + maxWait);
    KafkaTool kafkaTool = new KafkaTool(consumerName, cluster.getZKConnect());
    kafkaTool.connect();
    TopicMetadata topicMetadata = kafkaTool.findTopicMetadata("hello");
    PartitionMetadata partitionMetadata = findPartition(topicMetadata.partitionsMetadata(), partition);
    KafkaPartitionReader partitionReader = 
        new KafkaPartitionReader(consumerName, cluster.getZKConnect(), "hello", partitionMetadata);
    List<byte[]> messages = partitionReader.fetch(10000, maxRead, maxWait);
    for(int i = 0; i < messages.size(); i++) {
      byte[] message = messages.get(i) ;
      System.out.println((i + 1) + ". " + new String(message));
    }
    partitionReader.commit();
    partitionReader.close();
    kafkaTool.close();
  }
  
  private PartitionMetadata findPartition(List<PartitionMetadata> list, int partition) {
    for(PartitionMetadata sel : list) {
      if(sel.partitionId() == partition) return sel;
    }
    return null;
  }  
}
