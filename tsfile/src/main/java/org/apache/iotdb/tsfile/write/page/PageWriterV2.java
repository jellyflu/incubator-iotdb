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
package org.apache.iotdb.tsfile.write.page;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import org.apache.iotdb.tsfile.compress.ICompressor;
import org.apache.iotdb.tsfile.encoding.encoder.Encoder;
import org.apache.iotdb.tsfile.file.header.PageHeader;
import org.apache.iotdb.tsfile.file.metadata.enums.CompressionType;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.apache.iotdb.tsfile.file.metadata.statistics.Statistics;
import org.apache.iotdb.tsfile.utils.Binary;
import org.apache.iotdb.tsfile.utils.PublicBAOS;
import org.apache.iotdb.tsfile.utils.ReadWriteForEncodingUtils;
import org.apache.iotdb.tsfile.write.schema.MeasurementSchema;
import org.apache.iotdb.tsfile.write.schemaV2.TimeseriesSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This writer is used to write time-value into a page. It consists of a time encoder,
 * a value encoder and respective OutputStream.
 */
public class PageWriterV2 {

  private static final Logger logger = LoggerFactory.getLogger(PageWriter.class);

  // time of the latest written time value pair, we assume data is written in time order
  private long pageMaxTime;
  private long pageMinTime = Long.MIN_VALUE;

  private ICompressor compressor;

  // time
  private Encoder timeEncoder;
  private PublicBAOS timeOut;
  // value
  private Encoder valueEncoder;
  private PublicBAOS valueOut;

  /**
   * statistic of current page. It will be reset after calling {@code writePageHeaderAndDataIntoBuff()}
   */
  private Statistics<?> statistics;
  private int pointNumber;

  public PageWriterV2() {
    this(null, null);
  }

  public PageWriterV2(TimeseriesSchema timeseriesSchema) {
    this(timeseriesSchema.getTimeEncoder(), timeseriesSchema.getValueEncoder());
    this.statistics = Statistics.getStatsByType(timeseriesSchema.getType());
    this.compressor = ICompressor.getCompressor(timeseriesSchema.getCompressionType());
  }

  private PageWriterV2(Encoder timeEncoder, Encoder valueEncoder) {
    this.timeOut = new PublicBAOS();
    this.valueOut = new PublicBAOS();
    this.timeEncoder = timeEncoder;
    this.valueEncoder = valueEncoder;
  }

  /**
   * write a time value pair into encoder
   */
  public void write(long time, boolean value) {
    ++pointNumber;
    this.pageMaxTime = time;
    if (pageMinTime == Long.MIN_VALUE) {
      pageMinTime = time;
    }
    timeEncoder.encode(time, timeOut);
    valueEncoder.encode(value, valueOut);
    statistics.update(time, value);
  }

  /**
   * write a time value pair into encoder
   */
  public void write(long time, short value) {
    ++pointNumber;
    timeEncoder.encode(time, timeOut);
    valueEncoder.encode(value, valueOut);
    statistics.update(time, value);
  }

  /**
   * write a time value pair into encoder
   */
  public void write(long time, int value) {
    ++pointNumber;
    this.pageMaxTime = time;
    if (pageMinTime == Long.MIN_VALUE) {
      pageMinTime = time;
    }
    timeEncoder.encode(time, timeOut);
    valueEncoder.encode(value, valueOut);
    statistics.update(time, value);
  }

  /**
   * write a time value pair into encoder
   */
  public void write(long time, long value) {
    ++pointNumber;
    this.pageMaxTime = time;
    if (pageMinTime == Long.MIN_VALUE) {
      pageMinTime = time;
    }
    timeEncoder.encode(time, timeOut);
    valueEncoder.encode(value, valueOut);
    statistics.update(time, value);
  }

  /**
   * write a time value pair into encoder
   */
  public void write(long time, float value) {
    ++pointNumber;
    this.pageMaxTime = time;
    if (pageMinTime == Long.MIN_VALUE) {
      pageMinTime = time;
    }
    timeEncoder.encode(time, timeOut);
    valueEncoder.encode(value, valueOut);
    statistics.update(time, value);
  }

