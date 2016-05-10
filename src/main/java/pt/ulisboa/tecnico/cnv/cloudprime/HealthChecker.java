package pt.ulisboa.tecnico.cnv.cloudprime;

import java.util.ArrayList;

public final class HealthChecker {
	final static int HEALTH_CHECK_TIMEOUT = 10 * 1000;
	final static int HEALTH_CHECK_INTERVAL = 30 * 1000;
	final static int HEALTH_CHECK_GRACE_PERIOD = 90 * 1000;

	ArrayList<Boolean> shouldStop = new ArrayList<>();

	void run() {
		new Thread() {
			@Override
			public void run() {
				while (true) {
					shouldStop.clear();
					int position = 0;
					System.out.println("Will run health check");
					ServerGroup.updateServers();
					for (final Server server : ServerGroup.getServers().values()) {
						String s = server.getInstanceIpAddress();
						final int pos = position++;
						shouldStop.add(false);
						new CheckerThread(pos, s).start();
						new TimerThread(pos, s).start();
					}
					try {
						Thread.sleep(HEALTH_CHECK_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
						System.exit(-1);
					}
				}
			}
		}.start();
	}

	private class CheckerThread extends Thread {
		private int pos;
		private String instanceIP;

		public CheckerThread(int pos, String instanceIP) {
			super();
			this.pos = pos;
			this.instanceIP = instanceIP;
		}

		@Override
		public void run() {
			String urlString = "http://" + instanceIP + ":8000/f.html?n=1";
			String res = LoadBalancer.httpGet(urlString);
			if (res == null) {
				synchronized (shouldStop) {
					if (!shouldStop.get(pos)) {
						shouldStop.remove(pos);
						shouldStop.add(pos, true);
						// TODO: "restart" instance
						System.out.println("Instance is unhealthy: status check code is not HTTP OK");
					}
				}
			} else {
				synchronized (shouldStop) {
					if (!shouldStop.get(pos)) {
						shouldStop.remove(pos);
						shouldStop.add(pos, true);
						System.out.println("Healthy instance! Thread not interrupted!");
					}
				}
			}
		}
	}

	private class TimerThread extends Thread {
		private int pos;
		private String instanceIP;

		public TimerThread(int pos, String instanceIP) {
			super();
			this.pos = pos;
			this.instanceIP = instanceIP;
		}

		@Override
		public void run() {
			try {
				sleep(HEALTH_CHECK_TIMEOUT);
				synchronized (shouldStop) {
					if (!shouldStop.get(pos)) {
						shouldStop.remove(pos);
						shouldStop.add(pos, true);
						// TODO: "restart" instance
						System.out.println("Instance is unhealthy: timeout");
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
}
