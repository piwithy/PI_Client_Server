package fr.piwithy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PiThreadTest {

    int nThread;
    int nIter;

    @BeforeEach
    void setUp(){
        nThread=8;
        nIter=1000;
    }


    @Test
    void call() {
        ExecutorService executorService = Executors.newFixedThreadPool(nThread);
        ArrayList<Callable<Integer>> piThreads = new ArrayList<>();
        int count;
        double pi = 0;
        //PiServer.LOGGER.debug("Starting Pi computing w/" + nIter + " iteration on: " + nThread + " Threads");
        for (int i = 0; i < nThread; i++) {
            piThreads.add(new PiThread(nIter / nThread, i + 1));

        }
        try {
            count = executorService.invokeAll(piThreads).stream().map(integerFuture -> {
                try {
                    return integerFuture.get();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).mapToInt(Integer::intValue).sum();
            pi = (double) count / nIter * 4;

        } catch (
                Exception e) {
            e.printStackTrace();
        }
        try {
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (!executorService.isTerminated())
                PiServer.LOGGER.fatal("Canceling non-finished task!");
            executorService.shutdownNow();
        }
        assertTrue(pi>3 && pi<4);
    }
}