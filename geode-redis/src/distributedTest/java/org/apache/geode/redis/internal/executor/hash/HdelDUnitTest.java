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

package org.apache.geode.redis.internal.executor.hash;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.RedisException;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import org.apache.geode.redis.ConcurrentLoopingThreads;
import org.apache.geode.test.awaitility.GeodeAwaitility;
import org.apache.geode.test.dunit.rules.MemberVM;
import org.apache.geode.test.dunit.rules.RedisClusterStartupRule;

public class HdelDUnitTest {

  @ClassRule
  public static RedisClusterStartupRule clusterStartUp = new RedisClusterStartupRule();

  private static final String LOCAL_HOST = "127.0.0.1";
  private static final int HASH_SIZE = 10000;
  private static final int JEDIS_TIMEOUT =
      Math.toIntExact(GeodeAwaitility.getTimeout().toMillis());

  private static Jedis jedis1;
  private static Jedis jedis2;

  private static MemberVM locator;
  private static MemberVM server1;
  private static MemberVM server2;

  private static int redisServerPort1;
  private static int redisServerPort2;

  @BeforeClass
  public static void classSetup() {

    locator = clusterStartUp.startLocatorVM(0);
    server1 = clusterStartUp.startRedisVM(1, locator.getPort());
    server2 = clusterStartUp.startRedisVM(2, locator.getPort());

    redisServerPort1 = clusterStartUp.getRedisPort(1);
    redisServerPort2 = clusterStartUp.getRedisPort(2);

    jedis1 = new Jedis(LOCAL_HOST, redisServerPort1, JEDIS_TIMEOUT);
    jedis2 = new Jedis(LOCAL_HOST, redisServerPort2, JEDIS_TIMEOUT);
  }

  @Before
  public void testSetup() {
    jedis1.flushAll();
  }

  @AfterClass
  public static void tearDown() {
    jedis1.disconnect();
    jedis2.disconnect();

    server1.stop();
    server2.stop();
  }


  @Test
  public void testConcurrentHDelReturnExceptedNumberOfDeletions() {

    AtomicLong client1Deletes = new AtomicLong();
    AtomicLong client2Deletes = new AtomicLong();

    String key = "HSET";

    Map setUpData =
        makeHashMap(HASH_SIZE, "field", "value");

    jedis1.hset(key, setUpData);

    new ConcurrentLoopingThreads(HASH_SIZE,
        (i) -> {
          Long deleted = jedis1.hdel(key, "field" + i, "value" + i);
          client1Deletes.addAndGet(deleted);},
        (i) -> {
          Long deleted = jedis2.hdel(key, "field" + i, "value" + i);
          client2Deletes.addAndGet(deleted);})
        .run();

    assertThat(client1Deletes.get() + client2Deletes.get()).isEqualTo(HASH_SIZE);
  }

  public void testConcurrentHDel_whenOneServerCrashes_ReturnExceptedNumberOfDeletions() {

    AtomicLong client1Deletes = new AtomicLong();
    AtomicLong client2Deletes = new AtomicLong();

    String key = "HSET";

    Map setUpData =
        makeHashMap(HASH_SIZE, "field", "value");

    jedis1.hset(key, setUpData);

    new ConcurrentLoopingThreads(HASH_SIZE,
        (i) -> {
          Long deleted = jedis1.hdel(key, "field" + i, "value" + i);
          client1Deletes.addAndGet(deleted);},
        (i) -> {
          Long deleted = jedis2.hdel(key, "field" + i, "value" + i);
          client2Deletes.addAndGet(deleted);})
        .run();



    assertThat(client1Deletes.get() + client2Deletes.get()).isEqualTo(HASH_SIZE);
  }


  private void hsetPerformAndVerify(int index, int minimumIterations, AtomicBoolean isRunning) {
    String key = "hset-key-" + index;
    int iterationCount = 0;

    while (iterationCount < minimumIterations || isRunning.get()) {
      String fieldName = "field-" + iterationCount;
      try {
        commands.hset(key, fieldName, "value-" + iterationCount);
        iterationCount += 1;
      } catch (RedisCommandExecutionException ignore) {
      } catch (RedisException ex) {
        if (ex.getMessage().contains("Connection reset by peer")) {
          // ignore it
        } else {
          throw ex;
        }
      }
    }

    for (int i = 0; i < iterationCount; i++) {
      String field = "field-" + i;
      String value = "value-" + i;
      Assertions.assertThat(commands.hget(key, field)).isEqualTo(value);
    }

    logger.info("--->>> HSET test ran {} iterations", iterationCount);
  }


  private Map<String, String> makeHashMap(int hashSize, String baseFieldName,
                                          String baseValueName) {
    Map<String, String> map = new HashMap<>();
    for (int i = 0; i < hashSize; i++) {
      map.put(baseFieldName + i, baseValueName + i);
    }
    return map;
  }
}
