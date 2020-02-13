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

import static java.lang.System.exit;

public class PiServer implements Runnable {

    private DatagramSocket socket;
    private int port;
    private boolean running;
    private byte[] buf = new byte[256];
    private ExecutorService piPool;

    public static final Logger LOGGER = LogManager.getLogger(PiServer.class);

    public PiServer(){
        try{
            port = 4445;
            this.socket=new DatagramSocket(port);
            LOGGER.info("Successfully Opened Socket on Port:" +port);
        }catch (SocketException e){
            e.printStackTrace();
            exit(-1);
        }
        piPool = Executors.newFixedThreadPool(100);
    }

    public void run(){
        this.running=true;
        LOGGER.info("Waiting for Clients");
        while (this.running){
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try{
                this.socket.receive(packet);
            }catch (IOException e){
                e.printStackTrace();
                exit(-1);
            }
            int data = ByteBuffer.wrap(packet.getData()).getInt();
            if(data==-1) {
                this.running=false;
                LOGGER.info("Stopping server as requested by " + packet.getAddress()+":"+packet.getPort());
                return;
            }else {
                LOGGER.info("[" + packet.getAddress() + ":" + packet.getPort() + "] Requested Pi W/" + data + " iteration");
                PiRequest rq = new PiRequest(data, socket, packet.getAddress(), packet.getPort());
                piPool.submit(new PiManager(rq));
            }

        }
    }

    public static void main(String[] args){
        Thread t = new Thread(new PiServer());
        t.start();
        try{
            t.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

}
