package fr.piwithy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;

public class PiServer{

    private DatagramSocket socket;
    private byte[] buf = new byte[256];
    private ExecutorService piPool;

    public static final Logger LOGGER = LogManager.getLogger(PiServer.class);

    public PiServer(){
        try{
            int port = ServerConfig.getInstance().getPort();
            this.socket=new DatagramSocket(port);
            LOGGER.info("Successfully Opened Socket on Port:" + port);
        }catch (SocketException e){
            e.printStackTrace();
            exit(-1);
        }
        piPool = Executors.newFixedThreadPool(100);
    }

    public void run(){
        LOGGER.info("Waiting for Clients");
        do {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try{
                this.socket.receive(packet);
            }catch (IOException e){
                e.printStackTrace();
                exit(-1);
            }
            int data = ByteBuffer.wrap(packet.getData()).getInt();
            if(data== -1) {
                LOGGER.info("Stopping server as requested by " + packet.getAddress()+":"+packet.getPort());
                break;
            }else {
                LOGGER.info("[" + packet.getAddress() + ":" + packet.getPort() + "] Requested Pi W/" + data + " iteration");
                PiRequest rq = new PiRequest(data, socket, packet.getAddress(), packet.getPort());
                piPool.submit(new PiManager(rq));
            }

        }while(true);
        try{
            piPool.shutdown();
            piPool.awaitTermination(5, TimeUnit.SECONDS);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            if (!piPool.isTerminated())
                PiServer.LOGGER.fatal("Canceling non-finished task!");
            piPool.shutdownNow();
        }
    }

}
