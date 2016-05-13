package pt.ulisboa.tecnico.cnv.cloudprime;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoadBalancer extends WebServer {
	final static int LB_ENDPOINT_PORT = 80;

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
		Server server = selectServer(query);
		String serverIp = server.getInstanceIpAddress();
		String urlString = "http://" + serverIp + ":8000/f.html?" + query;
		String res = httpGet(urlString);
		// FIXME: prototype. This should be addresses by "requestId" ???
		Long metric = DynamoDB.getMetric(query.substring(2));
		if (metric != null) {
			server.removeLoad(metric);
		}
		return res;
	}

	private synchronized Server selectServer(String query) {
		// TODO: if there is no server, retry after some delay
		Long metric = DynamoDB.getMetric(query.substring(2));
		if (metric == null) {
			// if there are no metrics associated to this request, use round robin
			List<Server> serverList = new ArrayList<>(ServerGroup.getServers().values());
			return serverList.get(choice++ % serverList.size());
		} else {
			//////// FIXME: prototype: select the less loaded server ////////////
			List<Server> serverList = new ArrayList<>(ServerGroup.getServers().values());
			long minLoad = Long.MAX_VALUE;
			Server choice = null;
			// TODO: Check if there is a server matching the requirements and launch one otherwise. Then wait for it.
			for (Server s : serverList) {
				if(s.getTotalLoad() < minLoad) {
					minLoad = s.getTotalLoad();
					choice = s;
				}
			}
			System.out.println("<Choosing server>");
			System.out.println("  Request with load: " + metric);
			System.out.println("  ID:                " + choice.getInstanceId());
			System.out.println("  Server load:       " + choice.getTotalLoad());
			System.out.println("</Choosing server>");
			choice.addLoad(metric);
			return choice;
			//////// FIXME: (END) prototype: select the less loaded server ////////////
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