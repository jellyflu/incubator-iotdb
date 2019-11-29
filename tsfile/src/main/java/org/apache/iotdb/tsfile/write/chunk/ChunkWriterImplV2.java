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
package org.apache.iotdb.tsfile.write.chunk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import org.apache.iotdb.tsfile.common.conf.TSFileDescriptor;
import org.apache.iotdb.tsfile.compress.ICompressor;
import org.apache.iotdb.tsfile.exception.write.PageException;
import org.apache.iotdb.tsfile.file.header.ChunkHeader;
import org.apache.iotdb.tsfile.file.header.PageHeader;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.apache.iotdb.tsfile.file.metadata.statistics.Statistics;
import org.apache.iotdb.tsfile.utils.Binary;
import org.apache.iotdb.tsfile.utils.PublicBAOS;
import org.apache.iotdb.tsfile.write.page.PageWriter;
import org.apache.iotdb.tsfile.write.page.PageWriterV2;
import org.apache.iotdb.tsfile.write.schemaV2.TimeseriesSchema;
import org.apache.iotdb.tsfile.write.writer.TsFileIOWriter;
import org.apache.iotdb.tsfile.write.writer.TsFileIOWriterV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChunkWriterImplV2 {

  private static final Logger logger = LoggerFactory.getLogger(ChunkWriterImpl.class);

  private TimeseriesSchema timeseriesSchema;

  private ICompressor compressor;

  /**
   * all pages of this column.
   */
  private PublicBAOS pageBuffer;

  private int numOfPages;

  /**
   * write data into current page
   */
  private PageWriterV2 pageWriter;

  /**
   * page size threshold.
   */
  private final long pageSizeThreshold;

  private final int maxNumberOfPointsInPage;

  /**
   * value count in current page.
   */
  private int valueCountInOnePageForNextCheck;

  // initial value for valueCountInOnePageForNextCheck
  private static final int MINIMUM_RECORD_COUNT_FOR_CHECK = 1500;

  /**
   * statistic of this chunk.
   */
  private Statistics<?> chunkStatistics;

  /**
   * @param schema schema of this measurement
   */
  public ChunkWriterImplV2(TimeseriesSchema schema) {
    this.timeseriesSchema = schema;
    this.compressor = ICompressor.getCompressor(schema.getCompressionType());
    this.pageBuffer = new PublicBAOS();

    this.pageSizeThreshold = TSFileDescriptor.getInstance().getConfig().getPageSizeInByte();
    this.maxNumberOfPointsInPage = TSFileDescriptor.getInstance().getConfig()
        .getMaxNumberOfPointsInPage();
    // initial check of memory usage. So that we have enough data to make an initial prediction
    this.valueCountInOnePageForNextCheck = MINIMUM_RECORD_COUNT_FOR_CHECK;

    // init statistics for this chunk and page
    this.chunkStatistics = Statistics.getStatsByType(timeseriesSchema.getType());

    this.pageWriter = new PageWriterV2(timeseriesSchema);
    this.pageWriter.setTimeEncoder(timeseriesSchema.getTimeEncoder());
    this.pageWriter.setValueEncoder(timeseriesSchema.getValueEncoder());
  }

  public void write(long time, long value) {
    pageWriter.write(time, value);
    checkPageSizeAndMayOpenANewPage();
  }

  public void write(long time, int value) {
    pageWriter.write(time, value);
    checkPageSizeAndMayOpenANewPage();
  }

  public void write(long time, boolean value) {
    pageWriter.write(time, value);
    checkPageSizeAndMayOpenANewPage();
  }

  public void write(long time, float value) {
    pageWriter.write(time, value);
    checkPageSizeAndMayOpenANewPage();
  }

  public void write(long time, double value) {
    pageWriter.write(time, value);
    checkPageSizeAndMayOpenANewPage();
  }

  public void write(long time, Binary value) {
    pageWriter.write(time, value);
    checkPageSizeAndMayOpenANewPage();
  }

  public void write(long[] timestamps, int[] values, int batchSize) {
    pageWriter.write(timestamps, values, batchSize);
    checkPageSizeAndMayOpenANewPage();
  }

  public void write(long[] timestamps, long[] values, int batchSize) {
    pageWriter.write(timestamps, values, batchSize);
    checkPageSizeAndMayOpenANewPage();
  }

  public void write(long[] timestamps, boolean[] values, int batchSize) {
    pageWriter.write(timestamps, values, batchSize);
    checkPageSizeAndMayOpenANewPage();
  }

  public void write(long[] timestamps, float[] values, int batchSize) {
    pageWriter.write(timestamps, values, batchSize);
    checkPageSizeAndMayOpenANewPage();
  }

  public void write(long[] timestamps, double[] values, int batchSize) {
    pageWriter.write(timestamps, values, batchSize);
    checkPageSizeAndMayOpenANewPage();
  }

  public void write(long[] timestamps, Binary[] values, int batchSize) {
    pageWriter.write(timestamps, values, batchSize);
    checkPageSizeAndMayOpenANewPage();
  }

  /**
   * check occupied memory size, if it exceeds the PageSize threshold, flush them to given
   * OutputStream.
   */
  private void checkPageSizeAndMayOpenANewPage() {
    if (pageWriter.getPointNumber() == maxNumberOfPointsInPage) {
      logger.debug("current line count reaches the upper bound, write page {}", timeseriesSchema);
      writePage();
    } else if (pageWriter.getPointNumber()
        >= valueCountInOnePageForNextCheck) { // need to check memory size
      // not checking the memory used for every value
      long currentPageSize = pageWriter.estimateMaxMemSize();
      if (currentPageSize > pageSizeThreshold) { // memory size exceeds threshold
        // we will write the current page
        logger.debug(
            "enough size, write page {}, pageSizeThreshold:{}, currentPateSize:{}, valueCountInOnePage:{}",
            timeseriesSchema.getMeasurementId(), pageSizeThreshold, currentPageSize,
            pageWriter.getPointNumber());
        writePage();
        valueCountInOnePageForNextCheck = MINIMUM_RECORD_COUNT_FOR_CHECK;
      } else {
        // reset the valueCountInOnePageForNextCheck for the next page
        valueCountInOnePageForNextCheck = (int) (((float) pageSizeThreshold / currentPageSize)
            * pageWriter.getPointNumber());
      }
    }
  }

  private void writePage() {
    try {
      pageWriter.writePageHeaderAndDataIntoBuff(pageBuffer);

      // update statistics of this chunk
      numOfPages++;
      this.chunkStatistics.mergeStatistics(pageWriter.getStatistics());
    } catch (IOException e) {
      logger.error("meet error in pageWriter.writePageHeaderAndDataIntoBuff,ignore this page:", e);
    } finally {
      // clear start time stamp for next initializing
      pageWriter.reset(timeseriesSchema);
    }
  }

  public void writeToFileWriter(TsFileIOWriterV2 tsfileWriter) throws IOException {
    // seal current page
    if (pageWriter.getPointNumber() > 0) {
      writePage();
    }

    writeAllPagesOfChunkToTsFile(tsfileWriter, chunkStatistics);

    // reinit this chunk writer
    pageBuffer.reset();
    this.chunkStatistics = Statistics.getStatsByType(timeseriesSchema.getType());
  }

  public long estimateMaxSeriesMemSize() {
    return pageWriter.estimateMaxMemSize() + this.estimateMaxPageMemSize();
  }

  public long getCurrentChunkSize() {
    // return the serialized size of the chunk header + all pages
    return ChunkHeader.getSerializedSize(timeseriesSchema.getMeasurementId()) + this
        .getCurrentDataSize();
  }

  public int getNumOfPages() {
    return numOfPages;
  }

  public TSDataType getDataType() {
    return timeseriesSchema.getType();
  }

  /**
   * write the page to specified IOWriter.
   *
   * @param writer     the specified IOWriter
   * @param statistics the chunk statistics
   * @throws IOException exception in IO
   */
  public void writeAllPagesOfChunkToTsFile(TsFileIOWriterV2 writer, Statistics<?> statistics)
      throws IOException {
    if (statistics.getCount() == 0) {
      return;
    }

    // start to write this column chunk
    writer.startFlushChunk(timeseriesSchema, compressor.getType(), timeseriesSchema.getType(),
        timeseriesSchema.getEncodingType(), statistics, pageBuffer.size(), numOfPages);

    // write all pages of this column
    writer.writeBytesToStream(pageBuffer);

    writer.endCurrentChunk();
  }

  /**
   * estimate max page memory size.
   *
   * @return the max possible allocated size currently
   */
  private long estimateMaxPageMemSize() {
    // return the sum of size of buffer and page max size
    return (long) (pageBuffer.size() +
        PageHeader.calculatePageHeaderSizeWithoutStatistics() +
        pageWriter.getStatistics().getSerializedSize());
  }

  /**
   * get current data size.
   *
   * @return current data size that the writer has serialized.
   */
  private long getCurrentDataSize() {
    return pageBuffer.size();
  }
}