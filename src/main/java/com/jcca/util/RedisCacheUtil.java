package com.jcca.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * spring redis 工具类
 *
 * @author xyx
 **/
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Slf4j
@Component
public class RedisCacheUtil {
    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    public StringRedisTemplate stringRedisTemplate;

    /**
     * @param key   缓存的键值
     * @param value 缓存的值
     * @return 缓存的对象
     */
    public void setStrV(String key, String value) {
        ValueOperations<String, String> operation = redisTemplate.opsForValue();
        operation.set(key, value);
    }

    /**
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     * @return 缓存的对象
     */
    public void setStrV(String key, String value, Integer timeout, TimeUnit timeUnit) {
        ValueOperations<String, String> operation = redisTemplate.opsForValue();
        operation.set(key, value, timeout, timeUnit);
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public String getStrV(String key) {
        //long time=System.currentTimeMillis();
        ValueOperations<String, String> operation = redisTemplate.opsForValue();
        String v = operation.get(key);
        //log.info(MyDateUtil.execTime("redis读取时间:",time));
        return v;
    }


    /**
     * @param key   缓存的键值
     * @param value 缓存的值
     * @return 缓存的对象
     */
    public void setObjV(String key, Object value) {
        ValueOperations<String, String> operation = redisTemplate.opsForValue();
        operation.set(key, JSON.toJSONString(value));
    }

    public void setObjV(String key, Object value, Integer timeout, TimeUnit timeUnit) {
        ValueOperations<String, String> operation = redisTemplate.opsForValue();
        operation.set(key, JSON.toJSONString(value), timeout, timeUnit);
    }


    /**
     * 删除单个对象
     *
     * @param key
     */
    public void deleteByKey(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 根据前缀删除
     * // 当redis数据量大时不能再用次方法,可以SCAN +pipeline 来操作
     */

    public void deleteByPrefix(String key) {
        Set<String> keys = redisTemplate.keys(key + "*");
        redisTemplate.delete(keys);
    }


    /**
     * 设置key的过期时间
     */

    public void expireTime(String key, Long time, TimeUnit timeUnit) {
        redisTemplate.expire(key, time, timeUnit);
    }

    ;

    /**
     * 入队
     */
    public void rightPush(String key, Map<String, Object> data) {
        redisTemplate.opsForList().rightPush(key, JSON.toJSONString(data));
    }

    /**
     * 阻塞出队
     */
    public Map<String, Object> bRPop(String key) {

        List<Object> results = redisTemplate.executePipelined(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
                stringRedisConn.bRPop(0, key);
                return null;
            }
        });

        if (CollUtil.isEmpty(results)) {
            return null;
        } else {
            return results.stream().filter(obj -> obj != null).map(obj -> JSONObject.parseObject(obj.toString(), Map.class)).findFirst().orElse(null);
        }

    }

    /**
     * 自增
     *
     * @param key
     * @param delta 默认为null,表示加1
     * @return
     */

    public Long incre(String key, Long delta) {
        if (delta == null) {
            return stringRedisTemplate.opsForValue().increment(key);
        } else {
            return stringRedisTemplate.opsForValue().increment(key, delta);
        }
    }

    /**
     * SADD ,向set集合添加元素
     */

    public Long sAdd(String key, String... member) {
        if (StrUtil.isNotEmpty(key) && ArrayUtil.isNotEmpty(member)) {
            return redisTemplate.opsForSet().add(key, member);
        }
        return -1L;
    }

    /***
     *SREM 移除集合中一个成员
     */
    public Long SREM(String key, String member) {
        if (StrUtil.isNotEmpty(key) && StrUtil.isNotEmpty(member)) {
            return redisTemplate.opsForSet().remove(key, member);
        }
        return -1L;
    }

    /***
     *SMEMBERS 获取集合成员
     */
    public Set SMembers(String key) {
        if (StrUtil.isNotEmpty(key)) {
            return redisTemplate.opsForSet().members(key);
        }
        return null;
    }

    /**
     * 哈希 添加
     *
     * @param key
     * @param hashKey
     * @param value
     */
    public void hSet(String key, Object hashKey, Object value) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        hash.put(key, hashKey, value);
    }

    /**
     * 哈希获取数据
     *
     * @param key
     * @param hashKey
     * @return
     */
    public Object hGet(String key, Object hashKey) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        return hash.get(key, hashKey);
    }

    /**
     * 哈希删除数据
     *
     * @param key
     * @param hashKey
     * @return
     */
    public Object hDel(String key, Object hashKey) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        return hash.delete(key, hashKey);
    }

    /**
     * 获取hash所有 key v
     *
     * @param key
     * @return
     */
    public List hGetAll(String key) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        return hash.values(key);
    }


    public boolean existsKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * SETNX
     *
     * @param key
     * @param value
     * @param expire 单位秒
     * @return
     */
    public boolean setNx(String key, String value, int expire) {
        if (redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(expire))) {
            return true;
        }
        return false;
    }


}
