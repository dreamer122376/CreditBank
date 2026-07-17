package com.creditbank.mvp.service;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 操作封装服务。
 * 对 RedisTemplate / StringRedisTemplate 进行二次封装，
 * 提供 String、Hash、List、Key 过期等常用操作的便捷方法。
 * 额外注释
 */
@Service
public class RedisService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // ==================== String 操作 ====================

    /** 设置 String 值（无过期时间） */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /** 设置 String 值（带过期时间） */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /** 获取 Object 值 */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /** 获取 String 值（使用 StringRedisTemplate） */
    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /** 设置 String 值（使用 StringRedisTemplate，无过期时间） */
    public void setString(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /** 设置 String 值（使用 StringRedisTemplate，带过期时间） */
    public void setString(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // ==================== Key 操作 ====================

    /** 删除单个 Key */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /** 批量删除 Keys */
    public Long delete(Set<String> keys) {
        return redisTemplate.delete(keys);
    }

    /** 判断 Key 是否存在 */
    public Boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    /** 设置 Key 过期时间 */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /** 获取 Key 剩余过期时间（秒） */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    /** 获取 Key 剩余过期时间（指定单位） */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    // ==================== Hash 操作 ====================

    /** 向 Hash 中存入单个字段 */
    public void hashPut(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /** 向 Hash 中批量存入键值对 */
    public void hashPutAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /** 从 Hash 中获取单个字段 */
    public Object hashGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /** 获取整个 Hash */
    public Map<Object, Object> hashGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /** 判断 Hash 中某个字段是否存在 */
    public Boolean hashExists(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    /** 删除 Hash 中一个或多个字段 */
    public Long hashDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    /** 获取 Hash 的所有字段名 */
    public Set<Object> hashKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /** 获取 Hash 的字段数量 */
    public Long hashSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    // ==================== List 操作 ====================

    /** 从 List 左侧插入 */
    public Long listLeftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /** 从 List 左侧批量插入 */
    public Long listLeftPushAll(String key, Object... values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    /** 从 List 右侧插入 */
    public Long listRightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /** 从 List 右侧批量插入 */
    public Long listRightPushAll(String key, Object... values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    /** 从 List 左侧弹出 */
    public Object listLeftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /** 从 List 右侧弹出 */
    public Object listRightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /** 获取 List 指定索引的元素 */
    public Object listIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /** 获取 List 指定范围元素 */
    public List<Object> listRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /** 获取 List 长度 */
    public Long listSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /** 修改 List 指定索引的值 */
    public void listSet(String key, long index, Object value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /** 从 List 中移除指定数量的元素 */
    public Long listRemove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    // ==================== 计数操作 ====================

    /** 自增指定步长 */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /** 自增 1 */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /** 自减指定步长 */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    /** 自减 1 */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    // ==================== 其他操作 ====================

    /** 按模式匹配 Keys（注意：生产环境慎用，会阻塞 Redis） */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /** 清空当前数据库 */
    public void flushDb() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.flushDb();
            return null;
        });
    }
}
