package io.castled.interceptors;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

@Slf4j
public class RetryInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        final Method method = methodInvocation.getMethod();
        final Retry retry = method.getAnnotation(Retry.class);
        int attempt = 0;
        final Class<? extends Throwable>[] types = retry.types();
        while (true) {
            try {
                return methodInvocation.proceed();
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw ex;
            }
            catch (final Exception ex) {
                if(matches(ex.getClass(), retry.ignore()) || !matches(ex.getClass(), types)) {
                    throw ex;
                }
                if(++attempt >= retry.attempts()) {
                    log.warn(String.format("Failed after several retries. [%d] for [%s]", attempt, method.getName()), ex);
                    throw ex;
                }
                if(retry.waitTime() > 0) {
                    Thread.sleep(retry.waitTime());
                }
                log.debug(String.format("Attempt [%d] for [%s] because of [%s]", attempt, method.getName(), ex.getMessage()));
            }
        }
    }

    private boolean matches(final Class<? extends Throwable> thrown, final Class<? extends Throwable>... types) {
        for (final Class<? extends Throwable> type : types) {
            if (type.isAssignableFrom(thrown)) {
                return true;
            }
        }
        return false;
    }
}
