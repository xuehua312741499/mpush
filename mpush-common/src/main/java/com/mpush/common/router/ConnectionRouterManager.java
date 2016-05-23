/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.common.router;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Created by ohun on 2016/1/4.
 */
public final class ConnectionRouterManager extends RemoteRouterManager {
    public static final ConnectionRouterManager INSTANCE = new ConnectionRouterManager();
    // TODO: 2015/12/30 可以增加一层本地缓存，防止疯狂查询redis, 但是要注意失效问题及数据不一致问题
    private final Cache<String, RemoteRouter> cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    @Override
    public RemoteRouter lookup(String userId) {
        RemoteRouter cached = cache.getIfPresent(userId);
        if (cached != null) return cached;
        RemoteRouter router = super.lookup(userId);
        if (router != null) {
            cache.put(userId, router);
        }
        return router;
    }

    /**
     * 如果推送失败，可能是缓存不一致了，可以让本地缓存失效
     * <p>
     * 失效对应的本地缓存
     *
     * @param userId
     */
    public void invalidateLocalCache(String userId) {
        cache.invalidate(userId);
    }
}
