/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.cluster.callback;

import org.apache.iotdb.cluster.rpc.request.BasicRequest;
import org.apache.iotdb.cluster.rpc.response.BasicResponse;

/**
 * Process single task.
 */
public class SingleTask extends Task {


  public SingleTask(boolean isSyncTask, BasicRequest request) {
    super(isSyncTask, 1, TaskState.INITIAL);
    setRequest(request);
  }

  /**
   * Process response. If it's necessary to redirect leader, redo the task.
   */
  @Override
  public void run(BasicResponse response) {
    if (response.isRedirected()) {
      setTaskState(TaskState.REDIRECT);
    } else if(getTaskState() != TaskState.EXCEPTION){
      setResponse(response);
      setTaskState(TaskState.FINISH);
    }
    getTaskNum().countDown();
  }
}
