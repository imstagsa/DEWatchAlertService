package net.digitaledge.watchalert;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class WatchalertService  {

	private Thread thread;
	private WatchAlertSettings settings;
	private final static String Version = new String("1.0.2");
	final static Logger logger = Logger.getLogger("WatchalertService");
	
	public static void main(String[] args)
	{
		String log4jConfPath = "log4j.properties";
		PropertyConfigurator.configure(log4jConfPath);
		logger.info("Starting WatchalertService Version " + Version);
		WatchalertService watchalertService = new WatchalertService();
		watchalertService.doStart();
	}
	
    public WatchalertService() {
    	
    }

    protected void doStart() {
    	try{
    		WatchAlertsWorker worker = new WatchAlertsWorker(settings);
    		thread = new Thread(worker);
    		thread.start();
    		logger.info("watchalertService has started.");
    	} catch (Exception e) {logger.debug("");}
    }
}