  /**
   * write a time value pair into encoder
   */
  public void write(long time, double value) {
    ++pointNumber;
    this.pageMaxTime = time;
    if (pageMinTime == Long.MIN_VALUE) {
      pageMinTime = time;
    }
    timeEncoder.encode(time, timeOut);
    valueEncoder.encode(value, valueOut);
    statistics.update(time, value);
  }

  /**
   * write a time value pair into encoder
   */
  public void write(long time, Binary value) {
    ++pointNumber;
    this.pageMaxTime = time;
    if (pageMinTime == Long.MIN_VALUE) {
      pageMinTime = time;
    }
    timeEncoder.encode(time, timeOut);
    valueEncoder.encode(value, valueOut);
    statistics.update(time, value);
  }

  /**
   * write time series into encoder
   */
  public void write(long[] timestamps, boolean[] values, int batchSize) {
    pointNumber += batchSize;
    this.pageMaxTime = timestamps[batchSize - 1];
    if (pageMinTime == Long.MIN_VALUE) {
      pageMinTime = timestamps[0];
    }
    for (int i = 0; i < batchSize; i++) {
      timeEncoder.encode(timestamps[i], timeOut);
      valueEncoder.encode(values[i], valueOut);
    }
    statistics.update(timestamps, values, batchSize);
  }

  /**
   * write time series into encoder
   */
  public void write(long[] timestamps, int[] values, int batchSize) {
    pointNumber += batchSize;
    this.pageMaxTime = timestamps[batchSize - 1];
    if (pageMinTime == Long.MIN_VALUE) {
      pageMinTime = timestamps[0];
    }
    for (int i = 0; i < batchSize; i++) {
      timeEncoder.encode(timestamps[i], timeOut);
      valueEncoder.encode(values[i], valueOut);
    }
    statistics.update(timestamps, values, batchSize);
  }

  /**
   * write time series into encoder
   */
  public void write(long[] timestamps, long[] values, int batchSize) {
    pointNumber += batchSize;
    this.pageMaxTime = timestamps[batchSize - 1];
    if (pageMinTime == Long.MIN_VALUE) {
      pageMinTime = timestamps[0];
    }
    for (int i = 0; i < batchSize; i++) {
      timeEncoder.encode(timestamps[i], timeOut);
      valueEncoder.encode(values[i], valueOut);
    }
    statistics.update(timestamps, values, batchSize);
  }

  /**
   * write time series into encoder
   */
  public void write(long[] timestamps, float[] values, int batchSize) {
    pointNumber += batchSize;
    this.pageMaxTime = timestamps[batchSize - 1];
    if (pageMinTime == Long.MIN_VALUE) {
      pageMinTime = timestamps[0];
    }
    for (int i = 0; i < batchSize; i++) {
      timeEncoder.encode(timestamps[i], timeOut);
      valueEncoder.encode(values[i], valueOut);
    }
    statistics.update(timestamps, values, batchSize);
  }

  /**
   * write time series into encoder
   */
  public void write(long[] timestamps, double[] values, int batchSize) {
    pointNumber += batchSize;
    this.pageMaxTime = timestamps[batchSize - 1];
    if (pageMinTime == Long.MIN_VALUE) {
      pageMinTime = timestamps[0];
    }
    for (int i = 0; i < batchSize; i++) {
      timeEncoder.encode(timestamps[i], timeOut);
      valueEncoder.encode(values[i], valueOut);
    }
    statistics.update(timestamps, values, batchSize);
  }

  /**
   * write time series into encoder
   */
  public void write(long[] timestamps, Binary[] values, int batchSize) {
    pointNumber += batchSize;
    this.pageMaxTime = timestamps[batchSize - 1];
    if (pageMinTime == Long.MIN_VALUE) {
      pageMinTime = timestamps[0];
    }
    for (int i = 0; i < batchSize; i++) {
      timeEncoder.encode(timestamps[i], timeOut);
      valueEncoder.encode(values[i], valueOut);
    }
    statistics.update(timestamps, values, batchSize);
  }

