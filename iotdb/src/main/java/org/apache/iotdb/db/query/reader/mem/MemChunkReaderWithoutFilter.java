/**
 * Copyright © 2019 Apache IoTDB(incubating) (dev@iotdb.apache.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.iotdb.db.query.reader.mem;

import org.apache.iotdb.db.engine.memtable.TimeValuePairSorter;
import org.apache.iotdb.db.query.reader.IReader;
import org.apache.iotdb.db.utils.TimeValuePair;
import org.apache.iotdb.tsfile.read.common.BatchData;
import org.apache.iotdb.db.engine.memtable.TimeValuePairSorter;

import java.io.IOException;
import java.util.Iterator;

// TODO merge MemChunkReaderWithoutFilter and MemChunkReaderWithFilter to one class
public class MemChunkReaderWithoutFilter implements IReader {

    private Iterator<TimeValuePair> timeValuePairIterator;

    public MemChunkReaderWithoutFilter(TimeValuePairSorter readableChunk) {
        timeValuePairIterator = readableChunk.getIterator();
    }

    @Override
    public boolean hasNext() {
        return timeValuePairIterator.hasNext();
    }

    @Override
    public TimeValuePair next() {
        return timeValuePairIterator.next();
    }

    @Override
    public void skipCurrentTimeValuePair() {
        next();
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean hasNextBatch() {
        return false;
    }

    @Override
    public BatchData nextBatch() {
        return null;
    }

    @Override
    public BatchData currentBatch() {
        return null;
    }
}