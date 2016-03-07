package com.kankan.op.cache;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import com.kankan.op.utils.Log;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisException;

/**
 * <pre>
 * 
 * Jedis命令使用模板
 * 
 * 更新至Redis 2.8
 * 
 * </pre>
 * 
 * @see http://redis.readthedocs.org/en/latest/index.html
 * @author YuanHaoliang
 */
public class JedisTemplate implements JedisCommands {
    private static final Logger log = Log.getLogger();

    /** Jedis连接池 */
    private JedisPool jedisPool;

    /**
     * 获取JedisClient实例，用完记得要调用returnResource返回连接到连接池！
     */
    public Jedis getJedis() {
        Jedis j = jedisPool.getResource();
        if (!j.isConnected()) {
            log.info("try reconnect jedis:{}", j);
            j.connect();
        }
        return j;
    }

    public JedisPool getJedisPool() {
        return this.jedisPool;
    }

    /**
     * 返还jedis实例到连接池，根据是否有exception判断正常返回还是Destory掉。
     * 
     * @param jedis
     * @param e
     */
    public void returnResource(Jedis jedis, Exception e) {
        if (e == null) {
            jedisPool.returnResource(jedis);
            throw new JedisException(e);
        }
        jedisPool.returnBrokenResource(jedis);
    }

