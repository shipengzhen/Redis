/**
 * 
 */
package com.bdqn.spz.spring.redis.redis;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

/**
 * @author 施鹏振 date 2017年12月13日 time 下午1:02:18
 */
public class JedisClient {

	private ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-redis.xml");
	
	private Log log=LogFactory.getLog(JedisClient.class);
	//private Logger logger=Logger.getLogger(JedisClient.class);

	@Test
	public void testJedisShardInfo() {
		JedisShardInfo jedisShardInfo = context.getBean("jedisShardInfo1", JedisShardInfo.class);
		Jedis jedis = jedisShardInfo.createResource();
		System.out.println(jedis.get("name"));
	}

	@Test
	public void testshardedJedisPool() {
		ShardedJedisPool shardedJedisPool = context.getBean("shardedJedisPool", ShardedJedisPool.class);
		ShardedJedis jedis = shardedJedisPool.getResource();
		System.out.println(jedis.get("name"));
	}

	@Test
	public void testJedisPool() {
		JedisPool pool = context.getBean("jedisPool", JedisPool.class);
		Jedis jedis = pool.getResource();
		System.out.println(jedis.get("name"));
	}
	
	@Test
	public void testJedisCluster() {
		try {
			JedisCluster jedisCluster = context.getBean("jedisCluster", JedisCluster.class);
			int num = 1000;
			String key = "name";
			String value =null;
			int count = 1;
			while (true) {
				for (int i = 1; i <= num; i++) {
					try {
						// 存数据
						jedisCluster.set(key+i,"spz"+i);
						// 取数据
						value = jedisCluster.get(key + i);
						System.out.println(key + i + "=" + value);
						log.info(key + i + "=" + value);
						if (value == null || "".equals(value)) {
							log.error("===>break" + key + i + " value is null");
							break;
						}
					} catch (Exception e) {
						log.error("====>", e);
						Thread.sleep(3000);
						continue;
					}
					// 删除数据
					// jedisCluster.del(key+i);
					// value = jedisCluster.get(key+i);
					// log.info(key+i + "=" + value);
				}
				log.info("===================================>count:" + count);
				if (value == null || "".equals(value)) {
					break;
				}
				count++;
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			log.error("====>", e);
		}

	}

	public static void main(String[] args) {
	    List<JedisShardInfo> shards = Arrays.asList(
                new JedisShardInfo("127.0.0.1", 6379)
                //new JedisShardInfo("192.168.104.15", 6379)
        );
        @SuppressWarnings("resource")
        ShardedJedis shardedJedis = new ShardedJedis(shards);
        ShardedJedisPipeline shardedJedisPipeline = shardedJedis.pipelined();
        for (int i = 0; i < 10; i++) {
            shardedJedisPipeline.set("k" + i, "v" + i);
        }
        shardedJedisPipeline.sync();
	}
}
