package javaservicedemon.demon;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class JavaService {

	   private static final Logger logger = Logger.getLogger(JavaService.class.toString());
		 
		   private static JavaService 
		       service = new JavaService();
		 
		   private ScheduledExecutorService executor;
		 
		   public static void main(String args[]) {	
		      if(args.length == 1 && "start".equalsIgnoreCase(args[0])) {
		         service.start();
		      }
		      else if(args.length == 1 && "stop".equalsIgnoreCase(args[0])) {
		         service.stop();
		      }
		      else {
		         logger.info("Required param: start or stop");
		      }
		   }
		 
		   public void start() {
		      executor = Executors.newSingleThreadScheduledExecutor();
		      executor.scheduleAtFixedRate(new ScheduledService(), 0, 30, TimeUnit.SECONDS);
		   }
		 
		   public void stop() {
		      if (executor != null) {
		         executor.shutdown();
		      }
		   }
		}
		 
		
