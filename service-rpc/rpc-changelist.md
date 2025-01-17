<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->

# 0.8.0 (version-0) -> version-1

Last Updated on October 27th, 2019 by Lei Rui.



## 1. Delete Old

| Latest Changes                     | Related Committers |
| ---------------------------------- | ------------------ |
| Delete struct TSSetStorageGroupReq | Jialin Qiao        |
| Remove struct TSDataValue          | Lei Rui            |
| Remove struct TSRowRecord          | Lei Rui            |



## 2. Add New

| Latest Changes                                               | Related Committers                 |
| ------------------------------------------------------------ | ---------------------------------- |
| Add struct TSBatchInsertionReq                               | qiaojialin                         |
| Add method TSExecuteBatchStatementResp insertBatch(1:TSBatchInsertionReq req) | qiaojialin                         |
| Add Struct TSStatusType                                      | Zesong Sun                         |
| Add TSCreateTimeseriesReq                                    | Zesong Sun                         |
| Add method TSStatus setStorageGroup(1:string storageGroup)   | Zesong Sun, Jialin Qiao            |
| Add method TSStatus createTimeseries(1:TSCreateTimeseriesReq req) | Zesong Sun                         |
| Add struct TSInsertReq                                       | qiaojialin                         |
| Add method TSRPCResp insertRow(1:TSInsertReq req)            | qiaojialin                         |
| Add struct TSDeleteDataReq                                   | Jack Tsai, qiaojialin              |
| Add method TSStatus deleteData(1:TSDeleteDataReq req)        | Jack Tsai, Jialin Qiao, qiaojialin |
| Add method TSStatus deleteTimeseries(1:list\<string> path)   | qiaojialin                         |
| Add method TSStatus deleteStorageGroups(1:list\<string> storageGroup) | Yi Tao                             |
| Add Struct TSExecuteInsertRowInBatchResp                     | Kaifeng Xue |
| Add method insertRowInBatch(1:TSInsertInBatchReq req);       | Kaifeng Xue |


## 3. Update

| Latest Changes                                               | Related Committers     |
| ------------------------------------------------------------ | ---------------------- |
| Add required string timestampPrecision in ServerProperties   | 1160300922             |
| Add optional list\<string\> dataTypeList in TSExecuteStatementResp | suyue                  |
| Update TSStatus to use TSStatusType, instead of using ~~TS_StatusCode, errorCode and errorMessage~~ | Zesong Sun             |
| Rename item in enum TSProtocolVersion from ~~TSFILE_SERVICE_PROTOCOL_V1~~ to IOTDB_SERVICE_PROTOCOL_V1 | qiaojialin             |
| Rename method name from ~~TSExecuteStatementResp executeInsertion(1:TSInsertionReq req)~~ to TSExecuteStatementResp insert(1:TSInsertionReq req) | qiaojialin             |
| Add required i32 compressor in TSCreateTimeseriesReq         | Jialin Qiao            |
| Add optional list\<string> nodesList, optional map\<string, string> nodeTimeseriesNum in TSFetchMetadataResp | jack870131             |
| Add optional i32 nodeLevel in TSFetchMetadataReq             | jack870131, Zesong Sun |
| Change the following methods' returned type to be TSStatus: <br />TSStatus closeSession(1:TSCloseSessionReq req), <br />TSStatus cancelOperation(1:TSCancelOperationReq req), <br />TSStatus closeOperation(1:TSCloseOperationReq req), <br />TSStatus setTimeZone(1:TSSetTimeZoneReq req), <br />TSStatus setStorageGroup(1:string storageGroup), <br />TSStatus createTimeseries(1:TSCreateTimeseriesReq req), <br />TSStatus insertRow(1:TSInsertReq req), <br />TSStatus deleteData(1:TSDeleteDataReq req) | Zesong Sun, qiaojialin |
| Change from ~~required string path~~ to required list\<string> paths in TSDeleteDataReq | qiaojialin             |
| Add optional set\<string> devices in TSFetchMetadataResp     | Zesong Sun             |
| Rename some fields in TSFetchMetadataResp: ~~ColumnsList~~ to columnsList, ~~showTimeseriesList~~ to timeseriesList, ~~showStorageGroups~~ to storageGroups | Zesong Sun             |
| Change struct TSQueryDataSet to eliminate row-wise rpc writing | Lei Rui                |
| Add optional i32 timeseriesNum in TSFetchMetadataResp        | Jack Tsai              |
| Add required i64 queryId in TSHandleIdentifier               | Yuan Tian    |
| Add optional set\<string> childPaths in TSFetchMetadataResp     | Haonan Hou             |
| Add optional string version in TSFetchMetadataResp           | Genius_pig             |
| Add required i64 statementId in TSExecuteStatementReq        | Yuan Tian |



