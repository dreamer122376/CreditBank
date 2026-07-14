package com.creditbank.mvp;

import com.creditbank.mvp.service.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisConnectionTest {

    @Autowired
    private RedisService redisService;

    @Test
    public void testConnection() {
        String testKey = "test:connection";
        String testValue = "Hello Redis!";

        redisService.setString(testKey, testValue);
        String result = redisService.getString(testKey);

        System.out.println("=== Redis 连接测试 ===");
        System.out.println("写入值: " + testValue);
        System.out.println("读取值: " + result);

        if (testValue.equals(result)) {
            System.out.println("✓ Redis 连接成功！");
        } else {
            System.out.println("✗ Redis 连接失败！");
        }

        redisService.delete(testKey);
    }

    @Test
    public void testStringOperations() {
        System.out.println("\n=== 字符串操作测试 ===");

        String key1 = "test:string:1";
        String key2 = "test:string:2";

        redisService.setString(key1, "value1");
        System.out.println("设置字符串 key1: " + redisService.getString(key1));

        redisService.setString(key2, "value2", 5, TimeUnit.SECONDS);
        System.out.println("设置带过期时间的字符串 key2: " + redisService.getString(key2));
        System.out.println("key2 剩余过期时间(秒): " + redisService.getExpire(key2, TimeUnit.SECONDS));

        redisService.delete(key1);
        redisService.delete(key2);
        System.out.println("✓ 字符串操作测试完成");
    }

    @Test
    public void testHashOperations() {
        System.out.println("\n=== 哈希操作测试 ===");

        String hashKey = "test:hash:user";

        redisService.hashPut(hashKey, "name", "张三");
        redisService.hashPut(hashKey, "age", 25);
        redisService.hashPut(hashKey, "email", "zhangsan@example.com");

        System.out.println("获取单个字段 name: " + redisService.hashGet(hashKey, "name"));
        System.out.println("获取单个字段 age: " + redisService.hashGet(hashKey, "age"));

        System.out.println("获取所有字段: " + redisService.hashGetAll(hashKey));
        System.out.println("判断字段是否存在 name: " + redisService.hashExists(hashKey, "name"));
        System.out.println("判断字段是否存在 address: " + redisService.hashExists(hashKey, "address"));
        System.out.println("获取所有字段名: " + redisService.hashKeys(hashKey));
        System.out.println("哈希表大小: " + redisService.hashSize(hashKey));

        redisService.hashDelete(hashKey, "email");
        System.out.println("删除 email 字段后: " + redisService.hashGetAll(hashKey));

        redisService.delete(hashKey);
        System.out.println("✓ 哈希操作测试完成");
    }

    @Test
    public void testListOperations() {
        System.out.println("\n=== 列表操作测试 ===");

        String listKey = "test:list";

        redisService.listRightPush(listKey, "A");
        redisService.listRightPush(listKey, "B");
        redisService.listRightPush(listKey, "C");
        redisService.listLeftPush(listKey, "0");

        System.out.println("列表大小: " + redisService.listSize(listKey));
        System.out.println("列表所有元素: " + redisService.listRange(listKey, 0, -1));
        System.out.println("索引为2的元素: " + redisService.listIndex(listKey, 2));

        System.out.println("左弹出: " + redisService.listLeftPop(listKey));
        System.out.println("右弹出: " + redisService.listRightPop(listKey));
        System.out.println("弹出后的列表: " + redisService.listRange(listKey, 0, -1));

        redisService.delete(listKey);
        System.out.println("✓ 列表操作测试完成");
    }

    @Test
    public void testIncrementDecrement() {
        System.out.println("\n=== 增减操作测试 ===");

        String counterKey = "test:counter";

        redisService.set(counterKey, 0);
        redisService.increment(counterKey);
        redisService.increment(counterKey);
        redisService.increment(counterKey, 5);
        System.out.println("增加后的值: " + redisService.get(counterKey));

        redisService.decrement(counterKey);
        redisService.decrement(counterKey, 3);
        System.out.println("减少后的值: " + redisService.get(counterKey));

        redisService.delete(counterKey);
        System.out.println("✓ 增减操作测试完成");
    }

    @Test
    public void testExpire() {
        System.out.println("\n=== 过期时间测试 ===");

        String expireKey = "test:expire";

        redisService.setString(expireKey, "will expire", 3, TimeUnit.SECONDS);
        System.out.println("设置3秒过期，当前值: " + redisService.getString(expireKey));
        System.out.println("剩余过期时间(秒): " + redisService.getExpire(expireKey, TimeUnit.SECONDS));

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("4秒后的值: " + redisService.getString(expireKey));
        System.out.println("key是否存在: " + redisService.exists(expireKey));

        System.out.println("✓ 过期时间测试完成");
    }

    @Test
    public void testObjectSerialization() {
        System.out.println("\n=== 对象序列化测试 ===");

        String objKey = "test:object";

        Map<String, Object> user = new HashMap<>();
        user.put("id", 1L);
        user.put("username", "testuser");
        user.put("active", true);
        user.put("score", 95.5);

        redisService.set(objKey, user);
        Object result = redisService.get(objKey);

        System.out.println("写入对象: " + user);
        System.out.println("读取对象: " + result);

        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> resultMap = (Map<String, Object>) result;
            if ("testuser".equals(resultMap.get("username"))) {
                System.out.println("✓ 对象序列化成功！");
            } else {
                System.out.println("✗ 对象序列化失败！");
            }
        }

        redisService.delete(objKey);
        System.out.println("✓ 对象序列化测试完成");
    }
}
