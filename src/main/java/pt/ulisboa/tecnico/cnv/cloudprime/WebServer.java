package pt.ulisboa.tecnico.cnv.cloudprime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {

	public static void main(String[] args) {
		new WebServer(8000);
	}

	WebServer(int port) {
		try {
			BytecodeAnalyser.instrumentalizeClass(IntFactorization.class);
		} catch (IOException | InterruptedException e) {
			System.err.println("Failed to instrument class.");
			e.printStackTrace();
			System.exit(-1);
		}

		HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);
			server.createContext("/f.html", new MyHandler());
			server.setExecutor(null); // creates a default executor
			server.start();
		} catch (IOException e) {
			System.err.println("Failed to open socket.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	String processRequest(String query) {
		try {
			Process p = Runtime.getRuntime()
					.exec("/home/miranda/bin/jdk1.7.0_80/bin/java -cp target/classes/:target/classes/pt/ulisboa/tecnico/cnv/cloudprime/ -XX:-UseSplitVerifier "
							+ IntFactorization.class.getName() + " " + query.substring(2));
			p.waitFor();

			BufferedReader outputReader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String outputLine = "";
			StringBuffer outputStringBuffer = new StringBuffer();
			while ((outputLine = outputReader.readLine()) != null) {
				outputStringBuffer.append(outputLine + "\n");
			}

			// String response = IntFactorization.getResponse(new
			// String[]{query.substring(2)});
			/*
			 * try (Writer writer = new BufferedWriter(new OutputStreamWriter(
			 * new FileOutputStream(query.substring(2) + ".txt"), "utf-8"))) {
			 * writer.write(response + "\n" +
			 * BytecodeAnalyser.getMetricsString()); }
			 */
			return outputStringBuffer.toString();
		} catch (IOException | InterruptedException e) {
			System.err.println(e.getMessage());
			System.err.println("Error " + e.getClass().getName() + ". The socket might have been closed. "
					+ "Make sure you don't close the previous request to issue a new one.");
			return "The socket might have been closed.";
		}
	}

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
