#!/bin/bash

if [ "x$JAVA_HOME" == "x" ] ; then 
  echo "WARNING JAVA_HOME is not set"
fi

export HADOOP_USER_NAME="neverwinterdp"

SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
APP_DIR=`cd $SCRIPT_DIR/..; pwd; cd $SCRIPT_DIR`
NEVERWINTERDP_BUILD_DIR=`cd $APP_DIR/../..; pwd; cd $SCRIPT_DIR`

JAVACMD=$JAVA_HOME/bin/java
SHELL=$NEVERWINTERDP_BUILD_DIR/scribengin/bin/shell.sh

DFS_APP_HOME="/applications/tracking-sample"
TRACKING_REPORT_PATH="/applications/tracking-sample/reports"

$SHELL plugin com.neverwinterdp.scribengin.dataflow.tracking.TrackingWithSimulationLauncher \
  --dfs-app-home $DFS_APP_HOME --local-app-home $APP_DIR --tracking-report-path $TRACKING_REPORT_PATH --dataflow-id tracking $@ 
