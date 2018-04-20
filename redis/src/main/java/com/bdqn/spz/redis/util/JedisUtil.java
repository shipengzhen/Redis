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
 * @author ʩ���� date 2017��12��12�� time ����2:55:24
 */
public class JedisUtil {

	private static JedisPoolConfig jedisPoolConfig;
	private static volatile JedisPool jedisPool;// valatile ���ȶ���
	private static JedisCluster jedisCluster;

	private JedisUtil() {
	}

	// ���ݿ����ӳ�����
	public static JedisPoolConfig getJedisPoolConfig() {
		if (jedisPoolConfig == null) {
			synchronized (JedisUtil.class) {
				if (jedisPoolConfig == null) {
					jedisPoolConfig = new JedisPoolConfig();
					jedisPoolConfig.setMaxTotal(1000);// �������������-1��ʾ������
					jedisPoolConfig.setMaxIdle(50);// ����һ��pool����ж��ٸ�״̬Ϊidle(����)��jedisʵ��
					jedisPoolConfig.setMinIdle(20); // ����һ��pool�����ж��ٸ�״̬Ϊidle(����)��jedisʵ��
					jedisPoolConfig.setMaxWaitMillis(6 * 1000);// ��ʾ��borrowһ��jedisʵ��ʱ�����ĵȴ�ʱ�䣬����ȴ���ʱ��ֱ���׳�JedisConnectionException
					jedisPoolConfig.setTestOnBorrow(true);// ���һ��jedisʵ����ʱ���Ƿ��������ӿ�����(ping());���Ϊtrue����õ���jedisʵ�����ǿ��õ�
					// jedisPoolConfig.setTestOnReturn(true);//returnһ��jedisʵ����poolʱ���Ƿ��������(ping())
				}
			}
		}
		return jedisPoolConfig;
	}

	// ���ӳ�
	public static JedisPool getJedisPool() {
		if (jedisPool == null) {
			synchronized (JedisUtil.class) {
				if (jedisPool == null) {
					jedisPool = new JedisPool(getJedisPoolConfig(), "192.168.88.131", 6379, 1000, "spz");
				}
			}
		}
		return jedisPool;
	}

	// ��Ⱥ
	public static JedisCluster getJedisCluster() {

		if (jedisCluster == null) {
			synchronized (JedisUtil.class) {
				if (jedisCluster == null) {
					// Redis��Ⱥ�Ľڵ㼯��
					Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
					jedisClusterNodes.add(new HostAndPort("192.168.88.131", 1000));
					jedisClusterNodes.add(new HostAndPort("192.168.88.131", 1001));
					jedisClusterNodes.add(new HostAndPort("192.168.88.131", 2000));
					jedisClusterNodes.add(new HostAndPort("192.168.88.131", 2001));
					jedisClusterNodes.add(new HostAndPort("192.168.88.131", 3000));
					jedisClusterNodes.add(new HostAndPort("192.168.88.131", 3001));
					// ���ݽڵ㼯����Ⱥ���Ӷ���
					// JedisCluster jedisCluster = new
					// JedisCluster(jedisClusterNodes);
					// ��Ⱥ���ڵ㼯�ϣ���ʱʱ�䣬����ض�����������ӳ�
					jedisCluster = new JedisCluster(jedisClusterNodes, 1000, 1000, 10, "spz", getJedisPoolConfig());
				}
			}
		}
		return jedisCluster;
	}

	// �ر��ͷ�
	public static void release(JedisPool jedisPool, Jedis jedis) {
		if (jedis != null) {
			// jedisPool.returnResourceObject(jedis);
			jedis.close();
		}
	}
}
