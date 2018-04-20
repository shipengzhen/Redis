/**
 * 
 */
package com.bdqn.spz.redis.test;


import java.util.Set;


import com.bdqn.spz.redis.util.JedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

/**
 * @author 施鹏振 date 2017年12月11日 time 下午12:54:27
 */
public class Test {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO 自动生成的方法存根
		new Test().jedisCluster();
	}
	
	public void jedisCluster(){
		JedisCluster jedisCluster=JedisUtil.getJedisCluster();
		int num = 1000;
		String key = "name";
		String value = "spz";
		System.out.println(jedisCluster.get(key));
		for (int i = 1; i <= num; i++) {
			// 存数据
			jedisCluster.set(key + i, value + i);
			// 取数据
			System.out.println(jedisCluster.get(key + i));
			// 删除数据
			// jedisCluster.del(key+i);
			// value = jedisCluster.get(key+i);
			// log.info(key+i + "=" + value);
		}
	}
	
	@org.junit.Test
	public void jedisPool(){
		JedisPool jedisPool=null;
		Jedis jedis=null;
		try {
			jedisPool=JedisUtil.getJedisPool();
			jedis=jedisPool.getResource();
			System.out.println(jedis.get("name"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			JedisUtil.release(jedisPool, jedis);
		}
	}
	
	@org.junit.Test
	@SuppressWarnings("resource")
	public void api() {
		Jedis jedis = new Jedis("192.168.88.131", 6379);
		// System.out.println(jedis.ping());
		// jedis.set("name","spz");
		// System.out.println(jedis.get("name"));
		jedis.auth("spz");
		Set<String> strings = jedis.keys("*");
		for (String string : strings) {
			System.out.println(string);
		}
		System.out.println(jedis.get("balance1"));
	}

	// 事务
	@org.junit.Test
	@SuppressWarnings("resource")
	public void transaction() {
		Jedis jedis = new Jedis("192.168.88.131", 6379);
		jedis.watch("k1","k2","k3");// 加锁
		try {
			Thread.sleep(7000);
		} catch (InterruptedException e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		}
		Transaction transaction = jedis.multi();// 开启事务
		transaction.set("k1", "v1");
		transaction.set("k2", "v2");
		transaction.set("k3", "v3");
		transaction.exec();// 提交事务
		// transaction.discard();//回滚事务
	}

	/**
	 * 通俗点讲，watch命令就是标记一个键，如果标记了一个键 在提交事务前如果改建被别人修改过，那么事务就会失败，这种情况通常可以在程序中
	 * 重新在尝试一次 用户1转账给用户2
	 */
	@org.junit.Test
	public void watch() {
		Jedis jedis = new Jedis("192.168.88.131", 6379);
		jedis.set("balance1", "100");// 用户1预存款100
		jedis.set("balance2", "100");// 用户2预存款100
		jedis.set("debt", "0");// 债务0
		Integer amtToSubtract = 110;// 实刷额度
		System.out.println("转帐前***************************");
		System.out.println("用户1余额-->" + jedis.get("balance1"));
		System.out.println("用户2余额-->" + jedis.get("balance2"));
		System.out.println("用户1欠额-->" + jedis.get("debt"));
		System.out.println("转帐中***************************");
		System.out.println("转账额度-->" + amtToSubtract);
		if (transMethod(jedis, amtToSubtract)) {
			System.out.println("转帐后***************************");
			System.out.println("用户1余额-->" + jedis.get("balance1"));
			System.out.println("用户2余额-->" + jedis.get("balance2"));
			System.out.println("用户1欠额-->" + jedis.get("debt"));
			System.out.println("成功");
		}else {
			System.out.println("有人在操作,失败,请重试");
		}
	}

	// 交易
	public boolean transMethod(Jedis jedis, Integer amtToSubtract) {
		boolean b = false;
		Integer balance1 = Integer.parseInt(jedis.get("balance1"));
		Integer balance2 = Integer.parseInt(jedis.get("balance1"));
		Integer debt = Integer.parseInt(jedis.get("debt"));
		jedis.watch("balance1","balance2","debt");// 加锁
		try {
			Thread.sleep(7000);
		} catch (InterruptedException e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		}
		Transaction transaction = jedis.multi();
		try {
			if (balance1 < amtToSubtract) {
				transaction.set("balance1", "0");// 用户1余额设置为0
				transaction.incrBy("balance2", amtToSubtract);// 用户2余额增加
				amtToSubtract = amtToSubtract - balance1;
				transaction.incrBy("debt", amtToSubtract);// 用户1欠额增加
			} else {
				transaction.decrBy("balance1", 10);// 用户1余额减少
				transaction.incrBy("balance2", 10);// 用户2余额增加
			}
			transaction.exec();
			Integer balance1_1 = Integer.parseInt(jedis.get("balance1"));
			Integer balance2_2 = Integer.parseInt(jedis.get("balance1"));
			Integer debt_2 = Integer.parseInt(jedis.get("debt"));
			if (balance1!=balance1_1||balance2!=balance2_2||debt!=debt_2) {
				b = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			transaction.discard();// 回滚事务
			b = false;
		}
		return b;
	}
}