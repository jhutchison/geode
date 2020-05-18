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
package org.apache.geode.redis.internal.executor.set;

import java.util.Collection;
import java.util.List;

import org.apache.geode.redis.internal.ByteArrayWrapper;
import org.apache.geode.redis.internal.Coder;
import org.apache.geode.redis.internal.CoderException;
import org.apache.geode.redis.internal.Command;
import org.apache.geode.redis.internal.ExecutionHandlerContext;
import org.apache.geode.redis.internal.RedisConstants;

public class SRandMemberExecutor extends SetExecutor {

  private static final String ERROR_NOT_NUMERIC = "The count provided must be numeric";

  @Override
  public void executeCommand(Command command, ExecutionHandlerContext context) {
    List<byte[]> commandElems = command.getProcessedCommand();

    ByteArrayWrapper key = command.getKey();

    int count = 1;

    if (commandElems.size() > 2) {
      try {
        count = Coder.bytesToInt(commandElems.get(2));
      } catch (NumberFormatException e) {
        command.setResponse(
            Coder.getErrorResponse(context.getByteBufAllocator(), ERROR_NOT_NUMERIC));
        return;
      }
    }
    if (count == 0) {
      command.setResponse(Coder.getNilResponse(context.getByteBufAllocator()));
      return;
    }
    if (count < 0) {
      count = -count;
    }

    RedisSetCommands redisSetCommands =
        new RedisSetCommandsFunctionExecutor(context.getRegionProvider().getDataRegion());
    Collection<ByteArrayWrapper> results = redisSetCommands.srandmember(key, count);
    try {
      if (results.isEmpty()) {
        command.setResponse(Coder.getNilResponse(context.getByteBufAllocator()));
      } else if (count == 1) {
        command.setResponse(
            Coder.getBulkStringResponse(context.getByteBufAllocator(),
                results.iterator().next().toBytes()));
      } else {
        command.setResponse(Coder.getArrayResponse(context.getByteBufAllocator(), results));
      }
    } catch (CoderException e) {
      command.setResponse(Coder.getErrorResponse(context.getByteBufAllocator(),
          RedisConstants.SERVER_ERROR_MESSAGE));
    }
  }
}
