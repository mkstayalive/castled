package io.castled.jarvis.taskmanager.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import redis.clients.jedis.JedisPool;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JarvisTaskClientConfig {
    private JedisPool jedisPool;
    private Jdbi jdbi;
    private JarvisKafkaConfig jarvisKafkaConfig;
    private List<TaskGroup> taskGroups;
    private int priorityCoolDownMins;
}
