package pt.ulisboa.tecnico.cnv.cloudprime;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public final class AutoScaler {
	private final static int COOLDOWN = 2 * 60 * 1000;
	private final static int METRICS_CHECK_INTERVAL = 1 * 60 * 1000;

	private boolean wasOnCD = false;

	void run() {
		try {
			init();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						makeScalingDecision();
						if (!wasOnCD) {
							Thread.sleep(METRICS_CHECK_INTERVAL);
						} else {
							// already waited for CD, will not wait any longer
							wasOnCD = false;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
						System.exit(-1);
					}
				}
			}
		}.start();
	}

	protected void makeScalingDecision() {
		// TODO: make real decisions...
		boolean scalingUp = false;
		boolean scalingDown = !scalingUp;
		String instanceIP = "";// ips.get(0);

		if (scalingUp && scalingDown) {
			System.err.println("Should not decide to scale up and down at the same time!?");
			return;
		}

		if (scalingUp) {
			scaleUp(1);
			enterCoolDown();
		} else if (scalingDown) {
			scaleDown(instanceIP);
			enterCoolDown();
		}
	}

	private void scaleUp(int number) {
		try {
			System.out.println("Starting a new instance.");
			RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
			/* configure to use your AMI, key and security group */
			runInstancesRequest.withImageId("ami-79b24619").withInstanceType("t2.micro").withMinCount(number)
					.withMaxCount(number).withKeyName("CNV-kp").withSecurityGroups("CNV-ssh+http");
			RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
			// FIXME: if number > 1 needs to be used, the code below must be
			// changed
			// Instance i = runInstancesResult.getReservation().getInstances().get(0);
			// ServerGroup.getServers().put(i.getInstanceId(), new Server(i));
		} catch (AmazonServiceException ase) {
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
		}
	}

	private void scaleDown(String instanceId) {
		try {
			// TODO: replace this for argument passing, the correct instance to
			// terminate
			instanceId = null;
			for (Server server : ServerGroup.getServers().values()) {
				if (server.isHealthy()) {
					instanceId = server.getInstanceId();
					ServerGroup.getServers().remove(server);
					break;
				}
			}
			if (instanceId == null) {
				System.err.println("No instance to terminate");
				return;
			}
			////////////////////////////////////////////////////////////////

			System.out.println("Terminating the instance.");
			TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
			termInstanceReq.withInstanceIds(instanceId);
			ec2.terminateInstances(termInstanceReq);

		} catch (AmazonServiceException ase) {
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
		}
	}

	void enterCoolDown() {
		try {
			wasOnCD = true;
			Thread.sleep(COOLDOWN);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/*
	 * Before running the code: Fill in your AWS access credentials in the
	 * provided credentials file template, and be sure to move the file to the
	 * default location (~/.aws/credentials) where the sample code will load the
	 * credentials from.
	 * https://console.aws.amazon.com/iam/home?#security_credential
	 *
	 * WARNING: To avoid accidental leakage of your credentials, DO NOT keep the
	 * credentials file in your source directory.
	 */

	static AmazonEC2 ec2;

	/**
	 * The only information needed to create a client are security credentials
	 * consisting of the AWS Access Key ID and Secret Access Key. All other
	 * configuration, such as the service endpoints, are performed
	 * automatically. Client parameters, such as proxies, can be specified in an
	 * optional ClientConfiguration object when constructing a client.
	 *
	 * @see com.amazonaws.auth.BasicAWSCredentials
	 * @see com.amazonaws.auth.PropertiesCredentials
	 * @see com.amazonaws.ClientConfiguration
	 */
	private static void init() throws Exception {

		/*
		 * The ProfileCredentialsProvider will return your [default] credential
		 * profile by reading from the credentials file located at
		 * (~/.aws/credentials).
		 */
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider().getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (~/.aws/credentials), and is in valid format.", e);
		}
		ec2 = new AmazonEC2Client(credentials);
		ec2.setEndpoint("ec2.us-west-2.amazonaws.com");

		ServerGroup.updateServers();
	}
}
