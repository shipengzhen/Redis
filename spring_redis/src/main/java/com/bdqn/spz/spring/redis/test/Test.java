package com.bdqn.spz.spring.redis.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bdqn.spz.spring.redis.redis.ShardedJedisClient;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

public class Test {
	
	public static void main(String[] args) {
		
//		ApplicationContext context=new ClassPathXmlApplicationContext("applicationContext-redis.xml");
//		
//		RedisTemplate redisTemplate=context.getBean("redisTemplate",RedisTemplate.class);
//		
//		redisTemplate.execute(new RedisCallback<String>() {
//			@Override
//			public String doInRedis(RedisConnection connection) throws DataAccessException {
//				System.out.println(connection.get("name".getBytes()).toString());
//				return null;
//			}
//		});
	    ShardedJedisClient shardedJedisClient=new ShardedJedisClient();
	    
	    System.out.println(shardedJedisClient.set("name","spz11"));
	    
	    @SuppressWarnings("resource")
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-redis.xml");
	    
	    JedisCluster jedisCluster = context.getBean("jedisCluster", JedisCluster.class);
	    System.out.println(jedisCluster.get("name"));
	    
	    JedisPool pool = context.getBean("jedisPool", JedisPool.class);
        Jedis jedis = pool.getResource();
        System.out.println(jedis.set("name","spz22"));
        System.out.println(jedis.get("name"));
		
	}
}
