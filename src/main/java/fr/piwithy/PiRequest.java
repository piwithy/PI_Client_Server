package fr.piwithy;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class PiRequest {
    public final DatagramSocket socket;
    public final InetAddress address;
    public final int port;
    public final int nIter;

    public PiRequest(int nIter, DatagramSocket socket, InetAddress address, int port) {
        this.socket = socket;
        this.address = address;
        this.port=port;
        this.nIter = nIter;
    }

}