  /**
   * flush all data remained in encoders.
   */
  private void prepareEndWriteOnePage() throws IOException {
    timeEncoder.flush(timeOut);
    valueEncoder.flush(valueOut);
  }

  /**
   * getUncompressedBytes return data what it has been written in form of
   * <code>size of time list, time list, value list</code>
   *
   * @return a new readable ByteBuffer whose position is 0.
   */
  public ByteBuffer getUncompressedBytes() throws IOException {
    prepareEndWriteOnePage();
    ByteBuffer buffer = ByteBuffer.allocate(timeOut.size() + valueOut.size() + 4);
    ReadWriteForEncodingUtils.writeUnsignedVarInt(timeOut.size(), buffer);
    buffer.put(timeOut.getBuf(), 0, timeOut.size());
    buffer.put(valueOut.getBuf(), 0, valueOut.size());
    buffer.flip();
    return buffer;
  }

  public long getPageMaxTime() {
    return pageMaxTime;
  }

  public long getPageMinTime() {
    return pageMinTime;
  }

  /**
   * write the page header and data into the PageWriter's output stream.
   */
  public void writePageHeaderAndDataIntoBuff(PublicBAOS pageBuffer) throws IOException {
    if (pointNumber == 0) {
      return;
    }

    ByteBuffer pageData = getUncompressedBytes();
    int uncompressedSize = pageData.remaining();
    int compressedSize;
    int compressedPosition = 0;
    byte[] compressedBytes = null;

    if (compressor.getType().equals(CompressionType.UNCOMPRESSED)) {
      compressedSize = pageData.remaining();
    } else {
      compressedBytes = new byte[compressor.getMaxBytesForCompression(uncompressedSize)];
      compressedPosition = 0;
      // data is never a directByteBuffer now, so we can use data.array()
      compressedSize = compressor
          .compress(pageData.array(), pageData.position(), uncompressedSize, compressedBytes);
    }

    // write the page header to IOWriter
    PageHeader header = new PageHeader(uncompressedSize, compressedSize, pointNumber, statistics,
        pageMaxTime, pageMinTime);
    header.serializeTo(pageBuffer);

    // write page content to temp PBAOS
    try (WritableByteChannel channel = Channels.newChannel(pageBuffer)) {
      logger.debug("start to flush a page data into buffer, buffer position {} ", pageBuffer.size());
      if (compressor.getType().equals(CompressionType.UNCOMPRESSED)) {
        channel.write(pageData);
      } else {
        pageBuffer.write(compressedBytes, compressedPosition, compressedSize);
      }
      logger.debug("start to flush a page data into buffer, buffer position {} ", pageBuffer.size());
    }
  }

  /**
   * calculate max possible memory size it occupies, including time outputStream and value outputStream, because size
   * outputStream is never used until flushing.
   *
   * @return allocated size in time, value and outputStream
   */
  public long estimateMaxMemSize() {
    return timeOut.size() + valueOut.size() + timeEncoder.getMaxByteSize() + valueEncoder
        .getMaxByteSize();
  }

  /**
   * reset this page
   */
  public void reset(TimeseriesSchema measurementSchema) {
    timeOut.reset();
    valueOut.reset();
    pointNumber =0;
    pageMinTime = Long.MIN_VALUE;
    pageMaxTime = Long.MIN_VALUE;
    statistics = Statistics.getStatsByType(measurementSchema.getType());
  }

  public void setTimeEncoder(Encoder encoder) {
    this.timeEncoder = encoder;
  }

  public void setValueEncoder(Encoder encoder) {
    this.valueEncoder = encoder;
  }

  public void initStatistics(TSDataType dataType) {
    statistics = Statistics.getStatsByType(dataType);
  }

  public int getPointNumber(){
    return pointNumber;
  }

  public Statistics<?> getStatistics(){
    return statistics;
  }

}