package com.bdqn.spz.spring.redis.test;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bdqn.spz.spring.redis.redis.RedisClient;
import com.bdqn.spz.spring.redis.redis.ShardedJedisClient;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

public class Test {

    public void ShardedJedisClientTest() {
        ShardedJedisClient shardedJedisClient = new ShardedJedisClient();
       String key="spz";
       shardedJedisClient.set(key,"1",600);
       System.out.println(shardedJedisClient.ttl(key));
       shardedJedisClient.incr(key);
       System.out.println(shardedJedisClient.ttl(key));
    }

    public void JedisPoolTest() {
        @SuppressWarnings("resource")
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-redis.xml");
        JedisPool pool = context.getBean("jedisPool", JedisPool.class);
        Jedis jedis = pool.getResource();
        for (int i = 0; i < 100; i++) {
            System.out.println(jedis.set("name" + i, "spz" + i));
            System.out.println(jedis.get("name" + i));
        }
    }

    public void JedisClusterTest() throws IOException {
        @SuppressWarnings("resource")
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-redis.xml");
        JedisCluster jedisCluster = null;
        try {
            jedisCluster = context.getBean("jedisCluster", JedisCluster.class);
            for (int i = 0; i < 100; i++) {
                System.out.println(jedisCluster.set("name" + i, "spz" + i));
                System.out.println(jedisCluster.get("name" + i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != jedisCluster) {
                jedisCluster.close();
            }
        }
    }

    public void redisClusterConfigurationTest() {
        RedisClient redisClient = new RedisClient();
        for (int i = 0; i < 100; i++) {
            redisClient.set("name" + i, "spz" + i);
            System.out.println(redisClient.get("name" + i));
            redisClient.del("name" + i);
        }
    }

    public static void main(String[] args) throws IOException {

        new Test().ShardedJedisClientTest();
    }
}
