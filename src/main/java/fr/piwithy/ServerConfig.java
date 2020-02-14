package fr.piwithy;

import java.io.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

public class ServerConfig {
    private int port, nTread;

    private File configFile;

    private static ServerConfig instance=null;

    public static ServerConfig getInstance() {
        if (instance==null) instance= new ServerConfig();
        return instance;
    }

    private ServerConfig(){
        configFile= new File("server.properties");
        if(!configFile.exists())
            this.writeDefaultConfig();
        this.readConfig();
    }

    private void writeDefaultConfig(){
        try (OutputStream output = new FileOutputStream(this.configFile.getPath())){
            Properties properties=new Properties();
            properties.setProperty("server.port", Integer.toString(4445));
            properties.setProperty("server.threads.max", Integer.toString(8));
            Properties tmp = new Properties() {
                @Override
                public synchronized Enumeration<Object> keys() {
                    return Collections.enumeration(new TreeSet<>(super.keySet()));
                }
            };
            tmp.putAll(properties);
            tmp.store(output, null);
        }catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void readConfig(){
        try (InputStream input = new FileInputStream(this.configFile.getPath())){
            Properties properties = new Properties();
            properties.load(input);
            this.port=Integer.parseInt(properties.getProperty("server.port"));
            this.nTread = Integer.parseInt(properties.getProperty("server.threads.max"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public int getPort() {
        return port;
    }

    public int getNTread() {
        return nTread;
    }
}
