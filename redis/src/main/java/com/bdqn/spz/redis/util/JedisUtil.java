package com.bdqn.spz.redis.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

/**
 * Redis缓存操作的工具类<br>
 * <p>
 * Copyright: Copyright (c) Nov 7, 2013 10:31:44 AM
 * <p>
 * Company: 欣网视讯
 * <p>
 * @author houxu@xwtec.cn
 * @version 1.0.0
 */
public class JedisUtil {

    /**
     * logger 日志记录器
     */
    private final Logger logger = Logger.getLogger(JedisUtil.class);

    /**
     * jedisPool redis连接池
     */
    @Resource(name = "jedisPool")
    public JedisPool jedisPool;

    /**
     * 方法描述:从连接池获取jedis对象
     * @return 返回Jedis的有效对象
     * date:Nov 14, 2013
     * add by: houxu@xwtec.cn
     */
    public Jedis getResource() throws Exception {
        logger.debug("[JedisUtil:getResource]: get jedis Resource from Pool...");
        Jedis jedis = null;// 声明jedis对象
        int cycleTimes = 0;// 出现异常已经循环获取的次数
        try {
            jedis = this.jedisPool.getResource();// 从pool中获取jedis对象
        } catch (JedisConnectionException ex) {
            try {
                // 获取占用异常,捕获异常,等待0.5秒后继续执行获取
                logger.debug("[JedisUtil:getResource]:redis pool is full,Program will sleep 0.5s to wait an idle.message:\n"
                                + ex.getMessage());

                while (cycleTimes < 3) {
                    cycleTimes++;// 获取次数 +1;
                    Thread.sleep(500);// 等待0.5s

                    logger.debug("[JedisUtil:getResource]:waiting to get an idle...");
                    try {
                        jedis = this.jedisPool.getResource();// 重新获取jedis对象
                    } catch (JedisConnectionException ex1) {
                        logger.debug("[JedisUtil:getResource]:get an idle failed.Program will try again.");
                        // 出现获取异常,继续执行
                        continue;
                    }

                    // 获取到对象后跳出循环
                    if (jedis != null) {
                        // 输出获取成功的消息
                        logger.debug("[JedisUtil:getResource]:get an idle success.");
                        break;
                    } else { // 如果获取出对象为null,则继续循环等待获取.
                        logger.debug("[JedisUtil:getResource]:get an idle is null.Program will try again.");
                        continue;
                    }
                }
            }
            // 处理线程截断异常
            catch (InterruptedException e) {
                logger.error("[JedisUtil:getResource]:get jedis object failed.message:\n" + e.getMessage());
            }
        }
        // 获取对象如果不为空则返回
        if (jedis != null) {
            logger.debug("[JedisUtil:getResource]: get jedis Resource from Pool success.");
        } else {// 当循环获取超过三次直接抛出异常 返回null
            logger.error("[JedisUtil:getResource]:get jedis object failed.if redis server is runing,please check the configration and Network connection.");
            throw new Exception("server can not get jedis Resource!");
        }
        return jedis;
    }

    /**
     * 方法描述:根据关键字从redis服务器中获取对应的value
     * @param String key 键值
     * @return 存储在redis中的value
     * date:Nov 14, 2013
     * add by: houxu@xwtec.cn
     * @throws SPTException 
     */
    public String getRedisStrValue(String key) throws Exception {

        Jedis jedis = null;// 声明一个链接对象
        String value = null;// 获取的键值所对应的值

        try {
            jedis = this.getResource();// 获取jedis资源

            // 资源不为空,则获取对应的value
            if (jedis != null)
                value = jedis.get(key);

        } finally {
            if (jedis != null)
                jedis.close();
        }
        return value;
    }

    /**
     * 方法描述:往redis中注入缓存对象
     * @param String key 对象的键值
     * @param String value 键值所对应的值
     * @return 返回成功与否,成功返回true 失败返回false
     * date:Nov 18, 2013
     * add by: houxu@xwtec.cn
     * @throws SPTException 
     */
    public boolean setRedisStrValue(String key, String value) throws Exception {

        Jedis jedis = null;// 声明一个链接对象
        boolean flag = true;// 返回标记,默认成功

        try {
            jedis = this.getResource();// 获取资源

            // 资源不为空则执行注入操作 否则返回注入失败
            if (jedis != null)
                jedis.set(key, value);
            else
                flag = false;
        } finally {
            // 归还资源
            if (jedis != null)
                jedis.close();
        }
        return flag;
    }

