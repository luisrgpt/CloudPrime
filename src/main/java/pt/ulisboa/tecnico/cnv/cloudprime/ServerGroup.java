package pt.ulisboa.tecnico.cnv.cloudprime;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

public final class ServerGroup {

	// using a ConcurrentHashMap because, even though the below methods are
	// synchronized, the map is being returned to other threads
	private static Map<String, Server> servers = new ConcurrentHashMap<>();

	/**
	 * @return the instances
	 */
	public static synchronized Map<String, Server> getServers() {
		return servers;
	}

	/**
	 * inserts newly launched instances in the instance group (checking with AW)
	 */
	public static synchronized void updateServers() {
		DescribeInstancesResult describeInstancesRequest = AutoScaler.ec2.describeInstances();
		List<Reservation> reservations = describeInstancesRequest.getReservations();
		for (Reservation reservation : reservations) {
			for (Instance instance : reservation.getInstances()) {
				String state = instance.getState().getName();
				if (state.equals("running")) {
					if (servers.get(instance.getInstanceId()) == null) {
						servers.put(instance.getInstanceId(), new Server(instance));
					}
				}
			}
		}
	}

	public static synchronized void deleteServer(Server s) {
		servers.remove(s.getInstanceId());
	}

}
