package net.digitaledge.watchalert;

import java.util.logging.Logger;

public class WatchalertService  {

	private Thread thread;
	private WatchAlertSettings settings;
	final static Logger logger = Logger.getLogger("WatchalertService");
	
	public static void main(String[] args)
	{
		WatchalertService watchalertService = new WatchalertService();
		watchalertService.doStart();
	}
	
    public WatchalertService() {
    	settings = new WatchAlertSettings();
    }

    protected void doStart() {
    	try{
    		logger.info("START watchalertService.");
    		WatchAlertsWorker worker = new WatchAlertsWorker(settings);
    		logger.info("watchalertService  starting.");
    		thread = new Thread(worker);
    		thread.start();
    		logger.info("watchalertService started.");
    	} catch (Exception e) {System.out.println(e.toString());}
    }
}
