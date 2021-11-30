package io.castled.jarvis.taskmanager;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.castled.jarvis.taskmanager.daos.JarvisTasksDAO;
import io.castled.jarvis.taskmanager.models.TaskGroup;
import io.castled.jarvis.taskmanager.models.JesqueTaskParams;
import io.castled.jarvis.taskmanager.models.TaskPriority;
import io.castled.kafka.producer.CastledKafkaProducer;
import io.castled.utils.ThreadUtils;
import net.greghaines.jesque.ConfigBuilder;
import net.greghaines.jesque.Job;
import net.greghaines.jesque.worker.NextQueueStrategy;
import net.greghaines.jesque.worker.Worker;
import net.greghaines.jesque.worker.WorkerPoolImpl;
import redis.clients.jedis.JedisPool;

import java.io.Closeable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class JesqueTasksClient implements Closeable {

    private final List<Worker> workers = Lists.newArrayList();
    private final ExecutorService workerExecutor;
    private final JarvisClientPool client;
    private static final long PUBLISH_DELAY_MS = 100;

    public JesqueTasksClient(JedisPool jedisPool, JarvisTasksDAO jarvisTasksDAO, TaskGroup taskGroup,
                             CastledKafkaProducer kafkaProducer) {
        List<String> queues = Arrays.stream(TaskPriority.values()).map(TaskPriority::name).collect(Collectors.toList());

        this.workerExecutor = Executors.newFixedThreadPool(taskGroup.getWorkerThreads(),
                new ThreadFactoryBuilder().setNameFormat("jarvis-worker-executor-%d").build());
        for (int i = 0; i < taskGroup.getWorkerThreads(); i++) {
            Worker worker = new WorkerPoolImpl(
                    new ConfigBuilder().withNamespace(taskGroup.getGroup()).build(), queues,
                    new JarvisJobFactory(taskGroup.getTaskExecutors(), jarvisTasksDAO, kafkaProducer), jedisPool,
                    NextQueueStrategy.RESET_TO_HIGHEST_PRIORITY);
            this.workers.add(worker);
            this.workerExecutor.execute(worker);
        }
        this.client = new JarvisClientPool(new ConfigBuilder().withNamespace(taskGroup.getGroup()).build(), jedisPool);
    }

    @Override
    public void close() {
        this.client.end();
        workers.forEach(worker -> worker.end(true));
        ThreadUtils.terminateGracefully(this.workerExecutor, 60);
    }

    public void enqueueTask(JesqueTaskParams jesqueTaskParams) {
        this.client.delayedEnqueue(jesqueTaskParams.getTaskPriority().name(), buildJesqueJob(jesqueTaskParams),
                System.currentTimeMillis() + PUBLISH_DELAY_MS);
    }

    public void batchEnqueueTasks(List<JesqueTaskParams> jesqueTaskParamsList) {
        Map<TaskPriority, List<JesqueTaskParams>> paramsPerPriority = jesqueTaskParamsList.stream()
                .collect(Collectors.groupingBy(JesqueTaskParams::getTaskPriority));
        for (TaskPriority priority : paramsPerPriority.keySet()) {
            List<Job> jobs = paramsPerPriority.get(priority).stream()
                    .map(this::buildJesqueJob).collect(Collectors.toList());
            this.client.delayedBatchEnqueue(priority.name(), jobs, System.currentTimeMillis() + PUBLISH_DELAY_MS);
        }
    }

    public Map<JesqueTaskParams, Boolean> jobExists(List<JesqueTaskParams> jesqueTaskParamsList) {
        Map<JesqueTaskParams, Boolean> result = Maps.newHashMap();
        Map<TaskPriority, List<JesqueTaskParams>> paramsPerPriority = jesqueTaskParamsList.stream()
                .collect(Collectors.groupingBy(JesqueTaskParams::getTaskPriority));
        for (TaskPriority priority : paramsPerPriority.keySet()) {
            for (JesqueTaskParams jesqueTaskParams : paramsPerPriority.get(priority)) {
                result.put(jesqueTaskParams, this.client.jobExists(priority.name(), buildJesqueJob(jesqueTaskParams)));
            }
        }
        return result;
    }


    public TaskPriority prioritiseTask(JesqueTaskParams taskParams) {
        TaskPriority currentPriority = taskParams.getTaskPriority();
        TaskPriority newPriority = TaskPriority.getPriority(currentPriority.getRank() + 1);
        if (newPriority == null) {
            return null;
        }
        if (this.client.moveJob(currentPriority.name(), newPriority.name(), buildJesqueJob(taskParams), System.currentTimeMillis())) {
            return newPriority;
        }
        return null;
    }

    private Job buildJesqueJob(JesqueTaskParams jesqueTaskParams) {
        return new Job(jesqueTaskParams.getTaskType(), ImmutableMap.of(JarvisConstants.ID_FIELD, jesqueTaskParams.getTaskId()));
    }
}
