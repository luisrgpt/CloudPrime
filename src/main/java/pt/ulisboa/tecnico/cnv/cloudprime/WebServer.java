package pt.ulisboa.tecnico.cnv.cloudprime;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public abstract class WebServer {

	WebServer(int port) {
		HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);
			server.createContext("/f.html", new MyHandler());
			server.setExecutor(null); // creates a default executor
			server.start();
			System.out.println("Running " + getClass().getSimpleName() +  ". Wating for requests...");
		} catch (IOException e) {
			System.err.println("Failed to open socket.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	abstract String processRequest(String query);

	private class MyHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange t) throws IOException {
			final String query = t.getRequestURI().getQuery();
			final OutputStream os = t.getResponseBody();
			if (query.charAt(0) == 'n' && query.charAt(1) == '=') {
				// TODO: verify if query.substring(2) is a number
				new Thread() {
					public void run() {
						String outputResponse = processRequest(query);
						try {
							t.sendResponseHeaders(200, outputResponse.length());
							OutputStream os = t.getResponseBody();
							os.write(outputResponse.getBytes());
							os.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}.start();
			} else {
				t.sendResponseHeaders(404, 5); // size of string "Error" = 5
				os.write("Error".getBytes());
				os.close();
			}
		}
	}
}
