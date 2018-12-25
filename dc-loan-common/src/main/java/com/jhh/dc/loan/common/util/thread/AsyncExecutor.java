package com.jhh.dc.loan.common.util.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jhh.dc.loan.common.util.thread.runner.AbstractRunner;

import java.util.concurrent.*;

/**
 * 异步执行Executor
 * @author xingmin
 */
public class AsyncExecutor {
    private static final ExecutorService EXECUTOR_POOL = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2, 50, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setNameFormat("asyncExecutor-default-pool-%d").build());

    private static final ExecutorService EXECUTOR_SINGLE_POOL = Executors.newFixedThreadPool(1);

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_POOL = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2, new ThreadFactoryBuilder().setNameFormat("asyncExecutor-schedule-pool-%d").build());

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SINGLE = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("asyncExecutor-sinle").build());


    /**
     * 使用默认线程池执行
     * @param runner the task to execute
     */
    public static void execute(AbstractRunner runner) {
        EXECUTOR_POOL.execute(runner);
    }

    /**
     * 使用默认线程池执行
     * @param runner the task to execute
     */
    public static void syncExecute(AbstractRunner runner) {
        EXECUTOR_SINGLE_POOL.execute(runner);
    }

    /**
     * 使用scheduledThreadPool delay执行
     * @param runner the task to execute
     * @param delayTime the time from now to delay execution
     * @param unit the time unit of the delay parameter
     */
    public static void delayExecute(AbstractRunner runner, int delayTime, TimeUnit unit) {
        SCHEDULED_EXECUTOR_POOL.schedule(runner, delayTime, unit);
    }

    public static void delaySyncExecute(AbstractRunner runner, int delayTime, TimeUnit unit) {
        SCHEDULED_EXECUTOR_SINGLE.schedule(runner, delayTime, unit);
    }

    public static void destroy() {
        EXECUTOR_POOL.shutdown();
        SCHEDULED_EXECUTOR_POOL.shutdown();
        SCHEDULED_EXECUTOR_SINGLE.shutdown();
        EXECUTOR_SINGLE_POOL.shutdown();
    }



}
