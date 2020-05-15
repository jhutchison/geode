/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.geode.redis.internal.executor.string;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.geode.DataSerializable;
import org.apache.geode.DataSerializer;
import org.apache.geode.Delta;
import org.apache.geode.cache.Region;
import org.apache.geode.redis.internal.ByteArrayWrapper;

public class RedisString implements DataSerializable {
  private ByteArrayWrapper value;

  // TODO: deltas

  public RedisString(ByteArrayWrapper value) {
    this.value = value;
  }

  // for serialization
  public RedisString() {
  }

  public int append(ByteArrayWrapper appendValue, Region<ByteArrayWrapper, RedisString> region,
                    ByteArrayWrapper key) {

    RedisString redisString = region.get(key);

    if (redisString == null) {
      value = appendValue;
    } else {
      byte[] newValue = concatArrays(value.toBytes(), appendValue.toBytes());
      value.setBytes(newValue);
    }

    region.put(key, this);
    return value.length();
  }

  public ByteArrayWrapper get(Region<ByteArrayWrapper, RedisString> region, ByteArrayWrapper key) {
    return region.get(key).getValue();
  }

  public RedisString set(ByteArrayWrapper value, Region<ByteArrayWrapper, RedisString> region, ByteArrayWrapper key) {
    this.value = value;
    return region.put(key, this);
  }

  public ByteArrayWrapper getValue() {
    return value;
  }

  private byte[] concatArrays(byte[] o, byte[] n) {
    int oLen = o.length;
    int nLen = n.length;
    byte[] combined = new byte[oLen + nLen];
    System.arraycopy(o, 0, combined, 0, oLen);
    System.arraycopy(n, 0, combined, oLen, nLen);
    return combined;
  }

  @Override
  public void toData(DataOutput out) throws IOException {
    DataSerializer.writeByteArray(value.toBytes(), out);
  }

  @Override
  public void fromData(DataInput in) throws IOException {
    value = new ByteArrayWrapper(DataSerializer.readByteArray(in));
  }

}
