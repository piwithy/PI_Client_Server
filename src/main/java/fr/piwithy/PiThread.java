package fr.piwithy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramSocket;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

public class PiThread implements Callable<Integer> {

    private Logger logger;

    private int nIter,tID;



    public PiThread(int nIter, int tID) {
        this.nIter = nIter;
        this.tID = tID;
        this.logger= LogManager.getLogger("PiDart Thread " + tID);
    }

    public int piDarts(){
        int count=0;
        double x,y,z;
        for(int i=0;i<nIter;i++){
            x= ThreadLocalRandom.current().nextDouble(0,1);
            y= ThreadLocalRandom.current().nextDouble(0,1);
            z=x*x+y*y;
            if(z<=1) count++;
        }
        return count;
    }

    @Override
    public Integer call() throws Exception {
        logger.debug("This is Thread: " +this.tID+" ; Working on "+ this.nIter + " iteration");
        Instant st = Instant.now();
        int cnt = piDarts();
        Instant end = Instant.now();
        long time_ms= Duration.between(st, end).toMillis();
        logger.debug("Computing Finished: " + cnt+ " duration " + time_ms+ "ms");
        return cnt;
    }
}
