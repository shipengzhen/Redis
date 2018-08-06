package com.bdqn.spz.spring.redis.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.alibaba.fastjson.JSON;
import com.bdqn.spz.spring.redis.pojo.Student;
import com.bdqn.spz.spring.redis.util.CommonUtil;

public class RedisClient {

    private static ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-redis.xml");

    private static RedisConnectionFactory redisConnectionFactory = context.getBean("jedisConnectionFactory",
            JedisConnectionFactory.class);

    @SuppressWarnings("unchecked")
    private static RedisTemplate<String, Object> redisTemplate = context.getBean("redisTemplate", RedisTemplate.class);

    private static StringRedisTemplate stringRedisTemplate = context.getBean("stringRedisTemplate",
            StringRedisTemplate.class);

    private static RedisConnection redisConnection = redisConnectionFactory.getConnection();

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        List<Student> students = new ArrayList<Student>();

        String key = "student";
        Student student = new Student();
        student.setName("施鹏振");
        student.setAge(20);
        student.setSex("男");

        students.add(student);

        if (redisConnection.exists(key.getBytes())) {
            stringRedisTemplate.delete(key);
        }

        ValueOperations<String, String> valueOperString = stringRedisTemplate.opsForValue();
        valueOperString.set(key, JSON.toJSONString(students), 300, TimeUnit.SECONDS);
        System.out.println(valueOperString.get(key));

        if (redisConnection.exists(key.getBytes())) {
            stringRedisTemplate.delete(key);
        }

        ValueOperations<String, Object> valueOper = redisTemplate.opsForValue();
        valueOper.set(key, students, 300, TimeUnit.SECONDS);
        //student = (Student) valueOper.get(key);
        //System.out.println(student2.getName());

        students = (List<Student>) valueOper.get(key);

        for (Student student2 : students) {
            System.out.println(student2.getName());
        }
    }

    /**
     * @功能描述：获取String
     * @参数说明：@param key
     * @参数说明：@return
     * @作者： shipengzhen
     * @创建时间：2018年7月24日 上午11:27:15
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
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return value;
    }

    /**
     * @功能描述：获取对象
     * @参数说明：@param key
     * @参数说明：@return
     * @作者： shipengzhen
     * @创建时间：2018年7月24日 上午11:23:54
     */
    public static Object getObject(String key) {
        Object value = null;
        RedisConnection connection = null;
        try {
            connection = redisConnection;
            byte[] valueByte = connection.get(key.getBytes());
            if (valueByte != null) {
                value = CommonUtil.unserialize(valueByte);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return value;
    }

    /**
     * @功能描述：写入String
     * @参数说明：@param key
     * @参数说明：@param value
     * @作者： shipengzhen
     * @创建时间：2018年7月24日 上午11:28:01
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
