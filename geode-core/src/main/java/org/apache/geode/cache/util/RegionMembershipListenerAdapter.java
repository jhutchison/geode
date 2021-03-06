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
package org.apache.geode.cache.util;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionEvent;
import org.apache.geode.cache.RegionMembershipListener;
import org.apache.geode.distributed.DistributedMember;

/**
 * Utility class that implements all methods in <code>RegionMembershipListener</code> with empty
 * implementations. Applications can subclass this class and only override the methods for the
 * events of interest.
 *
 *
 * @since GemFire 5.0
 */
public abstract class RegionMembershipListenerAdapter<K, V> extends CacheListenerAdapter<K, V>
    implements RegionMembershipListener<K, V> {
  @Override
  public void initialMembers(Region<K, V> r, DistributedMember[] initialMembers) {}

  @Override
  public void afterRemoteRegionCreate(RegionEvent<K, V> event) {}

  @Override
  public void afterRemoteRegionDeparture(RegionEvent<K, V> event) {}

  @Override
  public void afterRemoteRegionCrash(RegionEvent<K, V> event) {}
}
