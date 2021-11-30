package io.castled;

import com.google.inject.Inject;
import io.castled.jarvis.scheduler.JarvisScheduler;
import io.dropwizard.lifecycle.Managed;

public class CastledLifecycleManager implements Managed {

    private final JarvisScheduler jarvisScheduler;

    @Inject
    public CastledLifecycleManager(JarvisScheduler jarvisScheduler) {
        this.jarvisScheduler = jarvisScheduler;
    }

    @Override
    public void start() throws Exception {
        this.jarvisScheduler.startScheduler();
    }

    @Override
    public void stop() throws Exception {
        this.jarvisScheduler.close();
    }
}
