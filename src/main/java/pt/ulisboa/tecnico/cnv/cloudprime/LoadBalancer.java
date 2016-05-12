package pt.ulisboa.tecnico.cnv.cloudprime;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LoadBalancer extends WebServer {
	final static int LB_ENDPOINT_PORT = 80;
	final static long UNKNOWN_LOAD = -1;

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
		Server server = selectServer(query, getLoad(query));
		String serverIp = server.getInstanceIpAddress();
		String urlString = "http://" + serverIp + ":8000/f.html?" + query;
		String res = httpGet(urlString);
		server.removeLoad(getLoad(query)); // FIXME: prototype. This should be
											// addressed by "requestId" ???
		return res;
	}

	private long getLoad(String query) {
		Long[] instructionTypeCounter = DynamoDB.getMetrics(query.substring(2));
		long load;
		if (instructionTypeCounter == null) {
			load = UNKNOWN_LOAD;
		} else {
			load = instructionTypeCounter[0] + Long.MAX_VALUE; // FIXME: use
																// metrics
																// formula
		}
		return load;
	}

	private synchronized Server selectServer(String query, long load) {
		// TODO: if there is no server, retry after some delay
		Long[] instructionTypeCounter = DynamoDB.getMetrics(query.substring(2));
		if (instructionTypeCounter == null) {
			// if there are no metrics associated to this request, use round
			// robin
			List<Server> serverList = new ArrayList<>(ServerGroup.getServers().values());
			return serverList.get(choice++ % serverList.size());
		} else {
			//////// FIXME: prototype: selects always the less loaded server
			List<Server> serverList = new ArrayList<>(ServerGroup.getServers().values());
			long minLoad = Long.MAX_VALUE;
			Server choice = null;
			// TODO: Check if there is a server matching the requirements and
			// launch one otherwise. Then wait for it.
			for (Server s : serverList) {
				if (s.getTotalLoad() < minLoad) {
					minLoad = s.getTotalLoad();
					choice = s;
				}
			}
			System.out.println("<Choosing server>");
			System.out.println("  Request with load: " + load);
			System.out.println("  ID:                " + choice.getInstanceId());
			System.out.println("  Server load:       " + choice.getTotalLoad());
			System.out.println("</Choosing server>");
			choice.addLoad(load);
			return choice;
			//////// FIXME: (END) prototype: select the less loaded server
		}
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
			// e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result.toString();
	}

}