package com.bdqn.spz.spring.redis.redis;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

public class RedisClient {
	
	private ApplicationContext context=new ClassPathXmlApplicationContext("applicationContext-redis.xml");
	
	private RedisConnectionFactory redisConnectionFactory=context.getBean("jedisConnectionFactory",JedisConnectionFactory.class);
	
	//private RedisConnectionFactory redisConnectionFactory;
	
	private RedisConnection redisConnection = redisConnectionFactory.getConnection();
	
	public static void main(String[] args) {
		System.out.println(new RedisClient().get("name"));
	}

	/**
	 * GET key 杩斿洖key鎵�叧鑱旂殑瀛楃涓插�
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key) {

		String value = null;
		RedisConnection connection = null;
		try {
			connection = redisConnection;
			byte[] valueByte = connection.get(key.getBytes());
			if (valueByte != null) {
				value = new String(valueByte);
			}
		} catch (Exception e) {
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return value;
	}

	/**
	 * SET key value灏嗗瓧绗︿覆鍊紇alue鍏宠仈鍒発ey(瑕嗙洊鏃у�)銆�
	 * 
	 * @param key
	 * @param value
	 */
	public void set(String key, String value) {

		RedisConnection connection = null;
		try {
			connection = redisConnection;
			connection.set(key.getBytes(), value.getBytes());
		} catch (Exception e) {
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	/**
	 * APPEND key
	 * value濡傛灉key宸茬粡瀛樺湪骞朵笖鏄竴涓瓧绗︿覆锛孉PPEND鍛戒护灏唙alue杩藉姞鍒発ey鍘熸潵鐨勫�涔嬪悗銆�
	 * 
	 * @param key
	 * @param value
	 */
	public void append(String key, String value) {

		RedisConnection connection = null;
		try {
			connection = redisConnection;
			connection.append(key.getBytes(), value.getBytes());
		} catch (Exception e) {
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	/**
	 * RENAME key newkey
	 * 灏唊ey鏀瑰悕涓簄ewkey銆傚綋key鍜宯ewkey鐩稿悓鎴栬�key涓嶅瓨鍦ㄦ椂锛岃繑鍥炰竴涓敊璇�褰搉ewkey宸茬粡瀛樺湪鏃讹紝RENAME鍛戒护灏嗚鐩栨棫鍊笺�
	 * 
	 * @param key
	 * @param newKey
	 */
	public void rename(String key, String newKey) {

		RedisConnection connection = null;
		try {
			connection = redisConnection;
			connection.rename(key.getBytes(), newKey.getBytes());
		} catch (Exception e) {
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	/**
	 * TYPE key 杩斿洖key鎵�偍瀛樼殑鍊肩殑绫诲瀷銆�
	 * 
	 * @param key
	 * @return none(key涓嶅瓨鍦�,string(瀛楃涓�,list(鍒楄〃),set(闆嗗悎),zset(鏈夊簭闆�,hash(鍝堝笇琛�
	 */
	public String type(String key) {

		DataType type = null;
		RedisConnection connection = null;
		try {
			connection = redisConnection;
			type = connection.type(key.getBytes());
		} catch (Exception e) {
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return type.toString();
	}

	/**
	 * SETEX key seconds value灏嗗�value鍏宠仈鍒発ey锛屽苟灏唊ey鐨勭敓瀛樻椂闂磋涓簊econds(浠ョ涓哄崟浣�銆�
	 * 
	 * @param key
	 * @param timeOut
	 * @param value
	 */
	public void setEx(String key, long timeOut, String value) {

		RedisConnection connection = null;
		try {
			connection = redisConnection;
			connection.setEx(key.getBytes(), timeOut, value.getBytes());
		} catch (Exception e) {
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	/**
	 * EXPIRE key seconds 涓虹粰瀹歬ey璁剧疆鐢熷瓨鏃堕棿銆傚綋key杩囨湡鏃讹紝瀹冧細琚嚜鍔ㄥ垹闄ゃ�
	 * 
	 * @param key
	 * @param timeOut
	 *            (鍗曚綅:绉�
	 * @return
	 */
	public boolean expire(String key, long timeOut) {

		boolean f = false;
		RedisConnection connection = null;
		try {
			connection = redisConnection;
			f = connection.expire(key.getBytes(), timeOut);
		} catch (Exception e) {
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return f;
	}

	/**
	 * EXPIREAT
	 * EXPIREAT鐨勪綔鐢ㄥ拰EXPIRE涓�牱锛岄兘鐢ㄤ簬涓簁ey璁剧疆鐢熷瓨鏃堕棿銆備笉鍚屽湪浜嶦XPIREAT鍛戒护鎺ュ彈鐨勬椂闂村弬鏁版槸UNIX鏃堕棿鎴�unix
	 * timestamp)
	 * 
	 * @param key
	 * @param timestamp
	 *            (UNIX鏃堕棿鎴�unix timestamp) )
	 * @return
	 */
	public boolean expireAt(String key, long timestamp) {

		boolean f = false;
		RedisConnection connection = null;
		try {
			connection = redisConnection;
			f = connection.expireAt(key.getBytes(), timestamp);
		} catch (Exception e) {
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return f;
	}

	/**
	 * TTL 杩斿洖缁欏畾key鐨勫墿浣欑敓瀛樻椂闂�time to live)(浠ョ涓哄崟浣�
	 * 
	 * @param key
	 * @return
	 */
	public int ttl(String key) {

		Long time = 0L;
		RedisConnection connection = null;
		try {
			connection = redisConnection;
			time = connection.ttl(key.getBytes());
		} catch (Exception e) {
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return time.intValue();
	}

	/**
	 * PERSIST key 绉婚櫎缁欏畾key鐨勭敓瀛樻椂闂淬�
	 * 
	 * @param key
	 * @return
	 */
	public boolean persist(String key) {

		boolean f = false;
		RedisConnection connection = null;
		try {
			connection = redisConnection;
			f = connection.persist(key.getBytes());
		} catch (Exception e) {
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return f;
	}

	/**
	 * EXISTS 妫�煡缁欏畾key鏄惁瀛樺湪銆�
	 * 
	 * @param key
	 * @return
	 */
	public boolean exists(String key) {

		boolean f = false;
		RedisConnection connection = null;
		try {
			connection = redisConnection;
			f = connection.exists(key.getBytes());
		} catch (Exception e) {
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return f;
	}

	/**
	 * DEL 鍒犻櫎缁欏畾key銆�
	 * 
	 * @param key
	 * @return
	 */
	public int del(String key) {

		Long i = 0L;
		RedisConnection connection = null;
		try {
			connection = redisConnection;
			i = connection.del(key.getBytes());
		} catch (Exception e) {
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return i.intValue();
	}

}
