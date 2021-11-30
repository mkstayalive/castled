package io.castled.jarvis.taskmanager;

import net.greghaines.jesque.Config;
import net.greghaines.jesque.Job;
import net.greghaines.jesque.client.ClientPoolImpl;
import net.greghaines.jesque.json.ObjectMapperFactory;
import net.greghaines.jesque.utils.JesqueUtils;
import net.greghaines.jesque.utils.PoolUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

import java.util.List;

import static net.greghaines.jesque.utils.ResqueConstants.QUEUE;
import static net.greghaines.jesque.utils.ResqueConstants.QUEUES;

public class JarvisClientPool extends ClientPoolImpl {

    private final JedisPool jedisPool;

    public JarvisClientPool(Config config, JedisPool jedisPool) {
        super(config, jedisPool);
        this.jedisPool = jedisPool;
    }

    public boolean moveJob(String fromQueue, String toQueue, Job job, long future) {
        try {

            String jobJson = ObjectMapperFactory.get().writeValueAsString(job);
            return PoolUtils.doWorkInPool(this.jedisPool, jedis -> {
                long removed = jedis.zrem(JesqueUtils.createKey(getNamespace(), QUEUE, fromQueue), jobJson);
                Transaction txn = jedis.multi();
                if (removed > 0) {
                    txn.sadd(JesqueUtils.createKey(getNamespace(), QUEUES), toQueue);
                    txn.zadd(JesqueUtils.createKey(getNamespace(), QUEUE, toQueue), future, jobJson);
                }
                txn.exec();
                return removed > 0;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delayedBatchEnqueue(String queue, List<Job> jobs, long future) {
        try {
            PoolUtils.doWorkInPool(this.jedisPool, (PoolUtils.PoolWork<Jedis, Void>) jedis -> {
                Pipeline pipeline = jedis.pipelined();
                jedis.sadd(JesqueUtils.createKey(getNamespace(), QUEUES), queue);
                for (Job job : jobs) {
                    jedis.zadd(JesqueUtils.createKey(getNamespace(), QUEUE, queue), future, ObjectMapperFactory.get().writeValueAsString(job));
                }
                pipeline.sync();
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean jobExists(String queue, Job job) {
        try {
            return PoolUtils.doWorkInPool(this.jedisPool, jedis ->
                    (jedis.zscore(JesqueUtils.createKey(getNamespace(), QUEUE, queue),
                            ObjectMapperFactory.get().writeValueAsString(job)) != null));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
