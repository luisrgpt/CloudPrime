package pt.ulisboa.tecnico.cnv.cloudprime;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

class MultiThreadedServer extends WebServer {
	MultiThreadedServer(int port) {
		super(port);
	}

	public static void main(String[] args) {
		for (String arg : args)
			System.out.println(arg);
		if (args.length > 0)
			instrumentClass(new MultiThreadedBytecodeAnalyser());
		new MultiThreadedServer(8000);
	}

	@Override
	String processRequest(String query) {
		try {
			String response = IntFactorization.getResponse(new String[] { query.substring(2) });
			try (Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(query.substring(2) + ".txt"), "utf-8"))) {
				writer.write(response + "\n" + MultiThreadedBytecodeAnalyser.getMetricsString());
			}
			return response;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.err.println("Error " + e.getClass().getName() + ". The socket might have been closed. "
					+ "Make sure you don't close the previous request to issue a new one.");
			return "The socket might have been closed.";
		}
	}
}