package io.castled.jarvis.scheduler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.castled.jarvis.scheduler.models.JarvisScheduledJob;
import io.castled.jarvis.scheduler.models.JarvisSchedulerConfig;
import io.castled.jarvis.taskmanager.exceptions.JarvisException;
import io.castled.jarvis.taskmanager.exceptions.JarvisRuntimeException;
import io.castled.models.jobschedule.CronJobSchedule;
import io.castled.models.jobschedule.FrequencyJobSchedule;
import io.castled.models.jobschedule.JobSchedule;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.*;

@Slf4j
@Singleton
public class JarvisScheduler implements AutoCloseable {
    private final Set<JarvisGlobalCronJob> globalScheduledJobs;
    private final Scheduler scheduler;
    private final Injector injector;
    private final JarvisSchedulerConfig jarvisSchedulerConfig;

    //assuming all global jobs are cron based for now
    @Inject
    public JarvisScheduler(Set<JarvisGlobalCronJob> globalScheduledJobs, JarvisSchedulerConfig jarvisSchedulerConfig,
                           Injector injector) throws Exception {
        this.globalScheduledJobs = globalScheduledJobs;
        this.scheduler = new StdSchedulerFactory(getQuartzProperties(jarvisSchedulerConfig.getQuartzConfig())).getScheduler();
        this.injector = injector;
        this.jarvisSchedulerConfig = jarvisSchedulerConfig;
    }

    private Properties getQuartzProperties(Map<String, String> quartzConfig) {
        Properties properties = new Properties();
        quartzConfig.forEach(properties::setProperty);
        return properties;
    }

    public void startScheduler() throws Exception {
        scheduler.setJobFactory(injector.getInstance(JarvisJobFactory.class));
        Set<JobKey> existingJobKeys = this.scheduler.getJobKeys(GroupMatcher.groupEquals(JarvisSchedulerConstants.GLOBAL_JOB_GROUP));
        Map<JobDetail, Set<? extends Trigger>> jobAndTriggers = Maps.newHashMap();
        for (JarvisGlobalCronJob jarvisCronJob : globalScheduledJobs) {
            String cronExpression = getCronExpression(jarvisCronJob);
            if (cronExpression == null) {
                log.info("Skipping job schedule for job {}", jarvisCronJob.getClass().getCanonicalName());
                continue;
            }
            JobKey jobKey = JobKey.jobKey(jarvisCronJob.getClass().getCanonicalName(), JarvisSchedulerConstants.GLOBAL_JOB_GROUP);
            JobDetail jobDetail = JobBuilder.newJob(jarvisCronJob.getClass()).withIdentity(jobKey).build();
            jobAndTriggers.put(jobDetail, Collections.singleton(buildTrigger(jarvisCronJob, cronExpression)));
            existingJobKeys.remove(jobKey);
        }
        scheduler.scheduleJobs(jobAndTriggers, true);
        scheduler.deleteJobs(Lists.newArrayList(existingJobKeys));
        scheduler.start();
    }

    public void scheduleJob(JarvisScheduledJob jarvisScheduledJob) throws SchedulerException {
        JobDataMap jobDataMap = Optional.ofNullable(jarvisScheduledJob.getJobParams()).map(JobDataMap::new).orElse(null);
        Trigger trigger = buildTrigger(jarvisScheduledJob.getJobKey(),
                jarvisScheduledJob.getJobSchedule(), jarvisScheduledJob.getStartTs());
        JobDetail jobDetail = JobBuilder.newJob(jarvisScheduledJob.getJobClazz())
                .withIdentity(jarvisScheduledJob.getJobKey()).setJobData(jobDataMap).build();
        this.scheduler.scheduleJob(jobDetail, trigger);
    }

    public void deleteJob(JobKey jobKey) throws SchedulerException {
        this.scheduler.deleteJob(jobKey);
    }

    public void unScheduleJob(JobKey jobKey) throws SchedulerException {
        this.scheduler.unscheduleJob(TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup()));
    }

    private String getCronExpression(JarvisGlobalCronJob jarvisScheduledJob) {
        if (jarvisSchedulerConfig.getScheduleOverrides().containsKey(jarvisScheduledJob.getClass().getCanonicalName())) {
            return jarvisSchedulerConfig.getScheduleOverrides().get(jarvisScheduledJob.getClass().getCanonicalName());
        }
        return jarvisScheduledJob.getCronExpression();
    }

    public Trigger buildTrigger(JarvisGlobalCronJob jarvisGlobalCronJob, String cronExpression) {
        TriggerKey triggerKey = TriggerKey.triggerKey(jarvisGlobalCronJob.getClass().getCanonicalName(),
                JarvisSchedulerConstants.GLOBAL_TRIGGER_GROUP);
        return TriggerBuilder.newTrigger().withIdentity(triggerKey)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
    }


    public Trigger buildTrigger(JobKey jobKey, JobSchedule jobSchedule, Date startTs) {
        return TriggerBuilder.newTrigger().withSchedule(buildSchedule(jobSchedule))
                .withIdentity(TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup()))
                .startAt(Optional.ofNullable(startTs).orElse(new Date())).build();
    }

    private ScheduleBuilder<? extends Trigger> buildSchedule(JobSchedule jobSchedule) {
        switch (jobSchedule.getType()) {
            case FREQUENCY:
                FrequencyJobSchedule frequencyJobSchedule = (FrequencyJobSchedule) jobSchedule;
                return SimpleScheduleBuilder.repeatSecondlyForever(frequencyJobSchedule.getFrequency());
            case CRON:
                CronJobSchedule cronJobSchedule = (CronJobSchedule) jobSchedule;
                return CronScheduleBuilder.cronSchedule(cronJobSchedule.getCronExpression());
            default:
                throw new JarvisRuntimeException("Unknown schedule type " + jobSchedule.getType());
        }
    }

    @Override
    public void close() throws Exception {
        this.scheduler.shutdown();
    }
}
