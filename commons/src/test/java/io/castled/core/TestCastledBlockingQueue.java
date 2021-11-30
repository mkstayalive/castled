package io.castled.core;

import io.castled.utils.TimeUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TestCastledBlockingQueue {

    @Test
    public void testCastledBlockingQueue() throws Exception {
        AtomicLong atomicLong = new AtomicLong(0);
        CastledBlockingQueue<String> blockingQueue = new CastledBlockingQueue<>((record) -> {
            atomicLong.incrementAndGet();
        }, 100, 100, false);
        for (int i = 0; i < 100000; i++) {
            blockingQueue.writePayload("castled" + i, 10, TimeUnit.SECONDS);
        }
        blockingQueue.flush(TimeUtils.minutesToMillis(1));
        Assert.assertEquals(atomicLong.get(), 100000);
    }
}