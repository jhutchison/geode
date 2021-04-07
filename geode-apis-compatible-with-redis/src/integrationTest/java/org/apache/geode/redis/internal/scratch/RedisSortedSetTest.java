package org.apache.geode.redis.internal.scratch;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.Test;

public class RedisSortedSetTest {

  private RedisSortedSet subject = new RedisSortedSet();

  @Test
  public void shouldReturnObjectsInRankedOrder() {

    for (int i = 0; i < 10000; i++) {
      subject.add(new RedisSortedSet.Player((int) (i * Math.random()), "player" + i));
    }

    Object[] resultsAsArr = subject.getPlayerArray();

    for (int i = 0; i < resultsAsArr.length; i++) {
      RedisSortedSet.Player player = (RedisSortedSet.Player) resultsAsArr[i];
      System.out.println(player.getValue() + "  " + player.getScore());

      if (i > 0) {
        RedisSortedSet.Player previousPlayer = (RedisSortedSet.Player) resultsAsArr[i - 1];
        assertThat(player.getScore()).isGreaterThan(previousPlayer.getScore());
      }
    }
  }

  @Test
  public void shouldReturnObjectsInRevRankedOrder() {

    for (int i = 0; i < 10000; i++) {
      subject.add(new RedisSortedSet.Player((int) (i * Math.random()), "player" + i));
    }

    Object[] revResultsAsArr = subject.getReverseRankedPlayers();

    for (int i = 0; i < revResultsAsArr.length; i++) {
      RedisSortedSet.Player player = (RedisSortedSet.Player) revResultsAsArr[i];
      System.out.println(player.getValue() + "  " + player.getScore());

      if (i > 0) {
        RedisSortedSet.Player previousPlayer = (RedisSortedSet.Player) revResultsAsArr[i - 1];
        assertThat(player.getScore()).isLessThan(previousPlayer.getScore());
      }
    }
  }

    @Test
    public void shouldRemove() {

    subject.add(new RedisSortedSet.Player( 1, "Elvis"));
    subject.add(new RedisSortedSet.Player( 2, "Elvis"));
    assertThat(subject.getSize()).isEqualTo(2);

    subject.remove(new RedisSortedSet.Player(1, "Elvis"));
      assertThat(subject.getSize()).isEqualTo(1);

    }

    @Test
    public void should_ZREVRANGEBYSCORE(){

      for (int i = 0; i < 100; i++) {
        subject.add(new RedisSortedSet.Player( i, "player" + i));
      }

      Object[] actual = subject.revRangeByScore(
                  new RedisSortedSet.Player(5, ""),
                  new RedisSortedSet.Player(10, "")
              );


      for (int i = 0; i < actual.length; i++) {
        RedisSortedSet.Player player = (RedisSortedSet.Player) actual[i];
        System.out.println(player.getValue() + "  " + player.getScore());
      }

    }
}
