package com.bdqn.spz.redis.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;

/**
 * redis 客户端
 * 
 * @author sunyanxia
 *
 */
public class JedisClient {
    private static final Logger logger = Logger.getLogger(JedisClient.class);

    /**
     * 服务器地址
     */
    private static final String HOST = "host";

    /**
     * 服务器端口
     */
    private static final String PORT = "port";

    /**
     * redis ip
     */
    String host = ConfigReadUtil.getInstance().getProperty("redis.content.ip");

    /**
     * redis port
     */
    String port = ConfigReadUtil.getInstance().getProperty("redis.content.port");

    /**
     * 最大能够保持idel状态的对象数
     */
    String maxIdle = ConfigReadUtil.getInstance().getProperty("redis.pool.maxIdle");

    /**
     * 最大分配的对象数
     */
    String maxTotal = ConfigReadUtil.getInstance().getProperty("redis.pool.maxTotal");

    /**
     * 当池内没有返回对象时，最大等待时间
     */
    String maxWait = ConfigReadUtil.getInstance().getProperty("redis.pool.maxWait");

    /**
     * 当调用borrow Object方法时，是否进行有效性检查
     */
    String testOnBorrow = ConfigReadUtil.getInstance().getProperty("redis.pool.testOnBorrow");

    /**
     * 初始化ShardedJedisPool
     */
    private ShardedJedisPool shardedJedisPool = null;

    /**
     * 服务器列表信息
     */
    private List<Map<String, String>> serverList = null;

    /**
     * redis分片
     */
    private List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();

    public JedisClient() {

        if (null == serverList) {
            serverList = new ArrayList<Map<String, String>>();

            Map<String, String> map = new HashMap<String, String>();
            map.put("host", host);
            map.put("port", port);
            serverList.add(map);
        }
        initShardedJedisPool();

        if (null == shardedJedisPool) {
            logger.info("<请确认push read redis服务器是否启动!>");
            return;
        }
    }

    /**
     * 初始化客户端
     * 
     * @param serverFileredis服务器配置信息文件
     * @param sentinelFile
     *            redis sentinel(哨兵)配置信息文件
     */
    public void initShardedJedisPool() {
        long startTime = System.currentTimeMillis();

        logger.info("-- redisClient initializer start...");
        logger.info("-- redisClient connect to server:" + host + " - port:" + port + "...");
        if (null == shards || shards.size() == 0) {
            String host = null;
            Integer port = null;
            JedisShardInfo si = null;

            for (Map<String, String> map : serverList) {
                host = map.get(HOST).toString();
                port = Integer.parseInt(map.get(PORT).toString());
                si = new JedisShardInfo(host, port);
                shards.add(si);
            }

            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(Integer.parseInt(maxTotal));
            config.setMaxIdle(Integer.parseInt(maxIdle));
            config.setMaxWaitMillis(Integer.parseInt(maxWait));
            config.setTestOnBorrow(Boolean.parseBoolean(testOnBorrow));

            shardedJedisPool = new ShardedJedisPool(config, shards);
        }

        logger.info(
                "-- PushReadRedisClient initializer finish! times:" + (System.currentTimeMillis() - startTime) + "ms");
    }

    /**
     * 销毁连接池
     */
    public void destroy() {
        if (shardedJedisPool != null) {
            shardedJedisPool.destroy();
        }
    }

    /**
     * 获得ShardedJedis
     * 
     * @return
     */
    private ShardedJedis getResource() {
        return shardedJedisPool == null ? null : shardedJedisPool.getResource();
    }

    /**
     * 异常情况处理
     * 
     * @param ShardedJedis
     */
    private void returnBrokenResource(ShardedJedis ShardedJedis) {
        if (ShardedJedis != null && shardedJedisPool != null) {
            shardedJedisPool.returnBrokenResource(ShardedJedis);
            ShardedJedis = null;
        }
    }

    /**
     * 使用完成后归还ShardedJedis到連接池
     * 
     * @param ShardedJedis
     */
    private void returnResource(ShardedJedis ShardedJedis) {
        if (ShardedJedis != null && shardedJedisPool != null) {
            shardedJedisPool.returnResource(ShardedJedis);
        }
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                quantity = shardedJedis.del(key);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                flag = shardedJedis.exists(key);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                resultCode = shardedJedis.expire(key, seconds);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                result = shardedJedis.sort(key);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                result = shardedJedis.sort(key, sortingParameters);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                type = shardedJedis.type(key);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        ShardedJedis shardedJedis = null;
        String status = null;
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                status = shardedJedis.set(key, value);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        ShardedJedis shardedJedis = null;
        String statusCode = null;
        byte[] valueBytes = null;
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                valueBytes = CommonUtil.serialize(value);
                statusCode = shardedJedis.set(key.getBytes(), valueBytes);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        ShardedJedis shardedJedis = null;
        String value = null;
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                value = shardedJedis.get(key);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                value = shardedJedis.get(key);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        ShardedJedis shardedJedis = null;
        String element = null;
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                element = shardedJedis.lindex(key, index);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                len = shardedJedis.llen(key);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                element = shardedJedis.lpop(key);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                size = shardedJedis.lpush(key, strings);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                size = shardedJedis.lpush(key.getBytes(), valueBytes);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                size = shardedJedis.rpush(key.getBytes(), valueBytes);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                size = shardedJedis.lpushx(key, string);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                elements = shardedJedis.lrange(key, start, end);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                bytes = shardedJedis.lrange(key.getBytes(), 0, -1);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                quantity = shardedJedis.lrem(key, count, value);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                quantity = shardedJedis.lrem(key.getBytes(), count, valueBytes);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                value = shardedJedis.ttl(key);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
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
        boolean broken = false;
        try {
            shardedJedis = getResource();
            if (shardedJedis != null) {
                value = shardedJedis.ttl(key);
            }
        } catch (JedisException e) {
            broken = handleJedisException(e);
        } finally {
            closeResource(shardedJedis, broken);
        }

        return value;
    }

    private void closeResource(ShardedJedis shardedJedis, boolean conectionBroken) {
        try {
            if (conectionBroken) {
                shardedJedisPool.returnBrokenResource(shardedJedis);
            } else {
                shardedJedisPool.returnResource(shardedJedis);
            }
        } catch (Exception e) {
            logger.error("return back jedis failed, will fore close the jedis.", e);
            shardedJedisPool.destroy();
        }
    }

    private boolean handleJedisException(JedisException jedisException) {
        if (jedisException instanceof JedisConnectionException) {
            logger.error("Redis connection " + shardedJedisPool + " lost.", jedisException);
        } else if (jedisException instanceof JedisDataException) {
            if ((jedisException.getMessage() != null) && (jedisException.getMessage().indexOf("READONLY") != -1)) {
                logger.error("Redis connection " + shardedJedisPool + " are read-only slave.", jedisException);
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