    /**
     * 方法描述:往redis中注入缓存对象
     * @param String key 对象的键值
     * @param String value 键值所对应的值
     * @param int seconds 键值存储时间,如果为负数,则不设存储上限时间 单位:秒
     * @return 返回成功与否,成功返回true 失败返回false
     * date:Nov 18, 2013
     * add by: houxu@xwtec.cn
     * @throws SPTException 
     */
    public boolean setRedisStrValue(String key, String value, int seconds) throws Exception {

        boolean flag = true;// 返回标记,默认成功

        // 如果设置时间为负数,则无上限时间
        if (seconds <= 0) {
            this.setRedisStrValue(key, value);
            return flag;
        }

        Jedis jedis = null;// 声明一个链接对象

        try {
            jedis = this.getResource();// 获取资源

            // 资源不为空则执行注入操作 否则返回注入失败
            if (jedis != null) {
                // 判断是否已经存在,如果已经存在则删除
                if (jedis.exists(key)) {
                    jedis.del(key);
                }
                // 该方法内容为,如果含有相同的key值,则不覆盖.
                jedis.setex(key, seconds, value);
            } else
                flag = false;
        } finally {
            // 归还资源
            if (jedis != null)
                jedis.close();
        }
        return flag;
    }

    /**
     * 方法描述:删除redis中的缓存
     * @param 缓存的key值
     * @return 返回是否成功,成功:true 失败:false
     * date:Nov 18, 2013
     * add by: houxu@xwtec.cn
     * @throws SPTException 
     */
    public boolean delRedisStrValue(String... keys) throws Exception {

        Jedis jedis = null;// 声明一个链接对象
        boolean flag = true;// 返回标记,默认成功
        try {
            jedis = this.getResource();// 获取资源

            // 资源不为空则执行删除操作 否则返回注入失败
            if (jedis != null)
                jedis.del(keys);
            else
                flag = false;
        } finally {
            // 归还资源
            if (jedis != null)
                jedis.close();
        }
        return flag;
    }

    /**
     * 方法描述:查询对应的缓存keys
     * date:2013-11-19
     * add by: liuwenbing@xwtec.cn
     */
    @SuppressWarnings("deprecation")
    public Set<String> getKeys(String keyPrefix) {

        logger.info("RedisUtil.getKeys param: " + keyPrefix);

        Jedis jedis = null;// jedis对象
        Set<String> keys = null;// keys列表

        try {
            // 获取连接
            jedis = this.getResource();
            // 根据前台传过来的规则获取缓存key列表
            if (null != keyPrefix && !"".equals(keyPrefix)) {
                keys = jedis.keys(keyPrefix);
            }
        } catch (Exception e) {
            logger.error("[JedisUtil.getKeys]:failed. throw e:" + e.getMessage());
        } finally {
            // 使用完毕后将jedis对象归还连接池
            if (jedis != null)
                jedisPool.returnResource(jedis);
        }

        return keys;
    }

    /**
     * @功能描述：利用pipeline批量的将数据读入set中
     * @参数说明：@param key
     * @参数说明：@param list
     * @参数说明：@return
     * @作者： yuanzhen
     * @创建时间：2017年8月21日 下午1:50:47
     */
    public boolean setRedisSetValue(String key, List<String> list) {
        Jedis jedis = null;
        boolean flag = true;
        try {
            jedis = this.getResource();// 获取资源
            // 资源不为空则执行注入操作 否则返回注入失败
            if (jedis != null) {
                Pipeline pipeline = jedis.pipelined();
                for (int i = 0; i < list.size(); i++) {
                    pipeline.sadd(key, list.get(i));
                }
                pipeline.sync();
                logger.info("数据已经放入" + key + "集合中...");
            } else {
                flag = false;
                logger.error("redis连接初始化失败....");
            }
        } catch (Exception e) {
            logger.error("数据存入redis失败...抛出异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedisPool.close();
            }
        }
        return flag;
    }

    /**
     * 
     * @功能描述：利用sidff方法获得以baseKey为基准的差异集合
     * @参数说明：@param baseKey 以此key值为基准sidff
     * @参数说明：@param otherKey 与此key值sidff
     * @参数说明：@return
     * @作者： yuanzhen
     * @创建时间：2017年8月28日 下午4:13:51
     */
    public Set<String> reconciliationSidff(String baseKey, String otherKey) {
        Jedis jedis = null;
        Set<String> rst = new HashSet<String>();
        try {
            jedis = this.getResource();
            if (jedis != null) {
                rst = jedis.sdiff(baseKey, otherKey);
                logger.info("以" + baseKey + "为基准与" + otherKey + "对比完成...");
            } else {
                logger.error("redis连接初始化失败...");
            }
        } catch (Exception e) {
            logger.error("以" + baseKey + "为基准与" + otherKey + "对比数据失败...失败原因:" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedisPool.close();
            }
        }
        return rst;
    }

