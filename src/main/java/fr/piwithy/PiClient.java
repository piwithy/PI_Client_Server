package fr.piwithy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.System.exit;

public class PiClient implements Runnable {

    private DatagramSocket socket;
    private InetAddress address;
    private int nIter;
    public static int respCnt = 0;
    public static ArrayList<Double> piList;

    public static void endConnection(InetAddress server) throws IOException {
        byte[] buff = new byte[256];
        DatagramSocket socket = new DatagramSocket();
        ByteBuffer.wrap(buff).putInt(-1);
        DatagramPacket packet = new DatagramPacket(buff, buff.length, server, 4445);
        socket.send(packet);
        socket.close();
    }

    public static synchronized void incResponse() {
        respCnt++;
    }

    public static int getRespCnt() {
        return respCnt;
    }

    public static final Logger LOGGER = LogManager.getLogger(PiClient.class);

    public PiClient(int nIter, InetAddress addr) {
        piList = new ArrayList<>();
        this.nIter = nIter;
        try {
            this.socket = new DatagramSocket(null);

        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.address = addr;
    }

    public void run() {
        LOGGER.info("Requesting Pi w/" + nIter + " Iterations");
        try {
            LOGGER.info("Pi w/" + nIter + " Iteration: Pi=" + requestPi(nIter));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.socket.close();
    }

    public double requestPi(int nIter) throws InterruptedException {
        byte[] buff = new byte[256];
        ByteBuffer.wrap(buff).putInt(nIter);
        DatagramPacket packet = new DatagramPacket(buff, buff.length, address, ClientConfig.getInstance().getServerPort());
        try {
            this.socket.setSoTimeout(ClientConfig.getInstance().getServerTimeout()*1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] buffIn = new byte[256];
        DatagramPacket packet_rcv = new DatagramPacket(buffIn, buffIn.length);
        try {
            this.socket.receive(packet_rcv);
            incResponse();
        } catch (SocketTimeoutException e) {
            LOGGER.fatal("Time Out!");
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        double pi = ByteBuffer.wrap(packet_rcv.getData()).getDouble();
        addPi(pi);
        return pi;
    }

    public static synchronized void addPi(double pi) {
        if (pi != 0)
            piList.add(pi);
    }

    public static double getPiMean() {
        double ttl = 0;
        for (double pi : piList) {
            ttl += pi;
        }
        return ttl / piList.size();
    }

    public static void main(String[] args) {
        ClientConfig clientConfig = ClientConfig.getInstance();
        int repeat = 20;
        if (args.length > 0)
            repeat = Integer.parseInt(args[0]);
        ExecutorService executorService = Executors.newFixedThreadPool(repeat);
        ArrayList<Runnable> requests = new ArrayList<>();
            LOGGER.info("Server Address: " + clientConfig.getServerAddress());
        for (int i = 0; i < repeat; i++) {
            requests.add(new PiClient(ThreadLocalRandom.current().nextInt(clientConfig.getMinIteration(), clientConfig.getMaxIteration()), clientConfig.getServerAddress()));
        }
        for (Runnable request : requests) {
            executorService.submit(request);
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            endConnection(ClientConfig.getInstance().getServerAddress());
        } catch (IOException e) {
            LOGGER.warn("Error while requesting Server Stop");
        }
        LOGGER.info(getRespCnt() + " Responses");
        LOGGER.info("TimeOuts: " + ((repeat) - getRespCnt()));
        LOGGER.info("Pi mean: " + getPiMean());
    }

}
