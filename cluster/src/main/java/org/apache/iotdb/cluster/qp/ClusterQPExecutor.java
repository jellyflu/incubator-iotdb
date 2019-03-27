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
package org.apache.iotdb.cluster.qp;

import com.alipay.sofa.jraft.rpc.impl.cli.BoltCliClientService;
import org.apache.iotdb.cluster.config.ClusterConfig;
import org.apache.iotdb.cluster.config.ClusterDescriptor;
import org.apache.iotdb.cluster.exception.RaftConnectionException;
import org.apache.iotdb.cluster.utils.RaftUtils;
import org.apache.iotdb.cluster.utils.hash.PhysicalNode;
import org.apache.iotdb.cluster.utils.hash.Router;
import org.apache.iotdb.db.exception.PathErrorException;
import org.apache.iotdb.db.metadata.MManager;

public abstract class ClusterQPExecutor {

  private static final ClusterConfig CLUSTER_CONFIG = ClusterDescriptor.getInstance().getConfig();
  private Router router = Router.getInstance();
  private PhysicalNode localNode = new PhysicalNode(CLUSTER_CONFIG.getIp(), CLUSTER_CONFIG.getPort());

  /**
   * Get Storage Group Name by device name
   */
  public String getStroageGroupByDevice(String device) throws PathErrorException {
    String storageGroup;
    try {
      storageGroup = MManager.getInstance().getFileNameByPath(device);
    } catch (PathErrorException e) {
      throw new PathErrorException(String.format("File level of %s doesn't exist.", device));
    }
    return storageGroup;
  }

  /**
   * Get raft group id by storage group name
   */
  public String getGroupIdBySG(String storageGroup) {
    return router.getGroupID(router.routeGroup(storageGroup));
  }

  /**
   * Verify if the command can execute in local.
   * 1. If this node belongs to the storage group
   * 2. If this node is leader.
   */
  public boolean canHandle(String storageGroup, BoltCliClientService cliClientService) throws RaftConnectionException {
    if(router.containPhysicalNode(storageGroup, localNode)){
      String groupId = getGroupIdBySG(storageGroup);
      if(RaftUtils.convertPeerId(RaftUtils.getLeader(groupId, cliClientService)).equals(localNode)){
        return true;
      }
    }
    return false;
  }
}