    /**
     * @功能描述：从redis中获取数据
     * @参数说明：@param key redis的key值
     * @参数说明：@param batch	批处理数量
     * @参数说明：@return
     * @作者： liuyi
     * @创建时间：2017年8月29日 下午2:59:02
     */
    @SuppressWarnings("deprecation")
    public List<Object> queryListByLPop(String key, int batch) {
        List<Object> result = new ArrayList<Object>();
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Pipeline pipe = jedis.pipelined();
            for (int i = 0; i < batch; i++) {
                pipe.lpop(key);
            }
            result = pipe.syncAndReturnAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedisPool.returnResource(jedis);
        }
        return result;
    }

    /**
     * 删除给定的 key 。 <br>
     * 不存在的 key 会被忽略。
     * 
     * @param key
     *            关键字
     * @return 被删除 key 的数量。 <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     * @throws Exception 
     * 
     */
    public Long del(String key) {
        Jedis shardedJedis = null;
        Long quantity = null;
        try {
            shardedJedis = this.getResource();
            if (shardedJedis != null) {
                quantity = shardedJedis.del(key);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        boolean flag = false;
        try {
            shardedJedis = this.getResource();
            if (shardedJedis != null) {
                flag = shardedJedis.exists(key);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        Long resultCode = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                resultCode = shardedJedis.expire(key, seconds);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        List<String> result = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                result = shardedJedis.sort(key);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        List<String> result = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                result = shardedJedis.sort(key, sortingParameters);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        String type = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                type = shardedJedis.type(key);
            }
        } catch (Exception e) {
            handleJedisException(e);
        } finally {
            shardedJedis.close();
        }
        return type;
    }

    /**
     * 将字符串值 value 关联到 key 。 如果 key 已经持有其他值， SET 就覆写旧值，无视类型。
     * 
     * @param key
     *            关键字
     * @param value
     *            值
     * @return 状态码 OK <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public String set(String key, String value) {
        Jedis shardedJedis = null;
        String status = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                status = shardedJedis.set(key, value);
            }
        } catch (Exception e) {
            handleJedisException(e);
        } finally {
            shardedJedis.close();
        }
        return status;
    }

    /**
     * 将Object对象值 value 关联到 key 。 如果 key 已经持有其他值， SET 就覆写旧值，无视类型。
     * 
     * @param key
     *            关键字
     * @param value
     *            对象值
     * @return 状态码 OK <br>
     *         <b>Tips:</b>若要使用返回值，请做null值校验。和服务器建立连接多次失败，则返回null
     */
    public String set(String key, Object value) {
        Jedis shardedJedis = null;
        String statusCode = null;
        byte[] valueBytes = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                valueBytes = CommonUtil.serialize(value);
                statusCode = shardedJedis.set(key.getBytes(), valueBytes);
            }
        } catch (Exception e) {
            handleJedisException(e);
        } finally {
            shardedJedis.close();
        }
        return statusCode;
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
        Jedis shardedJedis = null;
        String value = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                value = shardedJedis.get(key);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        byte[] value = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                value = shardedJedis.get(key);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        String element = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                element = shardedJedis.lindex(key, index);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        Long len = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                len = shardedJedis.llen(key);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        String element = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                element = shardedJedis.lpop(key);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        Long size = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                size = shardedJedis.lpush(key, strings);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        Long size = null;
        byte[] valueBytes = CommonUtil.serialize(object);
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                size = shardedJedis.lpush(key.getBytes(), valueBytes);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        Long size = null;
        byte[] valueBytes = CommonUtil.serialize(object);
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                size = shardedJedis.rpush(key.getBytes(), valueBytes);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        List<String> elements = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                elements = shardedJedis.lrange(key, start, end);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        List<Object> list = new ArrayList<Object>();
        List<byte[]> bytes = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                bytes = shardedJedis.lrange(key.getBytes(), 0, -1);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        Long quantity = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                quantity = shardedJedis.lrem(key, count, value);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        Long value = null;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                value = shardedJedis.ttl(key);
            }
        } catch (Exception e) {
            handleJedisException(e);
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
        Jedis shardedJedis = null;
        Long value = null;
        try {
            shardedJedis = this.getResource();
            if (shardedJedis != null) {
                value = shardedJedis.ttl(key);
            }
        } catch (Exception e) {
            handleJedisException(e);
        } finally {
            shardedJedis.close();
        }

        return value;
    }

    private boolean handleJedisException(Exception jedisException) {
        if (jedisException instanceof JedisConnectionException) {
        } else if (jedisException instanceof JedisDataException) {
            if ((jedisException.getMessage() != null) && (jedisException.getMessage().indexOf("READONLY") != -1)) {
            } else {
                // dataException, isBroken=false
                return false;
            }
        } else {
            logger.error("Jedis exception happen.", jedisException);
        }
        return true;
    }

}
