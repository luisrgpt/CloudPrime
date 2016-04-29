package pt.ulisboa.tecnico.cnv.cloudprime;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

public final class InstanceGroup {

	private static Map<String, Instance> instances = new ConcurrentHashMap<>();

	/**
	 * @return the instances
	 */
	public static synchronized Map<String, Instance> getInstances() {
		return instances;
	}

	/**
	 * resets the instances (checking with AW)
	 */
	public static synchronized void resetInstances() {
		instances.clear();
		DescribeInstancesResult describeInstancesRequest = AutoScaler.ec2.describeInstances();
		List<Reservation> reservations = describeInstancesRequest.getReservations();
		for (Reservation reservation : reservations) {
			for (Instance instance : reservation.getInstances()) {
				String state = instance.getState().getName();
				if (state.equals("running")) {
					instances.put(instance.getInstanceId(), instance);
				}
			}
		}
	}

}
