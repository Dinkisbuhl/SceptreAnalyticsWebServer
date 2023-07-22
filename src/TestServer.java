import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Parsing.Concept;
import Parsing.NographSearcher;
import Parsing.Searcher;
import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.FileContextHandler;
import net.freeutils.httpserver.HTTPServer.VirtualHost;

public class TestServer 
{
	public static HTTPServer server;
	public static Thread t;
	
	public static class LocalHostServer extends HTTPServer
	{
		public LocalHostServer()
		{
			super();
		}
		
		public LocalHostServer(int port)
		{
			super(port);
		}
		
		@Override
	    protected ServerSocket createServerSocket() throws IOException {
	        ServerSocket serv = serverSocketFactory.createServerSocket(port,0,InetAddress.getLoopbackAddress());
	        serv.setReuseAddress(true);
	        return serv;
	    }
	}
	
	public static void mainMethod() 
	{
		try
		{
			int port = 9000;
			//HTTPServer server = new HTTPServer(port);
			server = new LocalHostServer(port);
			VirtualHost host = server.getVirtualHost(null);  // default virtual host
			
			RESTHandler rH = new RESTHandler();
			rH.setInstance(server);
			host.addContext("/rest", rH, "GET","POST");
			//host.addContext("/rest", new RESTHandlerHTML(), "GET","POST");
			host.addContext("/", new FileContextHandler(new File("Web")));
			
			
			//if(args != null && args.length>1)
			{
				//if(args[0].toLowerCase().equals("browser") && args[1].toLowerCase().equals("true"))
				{
					spawnBrowserOpen();
				}
			}
			server.start();
			/*
			final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		    executorService.scheduleAtFixedRate(new Runnable() {
		        @Override
		        public void run() {
		            printTest();
		        }
		    }, 0, 1, TimeUnit.SECONDS);
		    */
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * Method to open the browser to localhost page
	 */
	public static void spawnBrowserOpen()
	{
		Runnable r = new Runnable() 
		{
			public void run()
			{
				//System.out.println("Attempting to open browser");
				try 
				{
					Thread.sleep(1100);
					Desktop.getDesktop().browse(new URI("http://localhost:9000/testJQandMap.html"));
				} catch(Exception ex) {};
				
			}
		};
		
		t = new Thread(r);
		t.start();
		System.out.println(t.isAlive());
	}
	
	public static void printTest()
	{
		//System.out.println(t.isAlive());		
	}
}
