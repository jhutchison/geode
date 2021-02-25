package org.apache.geode.redis.internal.data;

import static org.apache.geode.distributed.ConfigurationProperties.MAX_WAIT_TIME_RECONNECT;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import org.apache.geode.test.awaitility.GeodeAwaitility;
import org.apache.geode.test.dunit.rules.MemberVM;
import org.apache.geode.test.dunit.rules.RedisClusterStartupRule;

public class BucketSizeTest {

  @ClassRule
  public static RedisClusterStartupRule clusterStartUpRule = new RedisClusterStartupRule(2);

  private static MemberVM locator;
  private static MemberVM server1;

  private static Properties locatorProperties;

  private static final String LOCAL_HOST = "127.0.0.1";
  private static final int JEDIS_TIMEOUT =
      Math.toIntExact(GeodeAwaitility.getTimeout().toMillis());

  private static Jedis jedis1;
  private static int redisServerPort1;
  private static int locatorPort;


  @BeforeClass
  public static void classSetup() {
    locatorProperties = new Properties();
    locatorProperties.setProperty(MAX_WAIT_TIME_RECONNECT, "15000");

    locator = clusterStartUpRule.startLocatorVM(0, locatorProperties);
    locatorPort = locator.getPort();

    server1 = clusterStartUpRule.startRedisVM(1, locatorPort);
    redisServerPort1 = clusterStartUpRule.getRedisPort(1);

    jedis1 = new Jedis(LOCAL_HOST, redisServerPort1, JEDIS_TIMEOUT);

  }


  @Test
  public void should_show_increase_in_dataStore_bytes_in_use_given_data_added() {
    String KEY = "key";
    String LONG_APPEND_VALUE = String.valueOf(Integer.MAX_VALUE);
    jedis1.set(KEY, "value");

    long initialDataStoreBytesInUse =
        clusterStartUpRule.getDataStoreBytesInUseForDataRegion(server1);

    for (int i = 0; i < 1000; i++) {
      jedis1.append(KEY, LONG_APPEND_VALUE);
    }

    long finalDataStoreBytesInUse = clusterStartUpRule.getDataStoreBytesInUseForDataRegion(server1);

    assertThat(initialDataStoreBytesInUse).isLessThan(finalDataStoreBytesInUse);

  }
}
