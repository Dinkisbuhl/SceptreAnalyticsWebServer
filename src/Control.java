import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Control 
{
	private static TestServer theServer;
	public static void main(String[] args)
	{
		theServer.mainMethod();
	}
}
