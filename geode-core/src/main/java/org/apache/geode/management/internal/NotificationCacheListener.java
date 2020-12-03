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
package org.apache.geode.management.internal;



import javax.management.Notification;

import org.apache.geode.CancelCriterion;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.util.CacheListenerAdapter;
import org.apache.geode.internal.cache.InternalCacheForClientAccess;
import org.apache.geode.internal.util.concurrent.StoppableCountDownLatch;

/**
 * This listener will be attached to each notification region corresponding to a member
 */
public class NotificationCacheListener extends CacheListenerAdapter<NotificationKey, Notification> {

  private final NotificationHubClient notifClient;
  private final StoppableCountDownLatch readyForEvents;

  public NotificationCacheListener(InternalCacheForClientAccess cache,
      MBeanProxyFactory proxyHelper) {
    notifClient =
        new NotificationHubClient(proxyHelper);

    this.readyForEvents =
        new StoppableCountDownLatch(new CacheListenerCancelCriterion(cache), 1);
  }

  @Override
  public void afterCreate(EntryEvent<NotificationKey, Notification> event) {
    blockUntilReady();
    notifClient.sendNotification(event);
  }

  @Override
  public void afterUpdate(EntryEvent<NotificationKey, Notification> event) {
    blockUntilReady();
    notifClient.sendNotification(event);
  }

  private void blockUntilReady() {
    try {
      readyForEvents.await();
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new RuntimeException(e);
    }
  }

  void markReady() {
    readyForEvents.countDown();
  }

  private class CacheListenerCancelCriterion extends CancelCriterion {
    private InternalCacheForClientAccess cache;

    public CacheListenerCancelCriterion(InternalCacheForClientAccess cache) {
      this.cache = cache;
    }

    @Override
    public String cancelInProgress() {
      String reason = cache.getCancelCriterion().cancelInProgress();
      if (reason != null) {
        return reason;
      }
      return null;
    }

    @Override
    public RuntimeException generateCancelledException(Throwable throwable) {
      return null;
    }
  }
}
