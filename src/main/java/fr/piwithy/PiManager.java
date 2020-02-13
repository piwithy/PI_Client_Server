package fr.piwithy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PiManager implements Runnable {

    private int nIter, nThread;
    private PiRequest request;

    public PiManager(PiRequest request) {
        this.nIter = request.nIter;
        this.request= request;
        nThread = 8;
    }




    @Override
    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(nThread);
        ArrayList<Callable<Integer>> piThreads = new ArrayList<>();
        Instant start = Instant.now();
        int count;
        double pi = 0;
        PiServer.LOGGER.debug("Starting Pi computing w/" + nIter + " iteration on: " + nThread + " Threads");
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

        byte[] bufOut= new byte[256];
        ByteBuffer.wrap(bufOut).putDouble(pi);
        DatagramPacket packet = new DatagramPacket(bufOut, bufOut.length, request.address, request.port);
        try {
            request.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Instant end = Instant.now();
        if(Duration.between(start,end).toMillis()>120000) {
            PiServer.LOGGER.fatal("Responded to " + request.address + ":" + request.port + " in " + Duration.between(start, end).toMillis() + "ms #TimedOut");
        }else{
            PiServer.LOGGER.info("Responded to " + request.address + ":" + request.port + " in " + Duration.between(start, end).toMillis() + "ms");
        }
    }
}
