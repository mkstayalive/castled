package io.castled.jarvis.scheduler;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

public class JarvisJobFactory implements JobFactory {

    private final Injector injector;

    @Inject
    public JarvisJobFactory(Injector injector) {
        this.injector = injector;
    }

    @Override
    public Job newJob(TriggerFiredBundle triggerFiredBundle, Scheduler scheduler) {
        JobDetail jobDetail = triggerFiredBundle.getJobDetail();
        return injector.getInstance(jobDetail.getJobClass());
    }
}
