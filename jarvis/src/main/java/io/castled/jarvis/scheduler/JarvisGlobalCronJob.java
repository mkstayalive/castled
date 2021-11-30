package io.castled.jarvis.scheduler;

import org.quartz.Job;

//assuming global jobs are all cron jobs for now
public interface JarvisGlobalCronJob extends Job {

    default String getCronExpression() {
        return null;
    }

}
