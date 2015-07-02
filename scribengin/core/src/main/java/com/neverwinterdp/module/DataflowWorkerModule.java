package com.neverwinterdp.module;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import com.google.inject.name.Names;
import com.neverwinterdp.vm.VMConfig;



@ModuleConfig(name = "DataflowWorkerModule", autoInstall = false, autostart = false) 
public class DataflowWorkerModule extends ServiceModule {
  final static public String NAME = "DataflowWorkerModule" ;
  
  @Override
  protected void configure(Map<String, String> props) {  
    Names.bindProperties(binder(), props) ;
    try {
      Configuration conf = new Configuration();
      VMConfig.overrideHadoopConfiguration(props, conf);
      FileSystem fs = FileSystem.get(conf);
      bindInstance(FileSystem.class, fs);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}