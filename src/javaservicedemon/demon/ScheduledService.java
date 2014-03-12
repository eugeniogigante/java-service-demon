package javaservicedemon.demon;

import java.util.logging.Logger;

public class ScheduledService implements Runnable {
	 
	   private static final Logger logger = Logger.getLogger(ScheduledService.class.toString());
	 
	    @Override
	    public void run() {
	        logger.info("I'm alive!");
	    }
	}
