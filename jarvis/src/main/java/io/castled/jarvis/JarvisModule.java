package io.castled.jarvis;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.castled.jarvis.scheduler.JarvisGlobalCronJob;
import io.castled.jarvis.taskmanager.scheduledjobs.JarvisTaskRefreshJob;
import io.castled.jarvis.taskmanager.scheduledjobs.JarvisTaskRetryJob;

public class JarvisModule extends AbstractModule {

    @Override
    protected void configure() {
        bindGlobalScheduledJobs();
    }

    private void bindGlobalScheduledJobs() {
        Multibinder<JarvisGlobalCronJob> multiBinder = Multibinder.newSetBinder(binder(), JarvisGlobalCronJob.class);
        multiBinder.addBinding().to(JarvisTaskRefreshJob.class);
        multiBinder.addBinding().to(JarvisTaskRetryJob.class);
    }

}
