package pt.ulisboa.tecnico.cnv.cloudprime;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LoadBalancer extends WebServer {
	final static int LB_ENDPOINT_PORT = 8001;

	int choice = 0;

	public static void main(String[] args) {
		new LoadBalancer(LB_ENDPOINT_PORT);
	}

	public LoadBalancer(int port) {
		super(port);
		new AutoScaler().run();
		new HealthChecker().run();
	}

	@Override
	String processRequest(String query) {
		String urlString = "http://" + selectServer(query) + ":8000/f.html?" + query;
		return httpGet(urlString);
	}

	private String selectServer(String query) {
		Long[] instructionTypeCounter = DynamoDB.getMetrics(query.substring(2));
		if (instructionTypeCounter == null) {
			List<Server> serverList = new ArrayList<>(ServerGroup.getServers().values());
			return serverList.get(choice++ % serverList.size()).getInstanceIpAddress();
		}

		// TODO: implement better choice. For now it's round robin
		// TODO: if there is no server, retry after some delay
		List<Server> serverList = new ArrayList<>(ServerGroup.getServers().values());
		return serverList.get(choice++ % serverList.size()).getInstanceIpAddress();
	}

	static String httpGet(String urlString) {
		StringBuilder result = new StringBuilder();
		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			int code = conn.getResponseCode();
			if (code == 200) {
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
				rd.close();
			} else {
				return null;
			}
		} catch (ConnectException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result.toString();
	}

}