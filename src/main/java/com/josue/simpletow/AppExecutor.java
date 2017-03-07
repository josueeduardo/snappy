package com.josue.simpletow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by josh on 3/7/17.
 */
public class AppExecutor {

    private static ExecutorService executorService;
    private static ScheduledExecutorService scheduledExecutorService;

   static void init(ThreadPoolExecutor threadPoolExecutor) {
        executorService = threadPoolExecutor;
       scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(threadPoolExecutor.getThreadFactory());
   }

    public static ExecutorService executor() {
        return executorService;
    }

    public static ScheduledExecutorService scheduler() {
        return scheduledExecutorService;
    }

}
