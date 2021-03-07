package com.yiu.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListUtil {
    public static <T> List<List<T>> divide(List<T> source, int threadNum) {
        return divide(source, threadNum, ts -> ts);
    }

    public static <T, PACK> List<PACK> divide(List<T> source, int threadNum, Function<List<T>, PACK> doPack) {

        if (source.size() < threadNum) {
            threadNum = source.size();
        }

        List<PACK> result = new ArrayList<PACK>();
        int remaider = source.size() % threadNum;
        int number = source.size() / threadNum;
        int offset = 0;
        for (int i = 0; i < threadNum; i++) {
            PACK pack = null;
            if (remaider > 0) {
                List<T> value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                pack = doPack.apply(value);
                remaider--;
                offset++;
            } else {
                List<T> value = source.subList(i * number + offset, (i + 1) * number + offset);
                pack = doPack.apply(value);
            }
            result.add(pack);
        }
        return result;
    }

    public static <T, R> List<R> handleByThread(List<T> source, int threadNum, Function<T, R> handle) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threadNum);
        List<R> rs = handleByThread(source, threadNum, handle, pool);
        pool.shutdown();
        return rs;
    }

    public static <T, R> List<R> handleByThread(List<T> source, int threadNum, Function<T, R> handle, ExecutorService pool) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(source.size());

        class SubThread implements Runnable {
            private List<T> subList;
            private List<R> retList;

            private SubThread(List<T> subList) {
                this.subList = subList;
                this.retList = new ArrayList<R>();
            }

            @Override
            public void run() {
                for (int i = 0; i < subList.size(); i++) {
                    try {
                        T t = subList.get(i);
                        R r = handle.apply(t);
                        retList.add(r);
                    } catch (Exception e) {
                        // do something
                        throw new RuntimeException(e);
                    }finally {
                        latch.countDown();
                    }
                }
            }
        }

        List<SubThread> divide = divide(source, threadNum, SubThread::new);
        System.out.println("线程数量: "+ divide.size());
        for (int i = 0; i < divide.size(); i++) {
            SubThread s = divide.get(i);
            pool.submit(s);
        }
        latch.await();
        List<R> rs = new ArrayList<>();
        for (int i = 0; i < divide.size(); i++) {
            SubThread s = divide.get(i);
            rs.addAll(s.retList);
        }
        return rs;
    }

    public static void main(String[] args) {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 500000; i++) {
            strings.add(i + "");
        }

        Function<String, String> function = s -> "我是第"+s+"个";

        Runnable dup = () -> {
            long s = System.currentTimeMillis();
            List<String> strings1 = null;
            try {
                strings1 = ListUtil.handleByThread(strings, 5, function);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long e = System.currentTimeMillis();
            System.out.println("多线程耗时：" + (e - s));
//            System.out.println(strings1);
        };

        Runnable sin = () -> {
            long s1 = System.currentTimeMillis();
            List<String> collect = strings.stream().map(function).collect(Collectors.toList());
            long e1 = System.currentTimeMillis();
            System.out.println("单线程耗时：" + (e1 - s1));
//            System.out.println(collect);
        };

        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.submit(dup);
        pool.submit(sin);
        pool.shutdown();
    }

}
