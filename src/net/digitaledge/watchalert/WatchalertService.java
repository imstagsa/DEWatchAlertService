package net.digitaledge.watchalert;


public class WatchalertService  {

	private Thread thread;
	private WatchAlertSettings settings;
	public static void main(String[] args)
	{
		
		WatchalertService watchalertService = new WatchalertService();
		watchalertService.doStart();
	}
	
    public WatchalertService() {
    	settings = new WatchAlertSettings();
    	doStart();
    }

    protected void doStart() {
    	try{
    		System.out.println("START watchalertService.");
    		WatchAlertsWorker worker = new WatchAlertsWorker(settings);
    		System.out.println("watchalertService  starting.");
    		thread = new Thread(worker);
    		thread.start();
    		System.out.println("watchalertService started.");
    	} catch (Exception e) {System.out.println(e.toString());}
    }
}