    public JedisTemplate(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * <pre>
     * INFO [section]
     * 
     * 以一种易于解释（parse）且易于阅读的格式，返回关于 Redis 服务器的各种信息和统计数值。
     * 
     * 通过给定可选的参数 section ，可以让命令只返回某一部分的信息：
     * 
     * server : 一般 Redis 服务器信息，包含以下域：
     * 
     * redis_version : Redis 服务器版本
     * redis_git_sha1 : Git SHA1
     * redis_git_dirty : Git dirty flag
     * os : Redis 服务器的宿主操作系统
     * arch_bits : 架构（32 或 64 位）
     * multiplexing_api : Redis 所使用的事件处理机制
     * gcc_version : 编译 Redis 时所使用的 GCC 版本
     * process_id : 服务器进程的 PID
     * run_id : Redis 服务器的随机标识符（用于 Sentinel 和集群）
     * tcp_port : TCP/IP 监听端口
     * uptime_in_seconds : 自 Redis 服务器启动以来，经过的秒数
     * uptime_in_days : 自 Redis 服务器启动以来，经过的天数
     * lru_clock : 以分钟为单位进行自增的时钟，用于 LRU 管理
     * clients : 已连接客户端信息，包含以下域：
     * 
     * connected_clients : 已连接客户端的数量（不包括通过从属服务器连接的客户端）
     * client_longest_output_list : 当前连接的客户端当中，最长的输出列表
     * client_longest_input_buf : 当前连接的客户端当中，最大输入缓存
     * blocked_clients : 正在等待阻塞命令（BLPOP、BRPOP、BRPOPLPUSH）的客户端的数量
     * memory : 内存信息，包含以下域：
     * 
     * used_memory : 由 Redis 分配器分配的内存总量，以字节（byte）为单位
     * used_memory_human : 以人类可读的格式返回 Redis 分配的内存总量
     * used_memory_rss : 从操作系统的角度，返回 Redis 已分配的内存总量（俗称常驻集大小）。这个值和 top 、 ps 等命令的输出一致。
     * used_memory_peak : Redis 的内存消耗峰值（以字节为单位）
     * used_memory_peak_human : 以人类可读的格式返回 Redis 的内存消耗峰值
     * used_memory_lua : Lua 引擎所使用的内存大小（以字节为单位）
     * mem_fragmentation_ratio : used_memory_rss 和 used_memory 之间的比率
     * mem_allocator : 在编译时指定的， Redis 所使用的内存分配器。可以是 libc 、 jemalloc 或者 tcmalloc 。
     * 在理想情况下， used_memory_rss 的值应该只比 used_memory 稍微高一点儿。
     * 当 rss > used ，且两者的值相差较大时，表示存在（内部或外部的）内存碎片。
     * 内存碎片的比率可以通过 mem_fragmentation_ratio 的值看出。
     * 当 used > rss 时，表示 Redis 的部分内存被操作系统换出到交换空间了，在这种情况下，操作可能会产生明显的延迟。
     * Because Redis does not have control over how its allocations are mapped to memory pages, high used_memory_rss is often the result of a spike in memory usage.
     * 
     * 当 Redis 释放内存时，分配器可能会，也可能不会，将内存返还给操作系统。
     * 如果 Redis 释放了内存，却没有将内存返还给操作系统，那么 used_memory 的值可能和操作系统显示的 Redis 内存占用并不一致。
     * 查看 used_memory_peak 的值可以验证这种情况是否发生。
     * persistence : RDB 和 AOF 的相关信息
     * 
     * stats : 一般统计信息
     * 
     * replication : 主/从复制信息
     * 
     * cpu : CPU 计算量统计信息
     * 
     * commandstats : Redis 命令统计信息
     * 
     * cluster : Redis 集群信息
     * 
     * keyspace : 数据库相关的统计信息
     * 
     * 除上面给出的这些值以外，参数还可以是下面这两个：
     * 
     * all : 返回所有信息
     * default : 返回默认选择的信息
     * 当不带参数直接调用 INFO 命令时，使用 default 作为默认参数。
     * 
     * 不同版本的 Redis 可能对返回的一些域进行了增加或删减。
     * 因此，一个健壮的客户端程序在对 INFO 命令的输出进行分析时，应该能够跳过不认识的域，并且妥善地处理丢失不见的域。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 具体请参见下面的测试代码。
     * 
     * @see http://redis.readthedocs.org/en/latest/server/info.html
     * </pre>
     */
    public String info() {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.info();
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * 以一种易于解释（parse）且易于阅读的格式，返回关于 Redis 服务器的各种信息和统计数值。 
     * 通过给定可选的参数 section ，可以让命令只返回某一部分的信息：
     * 
     * @see #info()
     * @see http://redis.readthedocs.org/en/latest/server/info.html
     * </pre>
     */
    public String info(String section) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.info(section);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }
    /**
     * <pre>
     * KEYS pattern
     * 
     * 查找所有符合给定模式 pattern 的 key 。
     * 
     * KEYS * 匹配数据库中所有 key 。
     * KEYS h?llo 匹配 hello ， hallo 和 hxllo 等。
     * KEYS h*llo 匹配 hllo 和 heeeeello 等。
     * KEYS h[ae]llo 匹配 hello 和 hallo ，但不匹配 hillo 。
     * 特殊符号用 \ 隔开
     * 
     * KEYS 的速度非常快，但在一个大的数据库中使用它仍然可能造成性能问题，如果你需要从一个数据集中查找特定的 key ，你最好还是用 Redis 的集合结构(set)来代替。
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(N)， N 为数据库中 key 的数量。
     * 返回值：
     * 符合给定模式的 key 列表。：
     * 
     * @see http://redis.readthedocs.org/en/latest/key/keys.html
     * </pre>
     */
    public Set<String> keys(String pattern) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.keys(pattern);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * DEL key [key ...]
     * 
     * 删除给定的一个或多个 key 。
     * 
     * 不存在的 key 会被忽略。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(N)， N 为被删除的 key 的数量。
     * 删除单个字符串类型的 key ，时间复杂度为O(1)。
     * 删除单个列表、集合、有序集合或哈希表类型的 key ，时间复杂度为O(M)， M 为以上数据结构内的元素数量。
     * 返回值：
     * 被删除 key 的数量。
     * 
     * @see http://redis.readthedocs.org/en/latest/key/del.html
     * </pre>
     */
    public Long del(String... keys) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.del(keys);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SET key value [EX seconds] [PX milliseconds] [NX|XX]
     * 
     * 将字符串值 value 关联到 key 。
     * 
     * 如果 key 已经持有其他值， SET 就覆写旧值，无视类型。
     * 
     * 对于某个原本带有生存时间（TTL）的键来说， 当 SET 命令成功在这个键上执行时， 这个键原有的 TTL 将被清除。
     * 
     * 可选参数
     * 
     * 从 Redis 2.6.12 版本开始， SET 命令的行为可以通过一系列参数来修改：
     * 
     * EX second ：设置键的过期时间为 second 秒。 SET key value EX second 效果等同于 SETEX key second value 。
     * PX millisecond ：设置键的过期时间为 millisecond 毫秒。 SET key value PX millisecond 效果等同于 PSETEX key millisecond value 。
     * NX ：只在键不存在时，才对键进行设置操作。 SET key value NX 效果等同于 SETNX key value 。
     * XX ：只在键已经存在时，才对键进行设置操作。
     * 因为 SET 命令可以通过参数来实现和 SETNX 、 SETEX 和 PSETEX 三个命令的效果，所以将来的 Redis 版本可能会废弃并最终移除 SETNX 、 SETEX 和 PSETEX 这三个命令。
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 在 Redis 2.6.12 版本以前， SET 命令总是返回 OK 。
     * 
     * 从 Redis 2.6.12 版本开始， SET 在设置操作成功完成时，才返回 OK 。
     * 如果设置了 NX 或者 XX ，但因为条件没达到而造成设置操作未执行，那么命令返回空批量回复（NULL Bulk Reply）。
     * 
     * @see http://redis.readthedocs.org/en/latest/string/set.html
     * </pre>
     */
    @Override
    public String set(String key, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.set(key, value);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * GET key
     * 
     * 返回 key 所关联的字符串值。
     * 
     * 如果 key 不存在那么返回特殊值 nil 。
     * 
     * 假如 key 储存的值不是字符串类型，返回一个错误，因为 GET 只能用于处理字符串值。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 当 key 不存在时，返回 nil ，否则，返回 key 的值。
     * 如果 key 不是字符串类型，那么返回一个错误。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/get.html
     */
    @Override
    public String get(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.get(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * EXISTS key
     * 
     * 检查给定 key 是否存在。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 若 key 存在，返回 1 ，否则返回 0 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/key/exists.html
     */
    @Override
    public Boolean exists(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.exists(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * TYPE key
     * 
     * 返回 key 所储存的值的类型。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * none (key不存在)
     * string (字符串)
     * list (列表)
     * set (集合)
     * zset (有序集)
     * hash (哈希表)
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/key/type.html
     */
    @Override
    public String type(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.type(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * EXPIRE key seconds
     * 
     * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
     * 
     * 在 Redis 中，带有生存时间的 key 被称为『易失的』(volatile)。
     * 
     * 生存时间可以通过使用 DEL 命令来删除整个 key 来移除，或者被 SET 和 GETSET 命令覆写(overwrite)，这意味着，如果一个命令只是修改(alter)一个带生存时间的 key 的值而不是用一个新的 key 值来代替(replace)它的话，那么生存时间不会被改变。
     * 
     * 比如说，对一个 key 执行 INCR 命令，对一个列表进行 LPUSH 命令，或者对一个哈希表执行 HSET 命令，这类操作都不会修改 key 本身的生存时间。
     * 
     * 另一方面，如果使用 RENAME 对一个 key 进行改名，那么改名后的 key 的生存时间和改名前一样。
     * 
     * RENAME 命令的另一种可能是，尝试将一个带生存时间的 key 改名成另一个带生存时间的 another_key ，这时旧的 another_key (以及它的生存时间)会被删除，然后旧的 key 会改名为 another_key ，因此，新的 another_key 的生存时间也和原本的 key 一样。
     * 
     * 使用 PERSIST 命令可以在不删除 key 的情况下，移除 key 的生存时间，让 key 重新成为一个『持久的』(persistent) key 。
     * 
     * 更新生存时间
     * 
     * 可以对一个已经带有生存时间的 key 执行 EXPIRE 命令，新指定的生存时间会取代旧的生存时间。
     * 
     * 过期时间的精确度
     * 
     * 在 Redis 2.4 版本中，过期时间的延迟在 1 秒钟之内 —— 也即是，就算 key 已经过期，但它还是可能在过期之后一秒钟之内被访问到，而在新的 Redis 2.6 版本中，延迟被降低到 1 毫秒之内。
     * 
     * Redis 2.1.3 之前的不同之处
     * 
     * 在 Redis 2.1.3 之前的版本中，修改一个带有生存时间的 key 会导致整个 key 被删除，这一行为是受当时复制(replication)层的限制而作出的，现在这一限制已经被修复。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 设置成功返回 1 。
     * 当 key 不存在或者不能为 key 设置生存时间时(比如在低于 2.1.3 版本的 Redis 中你尝试更新 key 的生存时间)，返回 0 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/key/expire.html
     */
    @Override
    public Long expire(String key, int seconds) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.expire(key, seconds);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * EXPIREAT key timestamp
     * 
     * EXPIREAT 的作用和 EXPIRE 类似，都用于为 key 设置生存时间。
     * 
     * 不同在于 EXPIREAT 命令接受的时间参数是 UNIX 时间戳(unix timestamp)。
     * 
     * 可用版本：
     * >= 1.2.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 如果生存时间设置成功，返回 1 。
     * 当 key 不存在或没办法设置生存时间，返回 0 。
     * 
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/key/expireat.html
     */
    @Override
    public Long expireAt(String key, long unixTime) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.expireAt(key, unixTime);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * TTL key
     * 
     * 以秒为单位，返回给定 key 的剩余生存时间(TTL, time to live)。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 当 key 不存在时，返回 -2 。
     * 当 key 存在但没有设置剩余生存时间时，返回 -1 。
     * 否则，以秒为单位，返回 key 的剩余生存时间。
     * 在 Redis 2.8 以前，当 key 不存在，或者 key 没有设置剩余生存时间时，命令都返回 -1 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/key/ttl.html
     */
    @Override
    public Long ttl(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.ttl(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SETBIT key offset value
     * 
     * 对 key 所储存的字符串值，设置或清除指定偏移量上的位(bit)。
     * 
     * 位的设置或清除取决于 value 参数，可以是 0 也可以是 1 。
     * 
     * 当 key 不存在时，自动生成一个新的字符串值。
     * 
     * 字符串会进行伸展(grown)以确保它可以将 value 保存在指定的偏移量上。当字符串值进行伸展时，空白位置以 0 填充。
     * 
     * offset 参数必须大于或等于 0 ，小于 2^32 (bit 映射被限制在 512 MB 之内)。
     * 
     * 对使用大的 offset 的 SETBIT 操作来说，内存分配可能造成 Redis 服务器被阻塞。具体参考 SETRANGE 命令，warning(警告)部分。
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度:
     * O(1)
     * 返回值：
     * 指定偏移量原来储存的位。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/setbit.html
     */
    @Override
    public Boolean setbit(String key, long offset, boolean value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.setbit(key, offset, value);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * GETBIT key offset
     * 
     * 对 key 所储存的字符串值，获取指定偏移量上的位(bit)。
     * 
     * 当 offset 比字符串值的长度大，或者 key 不存在时，返回 0 。
     * 
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 字符串值指定偏移量上的位(bit)。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/getbit.html
     */
    @Override
    public Boolean getbit(String key, long offset) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.getbit(key, offset);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SETRANGE key offset value
     * 
     * 用 value 参数覆写(overwrite)给定 key 所储存的字符串值，从偏移量 offset 开始。
     * 
     * 不存在的 key 当作空白字符串处理。
     * 
     * SETRANGE 命令会确保字符串足够长以便将 value 设置在指定的偏移量上，如果给定 key 原来储存的字符串长度比偏移量小(比如字符串只有 5 个字符长，但你设置的 offset 是 10 )，那么原字符和偏移量之间的空白将用零字节(zerobytes, "\x00" )来填充。
     * 
     * 注意你能使用的最大偏移量是 2^29-1(536870911) ，因为 Redis 字符串的大小被限制在 512 兆(megabytes)以内。如果你需要使用比这更大的空间，你可以使用多个 key 。
     * 
     * 当生成一个很长的字符串时，Redis 需要分配内存空间，该操作有时候可能会造成服务器阻塞(block)。在2010年的Macbook Pro上，设置偏移量为 536870911(512MB 内存分配)，耗费约 300 毫秒， 设置偏移量为 134217728(128MB 内存分配)，耗费约 80 毫秒，设置偏移量 33554432(32MB 内存分配)，耗费约 30 毫秒，设置偏移量为 8388608(8MB 内存分配)，耗费约 8 毫秒。 注意若首次内存分配成功之后，再对同一个 key 调用 SETRANGE 操作，无须再重新内存。
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度：
     * 对小(small)的字符串，平摊复杂度O(1)。(关于什么字符串是”小”的，请参考 APPEND 命令)
     * 否则为O(M)， M 为 value 参数的长度。
     * 返回值：
     * 被 SETRANGE 修改之后，字符串的长度。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/setrange.html
     */
    @Override
    public Long setrange(String key, long offset, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.setrange(key, offset, value);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * GETRANGE key start end
     * 
     * 返回 key 中字符串值的子字符串，字符串的截取范围由 start 和 end 两个偏移量决定(包括 start 和 end 在内)。
     * 
     * 负数偏移量表示从字符串最后开始计数， -1 表示最后一个字符， -2 表示倒数第二个，以此类推。
     * 
     * GETRANGE 通过保证子字符串的值域(range)不超过实际字符串的值域来处理超出范围的值域请求。
     * 
     * 在 <= 2.0 的版本里，GETRANGE 被叫作 SUBSTR。
     * 可用版本：
     * >= 2.4.0
     * 时间复杂度：
     * O(N)， N 为要返回的字符串的长度。
     * 复杂度最终由字符串的返回值长度决定，但因为从已有字符串中取出子字符串的操作非常廉价(cheap)，所以对于长度不大的字符串，该操作的复杂度也可看作O(1)。
     * 返回值：
     * 截取得出的子字符串。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/getrange.html
     */
    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.getrange(key, startOffset, endOffset);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * GETSET key value
     * 
     * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)。
     * 
     * 当 key 存在但不是字符串类型时，返回一个错误。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 返回给定 key 的旧值。
     * 当 key 没有旧值时，也即是， key 不存在时，返回 nil 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/getset.html
     */
    @Override
    public String getSet(String key, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.getSet(key, value);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SETNX key value
     * 
     * 将 key 的值设为 value ，当且仅当 key 不存在。
     * 
     * 若给定的 key 已经存在，则 SETNX 不做任何动作。
     * 
     * SETNX 是『SET if Not eXists』(如果不存在，则 SET)的简写。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 设置成功，返回 1 。
     * 设置失败，返回 0 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/setnx.html
     */
    @Override
    public Long setnx(String key, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.setnx(key, value);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SETEX key seconds value
     * 
     * 将值 value 关联到 key ，并将 key 的生存时间设为 seconds (以秒为单位)。
     * 
     * 如果 key 已经存在， SETEX 命令将覆写旧值。
     * 
     * 这个命令类似于以下两个命令：
     * 
     * SET key value
     * EXPIRE key seconds  # 设置生存时间
     * 不同之处是， SETEX 是一个原子性(atomic)操作，关联值和设置生存时间两个动作会在同一时间内完成，该命令在 Redis 用作缓存时，非常实用。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 设置成功时返回 OK 。
     * 当 seconds 参数不合法时，返回一个错误。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/setex.html
     */
    @Override
    public String setex(String key, int seconds, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.setex(key, seconds, value);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * DECRBY key decrement
     * 
     * 将 key 所储存的值减去减量 decrement 。
     * 
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECRBY 操作。
     * 
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
     * 
     * 本操作的值限制在 64 位(bit)有符号数字表示之内。
     * 
     * 关于更多递增(increment) / 递减(decrement)操作的更多信息，请参见 INCR 命令。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 减去 decrement 之后， key 的值。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/decrby.html
     */
    @Override
    public Long decrBy(String key, long integer) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.decrBy(key, integer);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * DECR key
     * 
     * 将 key 中储存的数字值减一。
     * 
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECR 操作。
     * 
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
     * 
     * 本操作的值限制在 64 位(bit)有符号数字表示之内。
     * 
     * 关于递增(increment) / 递减(decrement)操作的更多信息，请参见 INCR 命令。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 执行 DECR 命令之后 key 的值。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/decr.html
     */
    @Override
    public Long decr(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.decr(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * DECR key
     * 
     * 将 key 中储存的数字值减一。
     * 
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECR 操作。
     * 
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
     * 
     * 本操作的值限制在 64 位(bit)有符号数字表示之内。
     * 
     * 关于递增(increment) / 递减(decrement)操作的更多信息，请参见 INCR 命令。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 执行 DECR 命令之后 key 的值。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/incrby.html
     */
    @Override
    public Long incrBy(String key, long integer) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.incrBy(key, integer);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * INCR key
     * 
     * 将 key 中储存的数字值增一。
     * 
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作。
     * 
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
     * 
     * 本操作的值限制在 64 位(bit)有符号数字表示之内。
     * 
     * 这是一个针对字符串的操作，因为 Redis 没有专用的整数类型，所以 key 内储存的字符串被解释为十进制 64 位有符号整数来执行 INCR 操作。
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 执行 INCR 命令之后 key 的值。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/incr.html
     */
    @Override
    public Long incr(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.incr(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * APPEND key value
     * 
     * 如果 key 已经存在并且是一个字符串， APPEND 命令将 value 追加到 key 原来的值的末尾。
     * 
     * 如果 key 不存在， APPEND 就简单地将给定 key 设为 value ，就像执行 SET key value 一样。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度：
     * 平摊O(1)
     * 返回值：
     * 追加 value 之后， key 中字符串的长度。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/append.html
     */
    @Override
    public Long append(String key, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.append(key, value);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * Return a subset of the string from offset start to offset end (both
     * offsets are inclusive). Negative offsets can be used in order to provide
     * an offset starting from the end of the string. So -1 means the last char,
     * -2 the penultimate and so forth.
     * <p>
     * The function handles out of range requests without raising an error, but
     * just limiting the resulting range to the actual length of the string.
     * <p>
     * Time complexity: O(start+n) (with start being the start index and n the
     * total length of the requested range). Note that the lookup part of this
     * command is O(1) so for small strings this is actually an O(1) command.
     * 
     * @param key
     * @param start
     * @param end
     * @return Bulk reply
     * </pre>
     * 
     * @see
     */
    @Override
    public String substr(String key, int start, int end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.substr(key, start, end);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * HSET key field value
     * 
     * 将哈希表 key 中的域 field 的值设为 value 。
     * 
     * 如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。
     * 
     * 如果域 field 已经存在于哈希表中，旧值将被覆盖。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 如果 field 是哈希表中的一个新建域，并且值设置成功，返回 1 。
     * 如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 0 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/hash/hset.html
     */
    @Override
    public Long hset(String key, String field, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hset(key, field, value);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * HGET key field
     * 
     * 返回哈希表 key 中给定域 field 的值。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 给定域的值。
     * 当给定域不存在或是给定 key 不存在时，返回 nil 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/hash/hget.html
     */
    @Override
    public String hget(String key, String field) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hget(key, field);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * HSETNX key field value
     * 
     * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在。
     * 
     * 若域 field 已经存在，该操作无效。
     * 
     * 如果 key 不存在，一个新哈希表被创建并执行 HSETNX 命令。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 设置成功，返回 1 。
     * 如果给定域已经存在且没有操作被执行，返回 0 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/hash/hsetnx.html
     */
    @Override
    public Long hsetnx(String key, String field, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hsetnx(key, field, value);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * HMSET key field value [field value ...]
     * 
     * 同时将多个 field-value (域-值)对设置到哈希表 key 中。
     * 
     * 此命令会覆盖哈希表中已存在的域。
     * 
     * 如果 key 不存在，一个空哈希表被创建并执行 HMSET 操作。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度：
     * O(N)， N 为 field-value 对的数量。
     * 返回值：
     * 如果命令执行成功，返回 OK 。
     * 当 key 不是哈希表(hash)类型时，返回一个错误。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/hash/hmset.html
     */
    @Override
    public String hmset(String key, Map<String, String> hash) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hmset(key, hash);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * HMGET key field [field ...]
     * 
     * 返回哈希表 key 中，一个或多个给定域的值。
     * 
     * 如果给定的域不存在于哈希表，那么返回一个 nil 值。
     * 
     * 因为不存在的 key 被当作一个空哈希表来处理，所以对一个不存在的 key 进行 HMGET 操作将返回一个只带有 nil 值的表。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度：
     * O(N)， N 为给定域的数量。
     * 返回值：
     * 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/hash/hmget.html
     */
    @Override
    public List<String> hmget(String key, String... fields) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hmget(key, fields);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * INCRBY key increment
     * 
     * 将 key 所储存的值加上增量 increment 。
     * 
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCRBY 命令。
     * 
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
     * 
     * 本操作的值限制在 64 位(bit)有符号数字表示之内。
     * 
     * 关于递增(increment) / 递减(decrement)操作的更多信息，参见 INCR 命令。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 加上 increment 之后， key 的值。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/incrby.html
     */
    @Override
    public Long hincrBy(String key, String field, long value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hincrBy(key, field, value);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * HEXISTS key field
     * 
     * 查看哈希表 key 中，给定域 field 是否存在。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 如果哈希表含有给定域，返回 1 。
     * 如果哈希表不含有给定域，或 key 不存在，返回 0 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/hash/hexists.html
     */
    @Override
    public Boolean hexists(String key, String field) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hexists(key, field);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * HDEL key field [field ...]
     * 
     * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
     * 
     * 在Redis2.4以下的版本里， HDEL 每次只能删除单个域，如果你需要在一个原子时间内删除多个域，请将命令包含在 MULTI / EXEC 块内。
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度:
     * O(N)， N 为要删除的域的数量。
     * 返回值:
     * 被成功移除的域的数量，不包括被忽略的域。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/hash/hdel.html
     */
    @Override
    public Long hdel(String key, String... fields) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hdel(key, fields);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * HLEN key
     * 
     * 返回哈希表 key 中域的数量。
     * 
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 哈希表中域的数量。
     * 当 key 不存在时，返回 0 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/hash/hlen.html
     */
    @Override
    public Long hlen(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hlen(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * HKEYS key
     * 
     * 返回哈希表 key 中的所有域。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度：
     * O(N)， N 为哈希表的大小。
     * 返回值：
     * 一个包含哈希表中所有域的表。
     * 当 key 不存在时，返回一个空表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/hash/hkeys.html
     */
    @Override
    public Set<String> hkeys(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hkeys(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * HVALS key
     * 
     * 返回哈希表 key 中所有域的值。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度：
     * O(N)， N 为哈希表的大小。
     * 返回值：
     * 一个包含哈希表中所有值的表。
     * 当 key 不存在时，返回一个空表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/hash/hvals.html
     */
    @Override
    public List<String> hvals(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hvals(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * HGETALL key
     * 
     * 返回哈希表 key 中，所有的域和值。
     * 
     * 在返回值里，紧跟每个域名(field name)之后是域的值(value)，所以返回值的长度是哈希表大小的两倍。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度：
     * O(N)， N 为哈希表的大小。
     * 返回值：
     * 以列表形式返回哈希表的域和域的值。
     * 若 key 不存在，返回空列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/hash/hgetall.html
     */
    @Override
    public Map<String, String> hgetAll(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hgetAll(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * RPUSH key value [value ...]
     * 
     * 将一个或多个值 value 插入到列表 key 的表尾(最右边)。
     * 
     * 如果有多个 value 值，那么各个 value 值按从左到右的顺序依次插入到表尾：比如对一个空列表 mylist 执行 RPUSH mylist a b c ，得出的结果列表为 a b c ，等同于执行命令 RPUSH mylist a 、 RPUSH mylist b 、 RPUSH mylist c 。
     * 
     * 如果 key 不存在，一个空列表会被创建并执行 RPUSH 操作。
     * 
     * 当 key 存在但不是列表类型时，返回一个错误。
     * 
     * 在 Redis 2.4 版本以前的 RPUSH 命令，都只接受单个 value 值。
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 执行 RPUSH 操作后，表的长度。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/list/rpush.html
     */
    @Override
    public Long rpush(String key, String... string) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.rpush(key, string);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * LPUSH key value [value ...]
     * 
     * 将一个或多个值 value 插入到列表 key 的表头
     * 
     * 如果有多个 value 值，那么各个 value 值按从左到右的顺序依次插入到表头： 比如说，对空列表 mylist 执行命令 LPUSH mylist a b c ，列表的值将是 c b a ，这等同于原子性地执行 LPUSH mylist a 、 LPUSH mylist b 和 LPUSH mylist c 三个命令。
     * 
     * 如果 key 不存在，一个空列表会被创建并执行 LPUSH 操作。
     * 
     * 当 key 存在但不是列表类型时，返回一个错误。
     * 
     * 在Redis 2.4版本以前的 LPUSH 命令，都只接受单个 value 值。
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 执行 LPUSH 命令后，列表的长度。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/list/lpush.html
     */
    @Override
    public Long lpush(String key, String... string) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.lpush(key, string);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * LLEN key
     * 
     * 返回列表 key 的长度。
     * 
     * 如果 key 不存在，则 key 被解释为一个空列表，返回 0 .
     * 
     * 如果 key 不是列表类型，返回一个错误。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 列表 key 的长度。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/list/llen.html
     */
    @Override
    public Long llen(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.llen(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * LRANGE key start stop
     * 
     * 返回列表 key 中指定区间内的元素，区间以偏移量 start 和 stop 指定。
     * 
     * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。
     * 
     * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
     * 
     * 注意LRANGE命令和编程语言区间函数的区别
     * 
     * 假如你有一个包含一百个元素的列表，对该列表执行 LRANGE list 0 10 ，结果是一个包含11个元素的列表，这表明 stop 下标也在 LRANGE 命令的取值范围之内(闭区间)，这和某些语言的区间函数可能不一致，比如Ruby的 Range.new 、 Array#slice 和Python的 range() 函数。
     * 
     * 超出范围的下标
     * 
     * 超出范围的下标值不会引起错误。
     * 
     * 如果 start 下标比列表的最大下标 end ( LLEN list 减去 1 )还要大，那么 LRANGE 返回一个空列表。
     * 
     * 如果 stop 下标比 end 下标还要大，Redis将 stop 的值设置为 end 。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度:
     * O(S+N)， S 为偏移量 start ， N 为指定区间内元素的数量。
     * 返回值:
     * 一个列表，包含指定区间内的元素。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/list/lrange.html
     */
    @Override
    public List<String> lrange(String key, long start, long end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.lrange(key, start, end);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * LTRIM key start stop
     * 
     * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
     * 
     * 举个例子，执行命令 LTRIM list 0 2 ，表示只保留列表 list 的前三个元素，其余元素全部删除。
     * 
     * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。
     * 
     * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
     * 
     * 当 key 不是列表类型时，返回一个错误。
     * 
     * LTRIM 命令通常和 LPUSH 命令或 RPUSH 命令配合使用，举个例子：
     * 
     * LPUSH log newest_log
     * LTRIM log 0 99
     * 这个例子模拟了一个日志程序，每次将最新日志 newest_log 放到 log 列表中，并且只保留最新的 100 项。注意当这样使用 LTRIM 命令时，时间复杂度是O(1)，因为平均情况下，每次只有一个元素被移除。
     * 
     * 注意LTRIM命令和编程语言区间函数的区别
     * 
     * 假如你有一个包含一百个元素的列表 list ，对该列表执行 LTRIM list 0 10 ，结果是一个包含11个元素的列表，这表明 stop 下标也在 LTRIM 命令的取值范围之内(闭区间)，这和某些语言的区间函数可能不一致，比如Ruby的 Range.new 、 Array#slice 和Python的 range() 函数。
     * 
     * 超出范围的下标
     * 
     * 超出范围的下标值不会引起错误。
     * 
     * 如果 start 下标比列表的最大下标 end ( LLEN list 减去 1 )还要大，或者 start > stop ， LTRIM 返回一个空列表(因为 LTRIM 已经将整个列表清空)。
     * 
     * 如果 stop 下标比 end 下标还要大，Redis将 stop 的值设置为 end 。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度:
     * O(N)， N 为被移除的元素的数量。
     * 返回值:
     * 命令执行成功时，返回 ok 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/list/ltrim.html
     */
    @Override
    public String ltrim(String key, long start, long end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.ltrim(key, start, end);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * LINDEX key index
     * 
     * 返回列表 key 中，下标为 index 的元素。
     * 
     * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。
     * 
     * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
     * 
     * 如果 key 不是列表类型，返回一个错误。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(N)， N 为到达下标 index 过程中经过的元素数量。
     * 因此，对列表的头元素和尾元素执行 LINDEX 命令，复杂度为O(1)。
     * 返回值:
     * 列表中下标为 index 的元素。
     * 如果 index 参数的值不在列表的区间范围内(out of range)，返回 nil 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/list/lindex.html
     */
    @Override
    public String lindex(String key, long index) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.lindex(key, index);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * LSET key index value
     * 
     * 将列表 key 下标为 index 的元素的值设置为 value 。
     * 
     * 当 index 参数超出范围，或对一个空列表( key 不存在)进行 LSET 时，返回一个错误。
     * 
     * 关于列表下标的更多信息，请参考 LINDEX 命令。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * 对头元素或尾元素进行 LSET 操作，复杂度为 O(1)。
     * 其他情况下，为 O(N)， N 为列表的长度。
     * 返回值：
     * 操作成功返回 ok ，否则返回错误信息。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/list/lset.html
     */
    @Override
    public String lset(String key, long index, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.lset(key, index, value);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * LREM key count value
     * 
     * 根据参数 count 的值，移除列表中与参数 value 相等的元素。
     * 
     * count 的值可以是以下几种：
     * 
     * count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count 。
     * count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。
     * count = 0 : 移除表中所有与 value 相等的值。
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(N)， N 为列表的长度。
     * 返回值：
     * 被移除元素的数量。
     * 因为不存在的 key 被视作空表(empty list)，所以当 key 不存在时， LREM 命令总是返回 0 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/list/lrem.html
     */
    @Override
    public Long lrem(String key, long count, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.lrem(key, count, value);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * LPOP key
     * 
     * 移除并返回列表 key 的头元素。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 列表的头元素。
     * 当 key 不存在时，返回 nil 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/list/lpop.html
     */
    @Override
    public String lpop(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.lpop(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * RPOP key
     * 
     * 移除并返回列表 key 的尾元素。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 列表的尾元素。
     * 当 key 不存在时，返回 nil 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/list/rpop.html
     */
    @Override
    public String rpop(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.rpop(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SADD key member [member ...]
     * 
     * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。
     * 
     * 假如 key 不存在，则创建一个只包含 member 元素作成员的集合。
     * 
     * 当 key 不是集合类型时，返回一个错误。
     * 
     * 在Redis2.4版本以前， SADD 只接受单个 member 值。
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度:
     * O(N)， N 是被添加的元素的数量。
     * 返回值:
     * 被添加到集合中的新元素的数量，不包括被忽略的元素。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/set/sadd.html
     */
    @Override
    public Long sadd(String key, String... member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.sadd(key, member);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SMEMBERS key
     * 
     * 返回集合 key 中的所有成员。
     * 
     * 不存在的 key 被视为空集合。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度:
     * O(N)， N 为集合的基数。
     * 返回值:
     * 集合中的所有成员。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/set/smembers.html
     */
    @Override
    public Set<String> smembers(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.smembers(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SREM key member [member ...]
     * 
     * 移除集合 key 中的一个或多个 member 元素，不存在的 member 元素会被忽略。
     * 
     * 当 key 不是集合类型，返回一个错误。
     * 
     * 在 Redis 2.4 版本以前， SREM 只接受单个 member 值。
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度:
     * O(N)， N 为给定 member 元素的数量。
     * 返回值:
     * 被成功移除的元素的数量，不包括被忽略的元素。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/set/srem.html
     */
    @Override
    public Long srem(String key, String... member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.srem(key, member);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SPOP key
     * 
     * 移除并返回集合中的一个随机元素。
     * 
     * 如果只想获取一个随机元素，但不想该元素从集合中被移除的话，可以使用 SRANDMEMBER 命令。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度:
     * O(1)
     * 返回值:
     * 被移除的随机元素。
     * 当 key 不存在或 key 是空集时，返回 nil 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/set/spop.html
     */
    @Override
    public String spop(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.spop(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SCARD key
     * 
     * 返回集合 key 的基数(集合中元素的数量)。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度:
     * O(1)
     * 返回值：
     * 集合的基数。
     * 当 key 不存在时，返回 0 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/set/scard.html
     */
    @Override
    public Long scard(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.scard(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SISMEMBER key member
     * 
     * 判断 member 元素是否集合 key 的成员。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度:
     * O(1)
     * 返回值:
     * 如果 member 元素是集合的成员，返回 1 。
     * 如果 member 元素不是集合的成员，或 key 不存在，返回 0 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/set/sismember.html
     */
    @Override
    public Boolean sismember(String key, String member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.sismember(key, member);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SRANDMEMBER key [count]
     * 
     * 如果命令执行时，只提供了 key 参数，那么返回集合中的一个随机元素。
     * 
     * 从 Redis 2.6 版本开始， SRANDMEMBER 命令接受可选的 count 参数：
     * 
     * 如果 count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，数组中的元素各不相同。如果 count 大于等于集合基数，那么返回整个集合。
     * 如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count 的绝对值。
     * 该操作和 SPOP 相似，但 SPOP 将随机元素从集合中移除并返回，而 SRANDMEMBER 则仅仅返回随机元素，而不对集合进行任何改动。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度:
     * 只提供 key 参数时为 O(1) 。
     * 如果提供了 count 参数，那么为 O(N) ，N 为返回数组的元素个数。
     * 返回值:
     * 只提供 key 参数时，返回一个元素；如果集合为空，返回 nil 。
     * 如果提供了 count 参数，那么返回一个数组；如果集合为空，返回空数组。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/set/srandmember.html
     */
    @Override
    public String srandmember(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.srandmember(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZADD key score member [[score member] [score member] ...]
     * 
     * 将一个或多个 member 元素及其 score 值加入到有序集 key 当中。
     * 
     * 如果某个 member 已经是有序集的成员，那么更新这个 member 的 score 值，并通过重新插入这个 member 元素，来保证该 member 在正确的位置上。
     * 
     * score 值可以是整数值或双精度浮点数。
     * 
     * 如果 key 不存在，则创建一个空的有序集并执行 ZADD 操作。
     * 
     * 当 key 存在但不是有序集类型时，返回一个错误。
     * 
     * 对有序集的更多介绍请参见 sorted set 。
     * 
     * 在 Redis 2.4 版本以前， ZADD 每次只能添加一个元素。
     * 可用版本：
     * >= 1.2.0
     * 时间复杂度:
     * O(M*log(N))， N 是有序集的基数， M 为成功添加的新成员的数量。
     * 返回值:
     * 被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zadd.html
     */
    @Override
    public Long zadd(String key, double score, String member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zadd(key, score, member);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZRANGE key start stop [WITHSCORES]
     * 
     * 返回有序集 key 中，指定区间内的成员。
     * 
     * 其中成员的位置按 score 值递增(从小到大)来排序。
     * 
     * 具有相同 score 值的成员按字典序(lexicographical order )来排列。
     * 
     * 如果你需要成员按 score 值递减(从大到小)来排列，请使用 ZREVRANGE 命令。
     * 
     * 下标参数 start 和 stop 都以 0 为底，也就是说，以 0 表示有序集第一个成员，以 1 表示有序集第二个成员，以此类推。
     * 你也可以使用负数下标，以 -1 表示最后一个成员， -2 表示倒数第二个成员，以此类推。
     * 超出范围的下标并不会引起错误。
     * 比如说，当 start 的值比有序集的最大下标还要大，或是 start > stop 时， ZRANGE 命令只是简单地返回一个空列表。
     * 另一方面，假如 stop 参数的值比有序集的最大下标还要大，那么 Redis 将 stop 当作最大下标来处理。
     * 可以通过使用 WITHSCORES 选项，来让成员和它的 score 值一并返回，返回列表以 value1,score1, ..., valueN,scoreN 的格式表示。
     * 客户端库可能会返回一些更复杂的数据类型，比如数组、元组等。
     * 可用版本：
     * >= 1.2.0
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数，而 M 为结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrange.html
     */
    @Override
    public Set<String> zrange(String key, long start, long end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrange(key, start, end);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREM key member [member ...]
     * 
     * 移除有序集 key 中的一个或多个成员，不存在的成员将被忽略。
     * 
     * 当 key 存在但不是有序集类型时，返回一个错误。
     * 
     * 在 Redis 2.4 版本以前， ZREM 每次只能删除一个元素。
     * 可用版本：
     * >= 1.2.0
     * 时间复杂度:
     * O(M*log(N))， N 为有序集的基数， M 为被成功移除的成员的数量。
     * 返回值:
     * 被成功移除的成员的数量，不包括被忽略的成员。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrem.html
     */
    @Override
    public Long zrem(String key, String... member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrem(key, member);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZINCRBY key increment member
     * 
     * 为有序集 key 的成员 member 的 score 值加上增量 increment 。
     * 
     * 可以通过传递一个负数值 increment ，让 score 减去相应的值，比如 ZINCRBY key -5 member ，就是让 member 的 score 值减去 5 。
     * 
     * 当 key 不存在，或 member 不是 key 的成员时， ZINCRBY key increment member 等同于 ZADD key increment member 。
     * 
     * 当 key 不是有序集类型时，返回一个错误。
     * 
     * score 值可以是整数值或双精度浮点数。
     * 
     * 可用版本：
     * >= 1.2.0
     * 时间复杂度:
     * O(log(N))
     * 返回值:
     * member 成员的新 score 值，以字符串形式表示。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zincrby.html
     */
    @Override
    public Double zincrby(String key, double score, String member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zincrby(key, score, member);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZRANK key member
     * 
     * 返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递增(从小到大)顺序排列。
     * 
     * 排名以 0 为底，也就是说， score 值最小的成员排名为 0 。
     * 
     * 使用 ZREVRANK 命令可以获得成员按 score 值递减(从大到小)排列的排名。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度:
     * O(log(N))
     * 返回值:
     * 如果 member 是有序集 key 的成员，返回 member 的排名。
     * 如果 member 不是有序集 key 的成员，返回 nil 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrank.html
     */
    @Override
    public Long zrank(String key, String member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrank(key, member);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREVRANK key member
     * 
     * 返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递减(从大到小)排序。
     * 
     * 排名以 0 为底，也就是说， score 值最大的成员排名为 0 。
     * 
     * 使用 ZRANK 命令可以获得成员按 score 值递增(从小到大)排列的排名。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度:
     * O(log(N))
     * 返回值:
     * 如果 member 是有序集 key 的成员，返回 member 的排名。
     * 如果 member 不是有序集 key 的成员，返回 nil 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrevrank.html
     */
    @Override
    public Long zrevrank(String key, String member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrank(key, member);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREVRANGE key start stop [WITHSCORES]
     * 
     * 返回有序集 key 中，指定区间内的成员。
     * 
     * 其中成员的位置按 score 值递减(从大到小)来排列。
     * 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order)排列。
     * 除了成员按 score 值递减的次序排列这一点外， ZREVRANGE 命令的其他方面和 ZRANGE 命令一样。
     * 
     * 可用版本：
     * >= 1.2.0
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数，而 M 为结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrevrange.html
     */
    @Override
    public Set<String> zrevrange(String key, long start, long end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrange(key, start, end);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从小到大)次序排列。
     * 
     * 具有相同 score 值的成员按字典序(lexicographical order)来排列(该属性是有序集提供的，不需要额外的计算)。
     * 
     * 可选的 LIMIT 参数指定返回结果的数量及区间(就像SQL中的 SELECT LIMIT offset, count )，注意当 offset 很大时，定位 offset 的操作可能需要遍历整个有序集，此过程最坏复杂度为 O(N) 时间。
     * 
     * 可选的 WITHSCORES 参数决定结果集是单单返回有序集的成员，还是将有序集成员及其 score 值一起返回。
     * 该选项自 Redis 2.0 版本起可用。
     * 区间及无限
     * 
     * min 和 max 可以是 -inf 和 +inf ，这样一来，你就可以在不知道有序集的最低和最高 score 值的情况下，使用 ZRANGEBYSCORE 这类命令。
     * 
     * 默认情况下，区间的取值使用闭区间 (小于等于或大于等于)，你也可以通过给参数前增加 ( 符号来使用可选的开区间 (小于或大于)。
     * 
     * 举个例子：
     * 
     * ZRANGEBYSCORE zset (1 5
     * 返回所有符合条件 1 < score <= 5 的成员，而
     * 
     * ZRANGEBYSCORE zset (5 (10
     * 则返回所有符合条件 5 < score < 10 的成员。
     * 
     * 可用版本：
     * >= 1.0.5
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为被结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrangebyscore.html
     */
    @Override
    public Set<Tuple> zrangeWithScores(String key, long start, long end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrangeByScoreWithScores(key, start, end);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。有序集成员按 score 值递减(从大到小)的次序排列。
     * 
     * 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order )排列。
     * 
     * 除了成员按 score 值递减的次序排列这一点外， ZREVRANGEBYSCORE 命令的其他方面和 ZRANGEBYSCORE 命令一样。
     * 
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrevrangebyscore.html
     */
    @Override
    public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrangeByScoreWithScores(key, start, end);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZCARD key
     * 
     * 返回有序集 key 的基数。
     * 
     * 可用版本：
     * >= 1.2.0
     * 时间复杂度:
     * O(1)
     * 返回值:
     * 当 key 存在且是有序集类型时，返回有序集的基数。
     * 当 key 不存在时，返回 0 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zcard.html
     */
    @Override
    public Long zcard(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zcard(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZSCORE key member
     * 
     * 返回有序集 key 中，成员 member 的 score 值。
     * 
     * 如果 member 元素不是有序集 key 的成员，或 key 不存在，返回 nil 。
     * 
     * 可用版本：
     * >= 1.2.0
     * 时间复杂度:
     * O(1)
     * 返回值:
     * member 成员的 score 值，以字符串形式表示。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zscore.html
     */
    @Override
    public Double zscore(String key, String member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zscore(key, member);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SORT
     * SORT key [BY pattern] [LIMIT offset count] [GET pattern [GET pattern ...]] [ASC | DESC] [ALPHA] [STORE destination]
     * 
     * 返回或保存给定列表、集合、有序集合 key 中经过排序的元素。
     * 
     * 排序默认以数字作为对象，值被解释为双精度浮点数，然后进行比较。
     * 
     * 一般 SORT 用法
     * 最简单的 SORT 使用方法是 SORT key 和 SORT key DESC ：
     * 
     * SORT key 返回键值从小到大排序的结果。
     * SORT key DESC 返回键值从大到小排序的结果。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/key/sort.html
     */
    @Override
    public List<String> sort(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.sort(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SORT key [BY pattern] [LIMIT offset count] [GET pattern [GET pattern ...]] [ASC | DESC] [ALPHA] [STORE destination]
     * 
     * 返回或保存给定列表、集合、有序集合 key 中经过排序的元素。
     * 
     * 排序默认以数字作为对象，值被解释为双精度浮点数，然后进行比较。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/key/sort.html
     */
    @Override
    public List<String> sort(String key, SortingParams sortingParameters) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.sort(key, sortingParameters);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZCOUNT key min max
     * 
     * 返回有序集 key 中， score 值在 min 和 max 之间(默认包括 score 值等于 min 或 max )的成员的数量。
     * 
     * 关于参数 min 和 max 的详细使用方法，请参考 ZRANGEBYSCORE 命令。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度:
     * O(log(N))， N 为有序集的基数。
     * 返回值:
     * score 值在 min 和 max 之间的成员的数量。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zcount.html
     */
    @Override
    public Long zcount(String key, double min, double max) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zcount(key, min, max);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZCOUNT key min max
     * 
     * 返回有序集 key 中， score 值在 min 和 max 之间(默认包括 score 值等于 min 或 max )的成员的数量。
     * 
     * 关于参数 min 和 max 的详细使用方法，请参考 ZRANGEBYSCORE 命令。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度:
     * O(log(N))， N 为有序集的基数。
     * 返回值:
     * score 值在 min 和 max 之间的成员的数量。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zcount.html
     */
    @Override
    public Long zcount(String key, String min, String max) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zcount(key, min, max);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从小到大)次序排列。
     * 
     * 具有相同 score 值的成员按字典序(lexicographical order)来排列(该属性是有序集提供的，不需要额外的计算)。
     * 
     * 可选的 LIMIT 参数指定返回结果的数量及区间(就像SQL中的 SELECT LIMIT offset, count )，注意当 offset 很大时，定位 offset 的操作可能需要遍历整个有序集，此过程最坏复杂度为 O(N) 时间。
     * 
     * 可选的 WITHSCORES 参数决定结果集是单单返回有序集的成员，还是将有序集成员及其 score 值一起返回。
     * 该选项自 Redis 2.0 版本起可用。
     * 区间及无限
     * 
     * min 和 max 可以是 -inf 和 +inf ，这样一来，你就可以在不知道有序集的最低和最高 score 值的情况下，使用 ZRANGEBYSCORE 这类命令。
     * 
     * 默认情况下，区间的取值使用闭区间 (小于等于或大于等于)，你也可以通过给参数前增加 ( 符号来使用可选的开区间 (小于或大于)。
     * 
     * 举个例子：
     * 
     * ZRANGEBYSCORE zset (1 5
     * 返回所有符合条件 1 < score <= 5 的成员，而
     * 
     * ZRANGEBYSCORE zset (5 (10
     * 则返回所有符合条件 5 < score < 10 的成员。
     * 
     * 可用版本：
     * >= 1.0.5
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为被结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrangebyscore.html
     */
    @Override
    public Set<String> zrangeByScore(String key, double min, double max) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrangeByScore(key, min, max);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从小到大)次序排列。
     * </pre>
     * 
     * @see #zrangeByScore(String, double, double)
     */
    @Override
    public Set<String> zrangeByScore(String key, String min, String max) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrangeByScore(key, min, max);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。有序集成员按 score 值递减(从大到小)的次序排列。
     * 
     * 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order )排列。
     * 
     * 除了成员按 score 值递减的次序排列这一点外， ZREVRANGEBYSCORE 命令的其他方面和 ZRANGEBYSCORE 命令一样。
     * 
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrevrangebyscore.html
     */
    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrangeByScore(key, max, min);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从小到大)次序排列。
     * 
     * 具有相同 score 值的成员按字典序(lexicographical order)来排列(该属性是有序集提供的，不需要额外的计算)。
     * 
     * 可选的 LIMIT 参数指定返回结果的数量及区间(就像SQL中的 SELECT LIMIT offset, count )，注意当 offset 很大时，定位 offset 的操作可能需要遍历整个有序集，此过程最坏复杂度为 O(N) 时间。
     * 
     * 可选的 WITHSCORES 参数决定结果集是单单返回有序集的成员，还是将有序集成员及其 score 值一起返回。
     * 该选项自 Redis 2.0 版本起可用。
     * 区间及无限
     * 
     * min 和 max 可以是 -inf 和 +inf ，这样一来，你就可以在不知道有序集的最低和最高 score 值的情况下，使用 ZRANGEBYSCORE 这类命令。
     * 
     * 默认情况下，区间的取值使用闭区间 (小于等于或大于等于)，你也可以通过给参数前增加 ( 符号来使用可选的开区间 (小于或大于)。
     * 
     * 举个例子：
     * 
     * ZRANGEBYSCORE zset (1 5
     * 返回所有符合条件 1 < score <= 5 的成员，而
     * 
     * ZRANGEBYSCORE zset (5 (10
     * 则返回所有符合条件 5 < score < 10 的成员。
     * 
     * 可用版本：
     * >= 1.0.5
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为被结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrangebyscore.html
     */
    @Override
    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrangeByScore(key, min, max, offset, count);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。有序集成员按 score 值递减(从大到小)的次序排列。
     * 
     * 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order )排列。
     * 
     * 除了成员按 score 值递减的次序排列这一点外， ZREVRANGEBYSCORE 命令的其他方面和 ZRANGEBYSCORE 命令一样。
     * 
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrevrangebyscore.html
     */
    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrangeByScore(key, max, min);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从小到大)次序排列。
     * 
     * 具有相同 score 值的成员按字典序(lexicographical order)来排列(该属性是有序集提供的，不需要额外的计算)。
     * 
     * 可选的 LIMIT 参数指定返回结果的数量及区间(就像SQL中的 SELECT LIMIT offset, count )，注意当 offset 很大时，定位 offset 的操作可能需要遍历整个有序集，此过程最坏复杂度为 O(N) 时间。
     * 
     * 可选的 WITHSCORES 参数决定结果集是单单返回有序集的成员，还是将有序集成员及其 score 值一起返回。
     * 该选项自 Redis 2.0 版本起可用。
     * 区间及无限
     * 
     * min 和 max 可以是 -inf 和 +inf ，这样一来，你就可以在不知道有序集的最低和最高 score 值的情况下，使用 ZRANGEBYSCORE 这类命令。
     * 
     * 默认情况下，区间的取值使用闭区间 (小于等于或大于等于)，你也可以通过给参数前增加 ( 符号来使用可选的开区间 (小于或大于)。
     * 
     * 举个例子：
     * 
     * ZRANGEBYSCORE zset (1 5
     * 返回所有符合条件 1 < score <= 5 的成员，而
     * 
     * ZRANGEBYSCORE zset (5 (10
     * 则返回所有符合条件 5 < score < 10 的成员。
     * 
     * 可用版本：
     * >= 1.0.5
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为被结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrangebyscore.html
     */
    @Override
    public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrangeByScore(key, min, max, offset, count);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。有序集成员按 score 值递减(从大到小)的次序排列。
     * 
     * 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order )排列。
     * 
     * 除了成员按 score 值递减的次序排列这一点外， ZREVRANGEBYSCORE 命令的其他方面和 ZRANGEBYSCORE 命令一样。
     * 
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrevrangebyscore.html
     */
    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrangeByScore(key, max, min, offset, count);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从小到大)次序排列。
     * 
     * 具有相同 score 值的成员按字典序(lexicographical order)来排列(该属性是有序集提供的，不需要额外的计算)。
     * 
     * 可选的 LIMIT 参数指定返回结果的数量及区间(就像SQL中的 SELECT LIMIT offset, count )，注意当 offset 很大时，定位 offset 的操作可能需要遍历整个有序集，此过程最坏复杂度为 O(N) 时间。
     * 
     * 可选的 WITHSCORES 参数决定结果集是单单返回有序集的成员，还是将有序集成员及其 score 值一起返回。
     * 该选项自 Redis 2.0 版本起可用。
     * 区间及无限
     * 
     * min 和 max 可以是 -inf 和 +inf ，这样一来，你就可以在不知道有序集的最低和最高 score 值的情况下，使用 ZRANGEBYSCORE 这类命令。
     * 
     * 默认情况下，区间的取值使用闭区间 (小于等于或大于等于)，你也可以通过给参数前增加 ( 符号来使用可选的开区间 (小于或大于)。
     * 
     * 举个例子：
     * 
     * ZRANGEBYSCORE zset (1 5
     * 返回所有符合条件 1 < score <= 5 的成员，而
     * 
     * ZRANGEBYSCORE zset (5 (10
     * 则返回所有符合条件 5 < score < 10 的成员。
     * 
     * 可用版本：
     * >= 1.0.5
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为被结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrangebyscore.html
     */
    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrangeByScoreWithScores(key, min, max);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。有序集成员按 score 值递减(从大到小)的次序排列。
     * 
     * 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order )排列。
     * 
     * 除了成员按 score 值递减的次序排列这一点外， ZREVRANGEBYSCORE 命令的其他方面和 ZRANGEBYSCORE 命令一样。
     * 
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrevrangebyscore.html
     */
    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrangeByScoreWithScores(key, max, min);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从小到大)次序排列。
     * 
     * 具有相同 score 值的成员按字典序(lexicographical order)来排列(该属性是有序集提供的，不需要额外的计算)。
     * 
     * 可选的 LIMIT 参数指定返回结果的数量及区间(就像SQL中的 SELECT LIMIT offset, count )，注意当 offset 很大时，定位 offset 的操作可能需要遍历整个有序集，此过程最坏复杂度为 O(N) 时间。
     * 
     * 可选的 WITHSCORES 参数决定结果集是单单返回有序集的成员，还是将有序集成员及其 score 值一起返回。
     * 该选项自 Redis 2.0 版本起可用。
     * 区间及无限
     * 
     * min 和 max 可以是 -inf 和 +inf ，这样一来，你就可以在不知道有序集的最低和最高 score 值的情况下，使用 ZRANGEBYSCORE 这类命令。
     * 
     * 默认情况下，区间的取值使用闭区间 (小于等于或大于等于)，你也可以通过给参数前增加 ( 符号来使用可选的开区间 (小于或大于)。
     * 
     * 举个例子：
     * 
     * ZRANGEBYSCORE zset (1 5
     * 返回所有符合条件 1 < score <= 5 的成员，而
     * 
     * ZRANGEBYSCORE zset (5 (10
     * 则返回所有符合条件 5 < score < 10 的成员。
     * 
     * 可用版本：
     * >= 1.0.5
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为被结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrangebyscore.html
     */
    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。有序集成员按 score 值递减(从大到小)的次序排列。
     * 
     * 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order )排列。
     * 
     * 除了成员按 score 值递减的次序排列这一点外， ZREVRANGEBYSCORE 命令的其他方面和 ZRANGEBYSCORE 命令一样。
     * 
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrevrangebyscore.html
     */
    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrangeByScore(key, max, min, offset, count);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从小到大)次序排列。
     * 
     * 具有相同 score 值的成员按字典序(lexicographical order)来排列(该属性是有序集提供的，不需要额外的计算)。
     * 
     * 可选的 LIMIT 参数指定返回结果的数量及区间(就像SQL中的 SELECT LIMIT offset, count )，注意当 offset 很大时，定位 offset 的操作可能需要遍历整个有序集，此过程最坏复杂度为 O(N) 时间。
     * 
     * 可选的 WITHSCORES 参数决定结果集是单单返回有序集的成员，还是将有序集成员及其 score 值一起返回。
     * 该选项自 Redis 2.0 版本起可用。
     * 区间及无限
     * 
     * min 和 max 可以是 -inf 和 +inf ，这样一来，你就可以在不知道有序集的最低和最高 score 值的情况下，使用 ZRANGEBYSCORE 这类命令。
     * 
     * 默认情况下，区间的取值使用闭区间 (小于等于或大于等于)，你也可以通过给参数前增加 ( 符号来使用可选的开区间 (小于或大于)。
     * 
     * 举个例子：
     * 
     * ZRANGEBYSCORE zset (1 5
     * 返回所有符合条件 1 < score <= 5 的成员，而
     * 
     * ZRANGEBYSCORE zset (5 (10
     * 则返回所有符合条件 5 < score < 10 的成员。
     * 
     * 可用版本：
     * >= 1.0.5
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为被结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrangebyscore.html
     */
    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrangeByScoreWithScores(key, min, max);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。有序集成员按 score 值递减(从大到小)的次序排列。
     * 
     * 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order )排列。
     * 
     * 除了成员按 score 值递减的次序排列这一点外， ZREVRANGEBYSCORE 命令的其他方面和 ZRANGEBYSCORE 命令一样。
     * 
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrevrangebyscore.html
     */
    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrangeByScoreWithScores(key, max, min);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。有序集成员按 score 值递减(从大到小)的次序排列。
     * 
     * 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order )排列。
     * 
     * 除了成员按 score 值递减的次序排列这一点外， ZREVRANGEBYSCORE 命令的其他方面和 ZRANGEBYSCORE 命令一样。
     * 
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrevrangebyscore.html
     */
    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。有序集成员按 score 值递减(从大到小)的次序排列。
     * 
     * 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order )排列。
     * 
     * 除了成员按 score 值递减的次序排列这一点外， ZREVRANGEBYSCORE 命令的其他方面和 ZRANGEBYSCORE 命令一样。
     * 
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrevrangebyscore.html
     */
    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
     * 
     * 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。有序集成员按 score 值递减(从大到小)的次序排列。
     * 
     * 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order )排列。
     * 
     * 除了成员按 score 值递减的次序排列这一点外， ZREVRANGEBYSCORE 命令的其他方面和 ZRANGEBYSCORE 命令一样。
     * 
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数， M 为结果集的基数。
     * 返回值:
     * 指定区间内，带有 score 值(可选)的有序集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zrevrangebyscore.html
     */
    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREMRANGEBYRANK key start stop
     * 
     * 移除有序集 key 中，指定排名(rank)区间内的所有成员。
     * 
     * 区间分别以下标参数 start 和 stop 指出，包含 start 和 stop 在内。
     * 
     * 下标参数 start 和 stop 都以 0 为底，也就是说，以 0 表示有序集第一个成员，以 1 表示有序集第二个成员，以此类推。
     * 你也可以使用负数下标，以 -1 表示最后一个成员， -2 表示倒数第二个成员，以此类推。
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数，而 M 为被移除成员的数量。
     * 返回值:
     * 被移除成员的数量。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zremrangebyrank.html
     */
    @Override
    public Long zremrangeByRank(String key, long start, long end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zremrangeByRank(key, start, end);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREMRANGEBYSCORE key min max
     * 
     * 移除有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。
     * 
     * 自版本2.1.6开始， score 值等于 min 或 max 的成员也可以不包括在内，详情请参见 ZRANGEBYSCORE 命令。
     * 
     * 可用版本：
     * >= 1.2.0
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数，而 M 为被移除成员的数量。
     * 返回值:
     * 被移除成员的数量。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zremrangebyscore.html
     */
    @Override
    public Long zremrangeByScore(String key, double start, double end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zremrangeByScore(key, start, end);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZREMRANGEBYSCORE key min max
     * 
     * 移除有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。
     * 
     * 自版本2.1.6开始， score 值等于 min 或 max 的成员也可以不包括在内，详情请参见 ZRANGEBYSCORE 命令。
     * 
     * 可用版本：
     * >= 1.2.0
     * 时间复杂度:
     * O(log(N)+M)， N 为有序集的基数，而 M 为被移除成员的数量。
     * 返回值:
     * 被移除成员的数量。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/sorted_set/zremrangebyscore.html
     */
    @Override
    public Long zremrangeByScore(String key, String start, String end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zremrangeByScore(key, start, end);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * LINSERT key BEFORE|AFTER pivot value
     * 
     * 将值 value 插入到列表 key 当中，位于值 pivot 之前或之后。
     * 
     * 当 pivot 不存在于列表 key 时，不执行任何操作。
     * 
     * 当 key 不存在时， key 被视为空列表，不执行任何操作。
     * 
     * 如果 key 不是列表类型，返回一个错误。
     * 
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度:
     * O(N)， N 为寻找 pivot 过程中经过的元素数量。
     * 返回值:
     * 如果命令执行成功，返回插入操作完成之后，列表的长度。
     * 如果没有找到 pivot ，返回 -1 。
     * 如果 key 不存在或为空列表，返回 0 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/list/linsert.html
     */
    @Override
    public Long linsert(String key, LIST_POSITION where, String pivot, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.linsert(key, where, pivot, value);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }
    
    /**
     * 计算给定的一个或多个有序集的并集，其中给定 key 的数量必须以 numkeys 参数指定，
     * 并将该并集(结果集)储存到 destination 。
    * @param dstkey
    * @param sets
    * @param params
    * @return Integer reply, specifically the number of elements in the sorted
    *         set at dstkey
    */
   public Long zunionstore(final String dstkey,final String... sets) {
	   Exception ex = null;
       Jedis jedis = getJedis();
       try {
           return jedis.zunionstore(dstkey, sets);
       } catch (Exception e) {
           jedisPool.returnBrokenResource(jedis);
           ex = e;
           throw new JedisException(e);
       } finally {
           if (ex == null) {
               jedisPool.returnResource(jedis);
           }
       }
   }

    /**
     * <pre>
     * CONFIG GET parameter
     * 
     * CONFIG GET 命令用于取得运行中的 Redis 服务器的配置参数(configuration parameters)，在 Redis 2.4 版本中， 有部分参数没有办法用 CONFIG GET 访问，但是在最新的 Redis 2.6 版本中，所有配置参数都已经可以用 CONFIG GET 访问了。
     * 
     * CONFIG GET 接受单个参数 parameter 作为搜索关键字，查找所有匹配的配置参数，其中参数和值以“键-值对”(key-value pairs)的方式排列。
     * 
     * 比如执行 CONFIG GET s* 命令，服务器就会返回所有以 s 开头的配置参数及参数的值：
     * 
     * redis> CONFIG GET s*
     * 1) "save"                       # 参数名：save
     * 2) "900 1 300 10 60 10000"      # save 参数的值
     * 3) "slave-serve-stale-data"     # 参数名： slave-serve-stale-data
     * 4) "yes"                        # slave-serve-stale-data 参数的值
     * 5) "set-max-intset-entries"     # ...
     * 6) "512"
     * 7) "slowlog-log-slower-than"
     * 8) "1000"
     * 9) "slowlog-max-len"
     * 10) "1000"
     * 如果你只是寻找特定的某个参数的话，你当然也可以直接指定参数的名字：
     * 
     * redis> CONFIG GET slowlog-max-len
     * 1) "slowlog-max-len"
     * 2) "1000"
     * 使用命令 CONFIG GET * ，可以列出 CONFIG GET 命令支持的所有参数：
     * 
     * redis> CONFIG GET *
     * 1) "dir"
     * 2) "/var/lib/redis"
     * 3) "dbfilename"
     * 4) "dump.rdb"
     * 5) "requirepass"
     * 6) (nil)
     * 7) "masterauth"
     * 8) (nil)
     * 9) "maxmemory"
     * 10) "0"
     * 11) "maxmemory-policy"
     * 12) "volatile-lru"
     * 13) "maxmemory-samples"
     * 14) "3"
     * 15) "timeout"
     * 16) "0"
     * 17) "appendonly"
     * 18) "no"
     * # ...
     * 49) "loglevel"
     * 50) "verbose"
     * 所有被 CONFIG SET 所支持的配置参数都可以在配置文件 redis.conf 中找到，不过 CONFIG GET 和 CONFIG SET 使用的格式和 redis.conf 文件所使用的格式有以下两点不同：
     * 
     * 10kb 、 2gb 这些在配置文件中所使用的储存单位缩写，不可以用在 CONFIG 命令中， CONFIG SET 的值只能通过数字值显式地设定。
     * 
     * 像 CONFIG SET xxx 1k 这样的命令是错误的，正确的格式是 CONFIG SET xxx 1000 。
     * save 选项在 redis.conf 中是用多行文字储存的，但在 CONFIG GET 命令中，它只打印一行文字。
     * 
     * 以下是 save 选项在 redis.conf 文件中的表示：
     * 
     * save 900 1
     * save 300 10
     * save 60 10000
     * 
     * 但是 CONFIG GET 命令的输出只有一行：
     * 
     * redis> CONFIG GET save
     * 1) "save"
     * 2) "900 1 300 10 60 10000"
     * 
     * 上面 save 参数的三个值表示：在 900 秒内最少有 1 个 key 被改动，或者 300 秒内最少有 10 个 key 被改动，又或者 60 秒内最少有 1000 个 key 被改动，以上三个条件随便满足一个，就触发一次保存操作。
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度：
     * 不明确
     * 返回值：
     * 给定配置参数的值。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/server/config_get.html
     */
    public List<String> configGet(String pattern) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.configGet(pattern);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SDIFF key [key ...]
     * 
     * 返回一个集合的全部成员，该集合是所有给定集合之间的差集。
     * 
     * 不存在的 key 被视为空集。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度:
     * O(N)， N 是所有给定集合的成员数量之和。
     * 返回值:
     * 一个包含差集成员的列表。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/set/sdiff.html
     */
    public Set<String> sdiff(String[] keys) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.sdiff(keys);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SET key value [EX seconds] [PX milliseconds] [NX|XX]
     * 
     * 将字符串值 value 关联到 key 。
     * 
     * 如果 key 已经持有其他值， SET 就覆写旧值，无视类型。
     * 
     * 对于某个原本带有生存时间（TTL）的键来说， 当 SET 命令成功在这个键上执行时， 这个键原有的 TTL 将被清除。
     * 
     * 可选参数
     * 
     * 从 Redis 2.6.12 版本开始， SET 命令的行为可以通过一系列参数来修改：
     * 
     * EX second ：设置键的过期时间为 second 秒。 SET key value EX second 效果等同于 SETEX key second value 。
     * PX millisecond ：设置键的过期时间为 millisecond 毫秒。 SET key value PX millisecond 效果等同于 PSETEX key millisecond value 。
     * NX ：只在键不存在时，才对键进行设置操作。 SET key value NX 效果等同于 SETNX key value 。
     * XX ：只在键已经存在时，才对键进行设置操作。
     * 因为 SET 命令可以通过参数来实现和 SETNX 、 SETEX 和 PSETEX 三个命令的效果，所以将来的 Redis 版本可能会废弃并最终移除 SETNX 、 SETEX 和 PSETEX 这三个命令。
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 在 Redis 2.6.12 版本以前， SET 命令总是返回 OK 。
     * 
     * 从 Redis 2.6.12 版本开始， SET 在设置操作成功完成时，才返回 OK 。
     * 如果设置了 NX 或者 XX ，但因为条件没达到而造成设置操作未执行，那么命令返回空批量回复（NULL Bulk Reply）。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/set.html
     */
    @Override
    public String set(String key, String value, String nxxx, String expx, long time) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.set(key, value, nxxx, expx, time);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * PERSIST key
     * 
     * 移除给定 key 的生存时间，将这个 key 从『易失的』(带生存时间 key )转换成『持久的』(一个不带生存时间、永不过期的 key )。
     * 
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 当生存时间移除成功时，返回 1 .
     * 如果 key 不存在或 key 没有设置生存时间，返回 0 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/key/persist.html
     */
    @Override
    public Long persist(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.persist(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SETBIT key offset value
     * 
     * 对 key 所储存的字符串值，设置或清除指定偏移量上的位(bit)。
     * 
     * 位的设置或清除取决于 value 参数，可以是 0 也可以是 1 。
     * 
     * 当 key 不存在时，自动生成一个新的字符串值。
     * 
     * 字符串会进行伸展(grown)以确保它可以将 value 保存在指定的偏移量上。当字符串值进行伸展时，空白位置以 0 填充。
     * 
     * offset 参数必须大于或等于 0 ，小于 2^32 (bit 映射被限制在 512 MB 之内)。
     * 
     * 对使用大的 offset 的 SETBIT 操作来说，内存分配可能造成 Redis 服务器被阻塞。具体参考 SETRANGE 命令，warning(警告)部分。
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度:
     * O(1)
     * 返回值：
     * 指定偏移量原来储存的位。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/setbit.html
     */
    @Override
    public Boolean setbit(String key, long offset, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.setbit(key, offset, value);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * STRLEN key
     * 
     * 返回 key 所储存的字符串值的长度。
     * 
     * 当 key 储存的不是字符串值时，返回一个错误。
     * 
     * 可用版本：
     * >= 2.2.0
     * 复杂度：
     * O(1)
     * 返回值：
     * 字符串值的长度。
     * 当 key 不存在时，返回 0 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/strlen.html
     */
    @Override
    public Long strlen(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.strlen(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZADD key score member [[score member] [score member] ...]
     * 
     * 将一个或多个 member 元素及其 score 值加入到有序集 key 当中。
     * 
     * 如果某个 member 已经是有序集的成员，那么更新这个 member 的 score 值，并通过重新插入这个 member 元素，来保证该 member 在正确的位置上。
     * 
     * score 值可以是整数值或双精度浮点数。
     * 
     * 如果 key 不存在，则创建一个空的有序集并执行 ZADD 操作。
     * 
     * 当 key 存在但不是有序集类型时，返回一个错误。
     * 
     * 对有序集的更多介绍请参见 sorted set 。
     * 
     * 在 Redis 2.4 版本以前， ZADD 每次只能添加一个元素。
     * 可用版本：
     * >= 1.2.0
     * 时间复杂度:
     * O(M*log(N))， N 是有序集的基数， M 为成功添加的新成员的数量。
     * 返回值:
     * 被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员。
     * </pre>
     * 
     * @see #zadd(String, double, String)
     */
    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zadd(key, scoreMembers);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * LPUSHX key value
     * 
     * 将值 value 插入到列表 key 的表头，当且仅当 key 存在并且是一个列表。
     * 
     * 和 LPUSH 命令相反，当 key 不存在时， LPUSHX 命令什么也不做。
     * 
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * LPUSHX 命令执行之后，表的长度。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/list/lpushx.html
     */
    @Override
    public Long lpushx(String key, String... string) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.lpushx(key, string);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * RPUSHX key value
     * 
     * 将值 value 插入到列表 key 的表尾，当且仅当 key 存在并且是一个列表。
     * 
     * 和 RPUSH 命令相反，当 key 不存在时， RPUSHX 命令什么也不做。
     * 
     * 可用版本：
     * >= 2.2.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * RPUSHX 命令执行之后，表的长度。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/list/rpushx.html
     */
    @Override
    public Long rpushx(String key, String... string) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.rpushx(key, string);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * BLPOP key [key ...] timeout
     * 
     * BLPOP 是列表的阻塞式(blocking)弹出原语。
     * 
     * 它是 LPOP 命令的阻塞版本，当给定列表内没有任何元素可供弹出的时候，连接将被 BLPOP 命令阻塞，直到等待超时或发现可弹出元素为止。
     * 
     * 当给定多个 key 参数时，按参数 key 的先后顺序依次检查各个列表，弹出第一个非空列表的头元素。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/list/blpop.html
     */
    @Override
    public List<String> blpop(String arg) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.blpop(arg);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * BRPOP key [key ...] timeout
     * 
     * BRPOP 是列表的阻塞式(blocking)弹出原语。
     * 
     * 它是 RPOP 命令的阻塞版本，当给定列表内没有任何元素可供弹出的时候，连接将被 BRPOP 命令阻塞，直到等待超时或发现可弹出元素为止。
     * 
     * 当给定多个 key 参数时，按参数 key 的先后顺序依次检查各个列表，弹出第一个非空列表的尾部元素。
     * 
     * 关于阻塞操作的更多信息，请查看 BLPOP 命令， BRPOP 除了弹出元素的位置和 BLPOP 不同之外，其他表现一致。
     * 
     * 可用版本：
     * >= 2.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 假如在指定时间内没有任何元素被弹出，则返回一个 nil 和等待时长。
     * 反之，返回一个含有两个元素的列表，第一个元素是被弹出元素所属的 key ，第二个元素是被弹出元素的值。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/list/brpop.html
     */
    @Override
    public List<String> brpop(String arg) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.brpop(arg);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * DEL key [key ...]
     * 
     * 删除给定的一个或多个 key 。
     *     
     * @see #del(String...)
     * @see http://redis.readthedocs.org/en/latest/key/del.html
     * </pre>
     */
    @Override
    public Long del(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.del(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ECHO message
     * 
     * 打印一个特定的信息 message ，测试时使用。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * message 自身。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/connection/echo.html
     */
    @Override
    public String echo(String string) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.echo(string);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * MOVE key db
     * 
     * 将当前数据库的 key 移动到给定的数据库 db 当中。
     * 
     * 如果当前数据库(源数据库)和给定数据库(目标数据库)有相同名字的给定 key ，或者 key 不存在于当前数据库，那么 MOVE 没有任何效果。
     * 
     * 因此，也可以利用这一特性，将 MOVE 当作锁(locking)原语(primitive)。
     * 
     * 可用版本：
     * >= 1.0.0
     * 时间复杂度：
     * O(1)
     * 返回值：
     * 移动成功返回 1 ，失败则返回 0 。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/key/move.html
     */
    @Override
    public Long move(String key, int dbIndex) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.move(key, dbIndex);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * BITCOUNT key [start] [end]
     * 
     * 计算给定字符串中，被设置为 1 的比特位的数量。
     * 
     * 一般情况下，给定的整个字符串都会被进行计数，通过指定额外的 start 或 end 参数，可以让计数只在特定的位上进行。
     * 
     * start 和 end 参数的设置和 GETRANGE 命令类似，都可以使用负数值：比如 -1 表示最后一个位，而 -2 表示倒数第二个位，以此类推。
     * 
     * 不存在的 key 被当成是空字符串来处理，因此对一个不存在的 key 进行 BITCOUNT 操作，结果为 0 。
     * 
     * 可用版本：
     * >= 2.6.0
     * 时间复杂度：
     * O(N)
     * 返回值：
     * 被设置为 1 的位的数量。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/bitcount.html
     */
    @Override
    public Long bitcount(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.bitcount(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * BITCOUNT key [start] [end]
     * 
     * 计算给定字符串中，被设置为 1 的比特位的数量。
     * 
     * 一般情况下，给定的整个字符串都会被进行计数，通过指定额外的 start 或 end 参数，可以让计数只在特定的位上进行。
     * 
     * start 和 end 参数的设置和 GETRANGE 命令类似，都可以使用负数值：比如 -1 表示最后一个位，而 -2 表示倒数第二个位，以此类推。
     * 
     * 不存在的 key 被当成是空字符串来处理，因此对一个不存在的 key 进行 BITCOUNT 操作，结果为 0 。
     * 
     * 可用版本：
     * >= 2.6.0
     * 时间复杂度：
     * O(N)
     * 返回值：
     * 被设置为 1 的位的数量。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/string/bitcount.html
     */
    @Override
    public Long bitcount(String key, long start, long end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.bitcount(key, start, end);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * HSCAN key cursor [MATCH pattern] [COUNT count]
     * 
     * 具体信息请参考 SCAN 命令。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/key/scan.html#scan
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public ScanResult<Entry<String, String>> hscan(String key, int cursor) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hscan(key, cursor);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SSCAN key cursor [MATCH pattern] [COUNT count]
     * 
     * 详细信息请参考 SCAN 命令。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/key/scan.html#scan
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public ScanResult<String> sscan(String key, int cursor) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.sscan(key, cursor);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZSCAN
     * ZSCAN key cursor [MATCH pattern] [COUNT count]
     * 
     * 详细信息请参考 SCAN 命令。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/key/scan.html#scan
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public ScanResult<Tuple> zscan(String key, int cursor) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zscan(key, cursor);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * HSCAN key cursor [MATCH pattern] [COUNT count]
     * 
     * 具体信息请参考 SCAN 命令。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/key/scan.html
     */
    @Override
    public ScanResult<Entry<String, String>> hscan(String key, String cursor) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hscan(key, cursor);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * SSCAN key cursor [MATCH pattern] [COUNT count]
     * 
     * 详细信息请参考 SCAN 命令。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/key/scan.html#scan
     */
    @Override
    public ScanResult<String> sscan(String key, String cursor) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.sscan(key, cursor);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * ZSCAN
     * ZSCAN key cursor [MATCH pattern] [COUNT count]
     * 
     * 详细信息请参考 SCAN 命令。
     * </pre>
     * 
     * @see http://redis.readthedocs.org/en/latest/key/scan.html#scan
     */
    @Override
    public ScanResult<Tuple> zscan(String key, String cursor) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zscan(key, cursor);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * PFADD key element [element ...]
     * Related commands
     * 
     * PFADD
     * PFCOUNT
     * PFMERGE
     * Available since 2.8.9.
     * Time complexity: O(1) to add every element.
     * Adds all the element arguments to the HyperLogLog data structure stored at the variable name specified as first argument.
     * As a side effect of this command the HyperLogLog internals may be updated to reflect a different estimation of the number of unique items added so far (the cardinality of the set).
     * If the approximated cardinality estimated by the HyperLogLog changed after executing the command, PFADD returns 1, otherwise 0 is returned. The command automatically creates an empty HyperLogLog structure (that is, a Redis String of a specified length and with a given encoding) if the specified key does not exist.
     * To call the command without elements but just the variable name is valid, this will result into no operation performed if the variable already exists, or just the creation of the data structure if the key does not exist (in the latter case 1 is returned).
     * For an introduction to HyperLogLog data structure check the PFCOUNT command page.
     * Return value
     * Integer reply, specifically:
     * 1 if at least 1 HyperLogLog internal register was altered. 0 otherwise.
     * </pre>
     * 
     * @see http://redis.io/commands/pfadd
     */
    @Override
    public Long pfadd(String key, String... elements) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.pfadd(key, elements);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * PFCOUNT key [key ...]
     * Related commands
     * 
     * PFADD
     * PFCOUNT
     * PFMERGE
     * Available since 2.8.9.
     * Time complexity: O(1) with every small average constant times when called with a single key. O(N) with N being the number of keys, and much bigger constant times, when called with multiple keys.
     * When called with a single key, returns the approximated cardinality computed by the HyperLogLog data structure stored at the specified variable, which is 0 if the variable does not exist.
     * When called with multiple keys, returns the approximated cardinality of the union of the HyperLogLogs passed, by internally merging the HyperLogLogs stored at the provided keys into a temporary hyperLogLog.
     * The HyperLogLog data structure can be used in order to count unique elements in a set using just a small constant amount of memory, specifically 12k bytes for every HyperLogLog (plus a few bytes for the key itself).
     * The returned cardinality of the observed set is not exact, but approximated with a standard error of 0.81%.
     * For example in order to take the count of all the unique search queries performed in a day, a program needs to call PFADD every time a query is processed. The estimated number of unique queries can be retrieved with PFCOUNT at any time.
     * Note: as a side effect of calling this function, it is possible that the HyperLogLog is modified, since the last 8 bytes encode the latest computed cardinality for caching purposes. So PFCOUNT is technically a write command.
     * Return value
     * Integer reply, specifically:
     * The approximated number of unique elements observed via PFADD.
     * </pre>
     * 
     * @see http://redis.io/commands/pfcount
     */
    @Override
    public long pfcount(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.pfcount(key);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * 以pipeline方式发送命令
     * 当有大量数据需要插入的时候，使用pipeline的效率会非常高！
     * 此方法只提供命令发送，不对redis回包返回或处理。
     * 
     * 使用示例：
     *         jedisTemplate.pipelined(new RedisPipelineContent() {
     * 
     *             @Override
     *             public void pipelined(Pipeline p) {
     *                 p.set("foo1", "test");
     *                 p.set("foo2", "test");
     *             }
     *         });
     * </pre>
     * 
     * @param rpc
     */
    public void pipelined(RedisPipelineContent rpc) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            Pipeline p = jedis.pipelined();
            rpc.pipelined(p);
            p.sync();
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * <pre>
     * 以事务方式批量执行redis命令。
     * 当对数据一致性有特别高要求时，需要使用事务来改变状态。
     * 此方法只提供命令发送，不对redis回包返回或处理。
     * 使用示例：
     *         jedisTemplate.transaction(new RedisTransactionContent() {
     * 
     *             @Override
     *             public void transaction(Transaction t) {
     *                 t.set("foo1", "test");
     *                 t.set("foo2", "test");
     *             }
     *         });
     * </pre>
     * 
     * @param rtc
     */
    public void transaction(RedisTransactionContent rtc) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            Transaction t = jedis.multi();
            rtc.transaction(t);
            t.exec();
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    public static abstract class RedisPipelineContent {

        public abstract void pipelined(Pipeline p);
    }

    public static abstract class RedisTransactionContent {

        public abstract void transaction(Transaction t);
    }

}
