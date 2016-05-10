package pt.ulisboa.tecnico.cnv.cloudprime;

import com.amazonaws.services.ec2.model.Instance;

public final class Server {
	private Instance instance;
	private int nReceivedChecks;
	private int nTimeoutChecks;
	private int totalLoad;
	private boolean inGracePeriod;
	private boolean suspected; // TODO: not being used for now

	public Server(Instance instance) {
		super();
		this.instance = instance;
		nReceivedChecks = nTimeoutChecks = 0;
		inGracePeriod = true;
	}

	public String getInstanceId() {
		return instance.getInstanceId();
	}

	public String getInstanceIpAddress() {
		return instance.getPublicIpAddress();
	}
	
	public synchronized int getTotalLoad() {
		return totalLoad;
	}

	public synchronized void setTotalLoad(int totalLoad) {
		this.totalLoad = totalLoad;
	}

	/**
	 * register a received health check from this server
	 * 
	 * @return indicates true if server is healthy, and false otherwise
	 */
	boolean healthCheckReceived() {
		// TODO: more health checks
		// we are using only one health check for now. If this changes, this
		// code will have to be changed

		// also, do not need to increment the number of received checks because of that.
		// nReceivedChecks++;
		
		// when machine receives the first health check, we can stop the grace period
		inGracePeriod = false;
		
		return true;
	}

	/**
	 * register a failed health check from this server
	 * 
	 * @return indicates true if server is unhealthy, and false otherwise
	 */
	boolean healthCheckFailed() {
		// TODO: more health checks
		// we are using only one health check for now. If this changes, this
		// code will have to be changed
		nTimeoutChecks++;
		final int maxChecksInGracePeriod = HealthChecker.HEALTH_CHECK_GRACE_PERIOD
				/ HealthChecker.HEALTH_CHECK_INTERVAL;
		if (inGracePeriod && nTimeoutChecks < maxChecksInGracePeriod) {
			return false;
		} else {
			return true;
		}
	}
	
	boolean isHealthy() {
		// TODO: more health checks
		// we are using only one health check for now. If this changes, this
		// code will have to be changed
		return !inGracePeriod;
	}
}
