import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.StringTokenizer;


public class RequestHandler implements Runnable {
	protected DataInputStream clientInputStream;
	protected OutputStream clientOutputStream;
	protected OutputStream remoteOutputStream;
	protected InputStream remoteInputStream;
	protected Socket clientSocket;
	protected Socket remoteSocket;
	protected String requestType;
	protected String url;
	protected String uri;
	protected String httpVersion;
	protected HashMap<String, String> header;
	static String endOfLine = "\r\n";

	public RequestHandler(Socket clientSocket) {
		header = new HashMap<String, String>();
		this.clientSocket = clientSocket;
	}

	public void run() {
		try {
			clientInputStream = new DataInputStream(clientSocket.getInputStream());
			clientOutputStream = clientSocket.getOutputStream();

			clientToProxy();
			proxyToRemote();
			remoteToClient();

			System.out.println();

			if(remoteOutputStream != null) remoteOutputStream.close();
			if(remoteInputStream != null) remoteInputStream.close();
			if(remoteSocket != null) remoteSocket.close();


			if(clientOutputStream != null) clientOutputStream.close();
			if(clientInputStream != null) clientInputStream.close();
			if(clientSocket != null) clientSocket.close();
		} catch (IOException e) { }
	}

	@SuppressWarnings("deprecation")
	private void clientToProxy() {
		String line, key, value;
		StringTokenizer tokens;

		try {
			if(( line = clientInputStream.readLine()) != null) {
				tokens = new StringTokenizer(line);
				requestType = tokens.nextToken();
				url = tokens.nextToken();
				httpVersion = tokens.nextToken();
			}

			while((line = clientInputStream.readLine()) != null) {
				if(line.trim().length() == 0) break;

				tokens = new StringTokenizer(line);
				key = tokens.nextToken(":");
				value = line.replaceAll(key, "").replace(": ", "");
				header.put(key.toLowerCase(), value);
			}

			stripUnwantedHeaders();
			getUri();
		} 
		catch (UnknownHostException e) { return; } 
		catch (SocketException e){ return; } 
		catch (IOException e) { return;} 
	}

	private void proxyToRemote() {
		try{
			if(header.get("host") == null) return;
			if(!requestType.startsWith("GET") && !requestType.startsWith("POST")) 
				return;

			remoteSocket = new Socket(header.get("host"), 80);
			remoteOutputStream = remoteSocket.getOutputStream();

			// make sure streams are still open
			checkRemoteStreams();
			checkClientStreams();

			// make request from client to remote server
			String request = requestType + " " + uri + " HTTP/1.0";
			remoteOutputStream.write(request.getBytes());
			remoteOutputStream.write(endOfLine.getBytes());
			System.out.println(request);

			// send hostname
			String command = "host: "+ header.get("host");
			remoteOutputStream.write(command.getBytes());
			remoteOutputStream.write(endOfLine.getBytes());
			System.out.println(command);

			// send rest of the headers
			for( String key : header.keySet()) {
				if(!key.equals("host")){
					command = key + ": "+ header.get(key);
					remoteOutputStream.write(command.getBytes());
					remoteOutputStream.write(endOfLine.getBytes());
					System.out.println(command);
				}
			}

			remoteOutputStream.write(endOfLine.getBytes());
			remoteOutputStream.flush();

			// send client request data if its a POST request
			if(requestType.startsWith("POST")) {

				int contentLength = Integer.parseInt(header.get("content-length"));
				for (int i = 0; i < contentLength; i++)
				{
					remoteOutputStream.write(clientInputStream.read());
				}
			}

			// complete remote server request
			remoteOutputStream.write(endOfLine.getBytes());
			remoteOutputStream.flush();
		}
		catch (UnknownHostException e) { return; } 
		catch (SocketException e){ return; } 
		catch (IOException e) { return;} 
	}

	@SuppressWarnings("deprecation")
	private void remoteToClient() {
		try {

			// If socket is closed, return
			if(remoteSocket == null) return;

			String line;
			DataInputStream remoteOutHeader = new DataInputStream(remoteSocket.getInputStream());

			// get remote response header
			while((line = remoteOutHeader.readLine()) != null) {

				// check for end of header blank line
				if(line.trim().length() == 0) break;

				// check for proxy-connection: keep-alive
				if(line.toLowerCase().startsWith("proxy")) continue;
				if(line.contains("keep-alive")) continue;

				// write remote response to client
				System.out.println(line);
				clientOutputStream.write(line.getBytes());
				clientOutputStream.write(endOfLine.getBytes());
			}

			// complete remote header response
			clientOutputStream.write(endOfLine.getBytes());
			clientOutputStream.flush();

			// get remote response body
			remoteInputStream = remoteSocket.getInputStream();
			byte[] buffer = new byte[1024];

			// buffer remote response then write it back to client
			for(int i; (i = remoteInputStream.read(buffer)) != -1;) 
			{
				clientOutputStream.write(buffer, 0, i);
				clientOutputStream.flush();
			}
		} 
		catch (UnknownHostException e) { return; } 
		catch (SocketException e){ return; } 
		catch (IOException e) { return;} 
	}

	private void stripUnwantedHeaders() {
		if(header.containsKey("user-agent")) header.remove("user-agent");
		if(header.containsKey("referer")) header.remove("referer");
		if(header.containsKey("proxy-connection")) header.remove("proxy-connection");
		if(header.containsKey("connection") && header.get("connection").equalsIgnoreCase("keep-alive")) {
			header.remove("connection");
		}
	}

	private void checkClientStreams() {
		try {
			if(clientSocket.isOutputShutdown())
				clientOutputStream = clientSocket.getOutputStream();
			if(clientSocket.isInputShutdown())
				clientInputStream = new DataInputStream(clientSocket.getInputStream());
		}
		catch (UnknownHostException e) { return; } 
		catch (SocketException e){ return; } 
		catch (IOException e) { return;} 
	}

	private void checkRemoteStreams() {
		try {
			if(remoteSocket.isOutputShutdown())
				remoteOutputStream = remoteSocket.getOutputStream();
			if(remoteSocket.isInputShutdown())
				remoteInputStream = new DataInputStream(remoteSocket.getInputStream());
		} 
		catch (UnknownHostException e) { return; } 
		catch (SocketException e){ return; } 
		catch (IOException e) { return;} 
	}

	private void getUri()
	{
		if(header.containsKey("host")) {
			int temp = url.indexOf(header.get("host"));
			temp += header.get("host").length();

			if(temp < 0) { 
				// prevent index out of bound, use entire url instead
				uri = url;
			} else {
				// get uri from part of the url
				uri = url.substring(temp);	
			}
		}
	}

}
