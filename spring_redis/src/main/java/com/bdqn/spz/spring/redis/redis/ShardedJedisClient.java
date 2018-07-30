package com.bdqn.spz.spring.redis.redis;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
//import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.bdqn.spz.spring.redis.util.CommonUtil;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.exceptions.JedisException;

public class ShardedJedisClient {

    private static final Logger logger = Logger.getLogger(ShardedJedisClient.class);

    private ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-redis.xml");

    private ShardedJedisPool shardedJedisPool = context.getBean("shardedJedisPool", ShardedJedisPool.class);

    /**
     * @功能描述：获取ShardedJedis对象
     * @参数说明：@return
     * @作者： shipengzhen
     * @创建时间：2018年7月25日 下午2:12:22
     */
    private ShardedJedis getResource() {
        ShardedJedis shardedJedis = null;
        try {
            ShardedJedisPool shardedJedisPool=this.shardedJedisPool;
            if (null == shardedJedisPool) {
                throw new JedisException("shardedJedisPool 为  null");
            } else {
                shardedJedis = shardedJedisPool.getResource();
                logger.info("创建ShardedJedis实例成功------------------>");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("创建ShardedJedis实例失败------------------>");
        }
        return shardedJedis;
    }

    /**
     * @功能描述：将字符串值 value 关联到 key 。 如果 key 已经持有其他值， SET 就覆写旧值，无视类型。设置成功返回true,反之false
     * @参数说明：@param key
     * @参数说明：@param value
     * @参数说明：@return
     * @作者： shipengzhen
     * @创建时间：2018年7月29日 下午12:26:20
     */
    public boolean set(String key, String value) {
        ShardedJedis shardedJedis = null;
        boolean status = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                String statusCode = shardedJedis.set(key, value);
                if (null != statusCode) {
                    if ("OK".equals(statusCode)) {
                        status = true;
                    }
                }
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return status;
    }

    /**
     * @功能描述：设置成功返回true,反之false
     * @参数说明：@param key
     * @参数说明：@param value
     * @参数说明：@param seconds
     * @参数说明：@return
     * @作者： shipengzhen
     * @创建时间：2018年7月29日 下午12:19:34
     */
    public boolean set(String key, String value, int seconds) {
        ShardedJedis shardedJedis = null;
        boolean status = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                if (this.set(key, value)) {
                    Long statusCodeLong = this.expire(key, seconds);
                    if (null != statusCodeLong) {
                        if (1 == statusCodeLong) {
                            status = true;
                        }
                    }
                }
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return status;
    }

    /**
     * @功能描述：将字符串值 value 关联到 key 。 如果 key 已经持有其他值， SET 就覆写旧值，无视类型。设置成功返回true,反之false
     * @参数说明：@param key
     * @参数说明：@param value
     * @参数说明：@return
     * @作者： shipengzhen
     * @创建时间：2018年7月29日 下午12:32:57
     */
    public boolean set(String key, Object value) {
        ShardedJedis shardedJedis = null;
        byte[] valueBytes = null;
        boolean status = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                valueBytes = CommonUtil.serialize(value);
                String statusCode = shardedJedis.set(key.getBytes(), valueBytes);
                if (null != statusCode) {
                    if ("OK".equals(statusCode)) {
                        status = true;
                    }
                }
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return status;
    }

    /**
     * @功能描述：设置成功返回true,反之false
     * @参数说明：@param key
     * @参数说明：@param value
     * @参数说明：@param seconds
     * @参数说明：@return
     * @作者： shipengzhen
     * @创建时间：2018年7月29日 下午12:19:34
     */
    public boolean set(String key, Object value, int seconds) {
        ShardedJedis shardedJedis = null;
        boolean status = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                if (this.set(key, value)) {
                    Long statusCodeLong = this.expire(key, seconds);
                    if (null != statusCodeLong) {
                        if (1 == statusCodeLong) {
                            status = true;
                        }
                    }
                }
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return status;
    }

    /**
     * 返回 key 所关联的字符串值。
     * 
     * @param key
     *            关键字
     * @return 当 key 不存在时，返回 nil ，否则，返回 key 的值。 如果 key 不是字符串类型，那么返回一个错误。 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public String get(String key) {
        ShardedJedis shardedJedis = null;
        String value = null;

        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                value = shardedJedis.get(key);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }

        return value;
    }

    /**
     * 返回 key 所关联的Object值。 <br>
     * 获取之后需转换成对应的数据类型
     * 
     * @param key
     *            关键字
     * @return Object值。当 key 不存在时，返回null . <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public Object get(byte[] key) {
        ShardedJedis shardedJedis = null;
        byte[] value = null;

        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                value = shardedJedis.get(key);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        Object obj = null;
        if (value != null) {
            obj = CommonUtil.unserialize(value);
        }
        return obj;
    }

    /**
     * @功能描述：返回Object
     * @参数说明：@param key
     * @参数说明：@return
     * @作者： shipengzhen
     * @创建时间：2018年7月25日 下午3:24:05
     */
    public Object getObject(String key) {
        return this.get(key.getBytes());
    }

    /**
     * 删除给定的 key 。 <br>
     * 不存在的 key 会被忽略。
     * 
     * @param key
     *            关键字
     * @return 被删除 key 的数量。 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     * 
     */
    public Long del(String key) {
        ShardedJedis shardedJedis = null;
        Long quantity = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                quantity = shardedJedis.del(key);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return quantity;
    }

    /**
     * 检查给定 key 是否存在。
     * 
     * @param key
     *            关键字
     * @return 布尔值 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public Boolean exists(String key) {
        ShardedJedis shardedJedis = null;
        boolean flag = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                flag = shardedJedis.exists(key);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return flag;
    }

    /**
     * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
     * 
     * @param key
     *            关键字
     * @param seconds
     *            秒数
     * @return 设置成功返回 1 。 <br>
     *         当 key 不存在或者不能为 key 设置生存时间时(比如在低于 2.1.3 版本的 Redis 中你尝试更新 key
     *         的生存时间)，返回 0 。 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public Long expire(String key, int seconds) {
        ShardedJedis shardedJedis = null;
        Long resultCode = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                resultCode = shardedJedis.expire(key, seconds);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return resultCode;
    }

    /**
     * 返回键值从小到大排序的结果。
     * 
     * @param key
     *            关键字
     * @return 排序后的结果 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public List<String> sort(String key) {
        ShardedJedis shardedJedis = null;
        List<String> result = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                result = shardedJedis.sort(key);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return result;
    }

    /**
     * 返回或保存给定列表、集合、有序集合 key 中经过排序的元素。 <br>
     * 格式：SORT key [BY pattern] [LIMIT offset count] [GET pattern [GET pattern
     * ...]] [ASC | DESC] [ALPHA] [STORE destination]
     * 
     * @param key
     *            关键之
     * @param sortingParameters
     *            排序参数
     * @return 排序后的结果 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public List<String> sort(String key, SortingParams sortingParameters) {
        ShardedJedis shardedJedis = null;
        List<String> result = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                result = shardedJedis.sort(key, sortingParameters);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return result;
    }

    /**
     * 返回 key 所储存的值的类型。
     * 
     * @param key
     *            关键字
     * @return none (key不存在)。 <br>
     *         string (字符串)。 <br>
     *         list (列表)。 <br>
     *         set (集合)。 <br>
     *         zset (有序集)。 <br>
     *         hash (哈希表)。 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public String type(String key) {
        ShardedJedis shardedJedis = null;
        String type = null;

        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                type = shardedJedis.type(key);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return type;
    }

    /**
     * 返回列表 key 中，下标为 index 的元素。 <br>
     * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。
     * <br>
     * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。 <br>
     * 如果 key 不是列表类型，返回一个错误。
     * 
     * @param key
     *            关键字
     * @param index
     *            下标
     * @return 列表中下标为 index 的元素。 <br>
     *         如果 index 参数的值不在列表的区间范围内(out of range)，返回 nil 。 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public String lIndex(String key, long index) {
        ShardedJedis shardedJedis = null;
        String element = null;

        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                element = shardedJedis.lindex(key, index);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return element;
    }

    /**
     * 返回列表 key 的长度。 <br>
     * 如果 key 不存在，则 key 被解释为一个空列表，返回 0 . <br>
     * 如果 key 不是列表类型，返回一个错误。
     * 
     * @param key
     *            关键字
     * @return 列表 key 的长度。 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public Long lLen(String key) {
        ShardedJedis shardedJedis = null;
        Long len = null;

        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                len = shardedJedis.llen(key);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return len;
    }

    /**
     * 移除并返回列表 key 的头元素。
     * 
     * @param key
     *            关键字
     * @return 列表的头元素。当 key 不存在时，返回 nil 。 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public String lPop(String key) {
        ShardedJedis shardedJedis = null;
        String element = null;

        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                element = shardedJedis.lpop(key);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }

        return element;
    }

    /**
     * 将一个或多个值 value 插入到列表 key 的表头 <br>
     * 如果有多个 value 值，那么各个 value 值按从左到右的顺序依次插入到表头 <br>
     * 如果 key 不存在，一个空列表会被创建并执行 LPUSH 操作。 <br>
     * 当 key 存在但不是列表类型时，返回一个错误。
     * 
     * @param key关键字
     * @param strings值
     * @return 执行 LPUSH 命令后，列表的长度。 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public Long lPush(String key, String... strings) {
        ShardedJedis shardedJedis = null;
        Long size = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                size = shardedJedis.lpush(key, strings);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return size;
    }

    /**
     * 将一个object对象插入到列表 key 的表头
     * 
     * @param key
     *            关键字
     * @param object
     *            对象
     * @return 执行 LPUSH 命令后，列表的长度。
     * 
     *         <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public Long lpush(String key, Object object) {
        ShardedJedis shardedJedis = null;
        Long size = null;
        byte[] valueBytes = CommonUtil.serialize(object);

        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                size = shardedJedis.lpush(key.getBytes(), valueBytes);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return size;
    }

    /**
     * 将一个object对象插入到列表 key 的表尾
     * 
     * @param key
     *            关键字
     * @param object
     *            对象
     * @return 执行 RPUSH 命令后，列表的长度。
     * 
     *         <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public Long rpush(String key, Object object) {
        ShardedJedis shardedJedis = null;
        Long size = null;
        byte[] valueBytes = CommonUtil.serialize(object);

        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                size = shardedJedis.rpush(key.getBytes(), valueBytes);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return size;
    }

    /**
     * 将值 value 插入到列表 key 的表头，当且仅当 key 存在并且是一个列表。
     * 
     * @param key
     *            关键字
     * @param string
     *            值
     * @return 表的长度。 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public Long lPushx(String key, String... string) {
        ShardedJedis shardedJedis = null;
        Long size = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                size = shardedJedis.lpushx(key, string);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return size;
    }

    /**
     * 返回列表 key 中指定区间内的元素，区间以偏移量 start 和 end 指定。 <br>
     * 下标(index)参数 start 和 end 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。
     * <br>
     * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
     * 
     * @param key
     *            关键字
     * @param start
     *            开始索引
     * @param end
     *            结束索引
     * @return 一个列表，包含指定区间内的元素。 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public List<String> lRange(String key, long start, long end) {
        ShardedJedis shardedJedis = null;
        List<String> elements = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                elements = shardedJedis.lrange(key, start, end);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return elements;
    }

    /**
     * 返回列表key所有的元素
     * 
     * @param key
     *            关键字
     * @return 列表key所有的元素
     * 
     *         <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public List<Object> lrange(String key) {
        ShardedJedis shardedJedis = null;
        List<Object> list = new ArrayList<Object>();
        List<byte[]> bytes = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                bytes = shardedJedis.lrange(key.getBytes(), 0, -1);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }

        if (bytes != null && bytes.size() > 0) {
            for (byte[] bs : bytes) {
                Object obj = CommonUtil.unserialize(bs);
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * 根据参数 count 的值，移除列表中与参数 value 相等的元素。
     * 
     * @param key
     *            关键字
     * @param count
     *            数量
     * @param value
     *            值
     * @return 被移除元素的数量。 <br>
     *         因为不存在的 key 被视作空表(empty list)，所以当 key 不存在时， LREM 命令总是返回 0 。 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public Long lRem(String key, long count, String value) {
        ShardedJedis shardedJedis = null;
        Long quantity = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                quantity = shardedJedis.lrem(key, count, value);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return quantity;
    }

    /**
     * 根据参数 count 的值，移除列表中与参数 value 相等的元素。
     * 
     * @param key
     *            关键字
     * @param count
     *            数量
     * @param Object
     *            值,被序列化成字节
     * @return 被移除元素的数量。 <br>
     *         因为不存在的 key 被视作空表(empty list)，所以当 key 不存在时， LREM 命令总是返回 0 。 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public Long lRem(String key, long count, Object object) {
        ShardedJedis shardedJedis = null;
        Long quantity = null;
        byte[] valueBytes = CommonUtil.serialize(object);
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                quantity = shardedJedis.lrem(key.getBytes(), count, valueBytes);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }
        return quantity;
    }

    /**
     * 以秒为单位，返回给定 key 的剩余生存时间(TTL, time to live)。
     * @param key
     * @return
     */
    public Long ttl(String key) {
        ShardedJedis shardedJedis = null;
        Long value = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                value = shardedJedis.ttl(key);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }

        return value;
    }

    /**
     * 以秒为单位，返回给定 key 的剩余生存时间(TTL, time to live)。
     * @param key
     * @return
     */
    public Long ttl(byte[] key) {
        ShardedJedis shardedJedis = null;
        Long value = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                value = shardedJedis.ttl(key);
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shardedJedis.close();
        }

        return value;
    }

}
