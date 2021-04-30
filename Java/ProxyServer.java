import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyServer
{
	protected ServerSocket server;
	protected ExecutorService executor;
	protected static int LISTEN_PORT = 8080;

	public ProxyServer (int port)
	{
		executor = Executors.newCachedThreadPool();
		try {
			server = new ServerSocket(port);
		}
		catch (IOException e) {}
	}

	public void accept()
	{
		while (true) {
			try {
				executor.execute(new RequestHandler(server.accept()));
			}
			catch (IOException e) {}
		}
	}

	public static void main(String[] args)
	{
		System.out.println("ProxyServer is listening to port "+LISTEN_PORT);
		ProxyServer proxy = new ProxyServer(LISTEN_PORT);
		proxy.accept();
	}
}
