package pt.ulisboa.tecnico.cnv.cloudprime;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

public class LoadBalancer extends WebServer {
	int choice = 0;
	ArrayList<String> ips = null;

	public LoadBalancer(int port) {
		super(port);
		try {
			ips = getNames();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new LoadBalancer(8001);
	}

	@Override
	String processRequest(String query) {
		StringBuilder result = new StringBuilder();
		try {
			URL url = new URL("http://" + ips.get(choice++ % ips.size()) + ":8000/f.html?" + query);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	ArrayList<String> getNames() throws Exception {
		init();

		ArrayList<String> ips = new ArrayList<String>();

		try {
			/*
			 * Using AWS Ireland. Pick the zone where you have AMI, key and
			 * secgroup
			 */
			ec2.setEndpoint("ec2.us-west-2.amazonaws.com");
			cloudWatch.setEndpoint("monitoring.us-west-2.amazonaws.com");
			// Uncomment below if you need to start an instance
			/*
			 * System.out.println("Starting a new instance.");
			 * RunInstancesRequest runInstancesRequest = new
			 * RunInstancesRequest();
			 * 
			 * runInstancesRequest.withImageId("ami-08ef697b")
			 * .withInstanceType("t2.micro") .withMinCount(1) .withMaxCount(1)
			 * .withKeyName("cnv-aws") .withSecurityGroups("ssh+http8000");
			 * RunInstancesResult runInstancesResult =
			 * ec2.runInstances(runInstancesRequest); String newInstanceId =
			 * runInstancesResult.getReservation().getInstances()
			 * .get(0).getInstanceId();
			 */
			DescribeInstancesResult describeInstancesResult = ec2.describeInstances();
			List<Reservation> reservations = describeInstancesResult.getReservations();
			Set<Instance> instances = new HashSet<Instance>();

			for (Reservation reservation : reservations) {
				instances.addAll(reservation.getInstances());
			}
			for (Instance instance : instances) {
				String state = instance.getState().getName();
				if (state.equals("running")) {
					String ip = instance.getPublicIpAddress();
					System.out.println(ip);
					ips.add(ip);
				}
			}
		} catch (AmazonServiceException ase) {
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
		}
		return ips;
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
	static AmazonCloudWatchClient cloudWatch;

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
		cloudWatch = new AmazonCloudWatchClient(credentials);
	}

}