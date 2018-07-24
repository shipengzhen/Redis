/**
 * 
 */
package com.bdqn.spz.redis.util;

import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * @author 施鹏振 date 2017年12月12日 time 下午2:55:24
 */
public class JedisUtils {

    private static JedisPoolConfig jedisPoolConfig;
    private static volatile JedisPool jedisPool;// valatile 不稳定的
    private static JedisCluster jedisCluster;

    private JedisUtils() {
    }

    // 数据库链接池配置
    public static JedisPoolConfig getJedisPoolConfig() {
        if (jedisPoolConfig == null) {
            synchronized (JedisUtils.class) {
                if (jedisPoolConfig == null) {
                    jedisPoolConfig = new JedisPoolConfig();
                    jedisPoolConfig.setMaxTotal(1000);// 最大连接总数，-1表示不限制
                    jedisPoolConfig.setMaxIdle(50);// 控制一个pool最多有多少个状态为idle(空闲)的jedis实例
                    jedisPoolConfig.setMinIdle(20); // 控制一个pool最少有多少个状态为idle(空闲)的jedis实例
                    jedisPoolConfig.setMaxWaitMillis(6 * 1000);// 表示当borrow一个jedis实例时，最大的等待时间，如果等待超时，直接抛出JedisConnectionException
                    jedisPoolConfig.setTestOnBorrow(true);// 获得一个jedis实例的时候是否检查练级接可用性(ping());如果为true，则得到的jedis实例均是可用的
                    // jedisPoolConfig.setTestOnReturn(true);//return一个jedis实例给pool时，是否检查可用性(ping())
                }
            }
        }
        return jedisPoolConfig;
    }

    // 连接池
    public static JedisPool getJedisPool() {
        if (jedisPool == null) {
            synchronized (JedisUtils.class) {
                if (jedisPool == null) {
                    jedisPool = new JedisPool(getJedisPoolConfig(), "192.168.88.131", 6379, 1000, "spz");
                }
            }
        }
        return jedisPool;
    }

    // 集群
    public static JedisCluster getJedisCluster() {

        if (jedisCluster == null) {
            synchronized (JedisUtils.class) {
                if (jedisCluster == null) {
                    // Redis集群的节点集合
                    Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
                    jedisClusterNodes.add(new HostAndPort("192.168.88.131", 1000));
                    jedisClusterNodes.add(new HostAndPort("192.168.88.131", 1001));
                    jedisClusterNodes.add(new HostAndPort("192.168.88.131", 2000));
                    jedisClusterNodes.add(new HostAndPort("192.168.88.131", 2001));
                    jedisClusterNodes.add(new HostAndPort("192.168.88.131", 3000));
                    jedisClusterNodes.add(new HostAndPort("192.168.88.131", 3001));
                    // 根据节点集创集群链接对象
                    // JedisCluster jedisCluster = new
                    // JedisCluster(jedisClusterNodes);
                    // 集群各节点集合，超时时间，最多重定向次数，链接池
                    jedisCluster = new JedisCluster(jedisClusterNodes, 1000, 1000, 10, "spz", getJedisPoolConfig());
                }
            }
        }
        return jedisCluster;
    }

    // 关闭释放
    public static void release(JedisPool jedisPool, Jedis jedis) {
        if (jedis != null) {
            // jedisPool.returnResourceObject(jedis);
            jedis.close();
        }
    }
}
