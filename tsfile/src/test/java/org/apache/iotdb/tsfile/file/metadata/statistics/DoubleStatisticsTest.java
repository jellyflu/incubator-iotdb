/*
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
package org.apache.iotdb.tsfile.file.metadata.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class DoubleStatisticsTest {

  private static final double maxError = 0.0001d;

  @Test
  public void testUpdate() {
    Statistics<Double> doubleStats = new DoubleStatistics();
    doubleStats.updateStats(1.34d);
    assertFalse(doubleStats.isEmpty());
    doubleStats.updateStats(2.32d);
    assertFalse(doubleStats.isEmpty());
    assertEquals(2.32d, doubleStats.getMaxValue(), maxError);
    assertEquals(1.34d, doubleStats.getMinValue(), maxError);
    assertEquals(2.32d + 1.34d, doubleStats.getSumValue(), maxError);
  }

  @Test
  public void testMerge() {
    Statistics<Double> doubleStats1 = new DoubleStatistics();
    Statistics<Double> doubleStats2 = new DoubleStatistics();

    doubleStats1.updateStats(1.34d);
    doubleStats1.updateStats(100.13453d);

    doubleStats2.updateStats(200.435d);

    Statistics<Double> doubleStats3 = new DoubleStatistics();
    doubleStats3.mergeStatistics(doubleStats1);
    assertFalse(doubleStats3.isEmpty());
    assertEquals(100.13453d, doubleStats3.getMaxValue(), maxError);
    assertEquals(1.34d, doubleStats3.getMinValue(), maxError);
    assertEquals(100.13453d + 1.34d, doubleStats3.getSumValue(), maxError);

    doubleStats3.mergeStatistics(doubleStats2);
    assertEquals(200.435d, doubleStats3.getMaxValue(), maxError);
    assertEquals(1.34d, doubleStats3.getMinValue(), maxError);
    assertEquals(100.13453d + 1.34d + 200.435d, doubleStats3.getSumValue(), maxError);
  }

}
