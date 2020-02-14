package fr.piwithy;

import java.io.*;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

import static java.lang.System.exit;

public class ClientConfig {
    private InetAddress serverAddress;
    private int serverPort, serverTimeout;
    private int requestCount;
    private int minIteration, maxIteration;
    private File configFile;

    private static ClientConfig instance = null;

    public static ClientConfig getInstance() {
        if (instance == null)
            instance = new ClientConfig();
        return instance;
    }

    private ClientConfig() {
        this.configFile = new File("client.properties");
        if (!configFile.exists()) {
            this.writeDefaultConfig();
        }
        this.readConfig();
    }

    private void readConfig() {
        try (InputStream input = new FileInputStream(this.configFile.getPath())) {
            Properties properties = new Properties();
            properties.load(input);
            this.maxIteration = Integer.parseInt(properties.getProperty("iteration.max"));
            this.minIteration = Integer.parseInt(properties.getProperty("iteration.min"));
            this.requestCount = Integer.parseInt(properties.getProperty("request.count"));
            this.serverTimeout = Integer.parseInt(properties.getProperty("server.timeout"));
            this.serverAddress = InetAddress.getByName(properties.getProperty("server.address"));
            this.serverPort = Integer.parseInt(properties.getProperty("server.port"));

        } catch (IOException e) {
            e.printStackTrace();
            exit(-1);
        }
    }

    private void writeDefaultConfig() {
        PiClient.LOGGER.debug("Writing new Config file w/ default parameters");
        try (OutputStream output = new FileOutputStream(this.configFile.getPath())) {
            Properties properties = new Properties();
            properties.setProperty("iteration.max", Integer.toString(1000000));
            properties.setProperty("iteration.min", Integer.toString(100000));
            properties.setProperty("request.count", Integer.toString(20));
            properties.setProperty("server.timeout", Integer.toString(120));
            properties.setProperty("server.address", "127.0.0.1");
            properties.setProperty("server.port", Integer.toString(4445));
            Properties tmp = new Properties() {
                @Override
                public synchronized Enumeration<Object> keys() {
                    return Collections.enumeration(new TreeSet<>(super.keySet()));
                }
            };
            tmp.putAll(properties);
            tmp.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
            exit(-1);
        }
    }

    public InetAddress getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getServerTimeout() {
        return serverTimeout;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public int getMinIteration() {
        return minIteration;
    }

    public int getMaxIteration() {
        return maxIteration;
    }
}
