package com.josue.simpletow;

import org.xnio.OptionMap;
import org.xnio.Options;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Josue on 01/02/2017.
 */
public class Config {

    int port = 8080;
    String bindAddress = "0.0.0.0";

    final OptionMap.Builder optionBuilder = OptionMap.builder();
    boolean httpTracer;
    ThreadPoolExecutor threadPoolExecutor;
    List<Interceptor> interceptors = new LinkedList<>();

//    //SSR
//    public enum DiscoveryMode {
//        NONE, CLIENT_ONLY, SERVICE_ONY, SERVICE_AND_CLIENT
//    }

//    private DiscoveryMode discoveryMode = DiscoveryMode.NONE;
//    private String appName;

    public Config() {
        optionBuilder.set(Options.TCP_NODELAY, true);
        int processors = Runtime.getRuntime().availableProcessors();
        this.coreThreads(processors * 2);
        this.ioThreads(processors);


        this.threadPoolExecutor = new ThreadPoolExecutor(2, 5, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
    }

    public Config port(int port) {
        this.port = port;
        return this;
    }

    public Config bindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
        return this;
    }

//    public Config threadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
//        this.threadPoolExecutor = threadPoolExecutor;
//        return this;
//    }

    //TODO implement
//    public Config enableDiscovery(String appName, DiscoveryMode discoveryMode) {
//        if (appName == null || appName.trim().isEmpty()) {
//            throw new IllegalArgumentException("appName cannot be null or empty");
//        }
//        this.appName = appName;
//        this.discoveryMode = discoveryMode;
//
//        return this;
//    }

//    public DiscoveryMode getDiscoveryMode() {
//        return discoveryMode;
//    }

//    public String getAppName() {
//        return appName;
//    }


    public int getPort() {
        return port;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public Config ioThreads(int ioThreads) {
        optionBuilder.set(Options.WORKER_IO_THREADS, ioThreads);
        return this;
    }

    public Config coreThreads(int coreThreads) {
        optionBuilder.set(Options.WORKER_TASK_CORE_THREADS, coreThreads);
        return this;
    }

    public Config maxThreads(int maxThreads) {
        optionBuilder.set(Options.WORKER_TASK_MAX_THREADS, maxThreads);
        return this;
    }

    public Config tcpNoDeplay(boolean tcpNoDelay) {
        optionBuilder.set(Options.TCP_NODELAY, tcpNoDelay);
        return this;
    }

    public Config httpTracer(boolean httpTracer) {
        this.httpTracer = httpTracer;
        return this;
    }

    public OptionMap.Builder xnioOptions() {
        return optionBuilder;
    }

    public Config appExecutor(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
        return this;
    }

    public Config addInterceptor(Interceptor handlerInterceptor) {
        this.interceptors.add(handlerInterceptor);
        return this;
    }

}
