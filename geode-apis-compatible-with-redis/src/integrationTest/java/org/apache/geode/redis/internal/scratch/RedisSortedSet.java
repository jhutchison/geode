package org.apache.geode.redis.internal.scratch;

import java.util.HashMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class RedisSortedSet {

  private ConcurrentSkipListSet<Player> skipList;
  private HashMap<Integer,Player> hashMap;

  public RedisSortedSet(){
    this.skipList = new ConcurrentSkipListSet<>();
    this.hashMap = new HashMap<>();
  }

  public synchronized void add(Player player){
    skipList.add(player);
//    System.out.println("size in add :" + skipList.size());
//    System.out.println("adding: " + player);
//    hashMap.put(player.getScore(), player);
  }


  public Set<Player> getPlayers(){
//    System.out.println("size:" + skipList.size());
    return skipList;
  }

  public Object[] getPlayerArray(){
//    System.out.println("size:" + skipList.size());
    return skipList.toArray();
  }

  public Object[] revRangeByScore(Player low, Player high){
    NavigableSet<Player> result = skipList.subSet(low, high);
    NavigableSet<Player> reverseResult = result.descendingSet();
    return reverseResult.toArray();
  }

  public Object[] getReverseRankedPlayers(){

    Set result = this.skipList.descendingSet();
    return result.toArray();
  }

  public int getSize() {
    return this.skipList.size();
  }

  public void remove(Player player) {
    this.skipList.remove(player);
  }

//  public Object[] zrevrange(int start, int end){
//    this.skipList
//  }


  public static class Player implements Comparable {
    public void setScore(int score) {
      this.score = score;
    }

    int score;
    String value;

    public Player(int score, String value){
      this.score= score;
      this.value = value;
    }

    public int getScore() {
      return this.score;
    }

    public  Object getValue() {
      return this.value;
    }



    @Override
    public int compareTo(Object otherPlayer){
      Player other = (Player)otherPlayer;

//      System.out.println("comparing " + other.getScore() + "to myself " + this.getScore());

      int result= 0;

      if (getScore() > other.getScore()){
        result= 1;
      }

      if (getScore() < other.getScore()) {
        result = -1;
      }
//      System.out.println("result is " + result);
      return result;
    }
  }

}
