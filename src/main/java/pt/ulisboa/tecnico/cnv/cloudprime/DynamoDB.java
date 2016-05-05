package pt.ulisboa.tecnico.cnv.cloudprime;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

/**
 * This sample demonstrates how to perform a few simple operations with the
 * Amazon DynamoDB service.
 */
class DynamoDB {
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

	private static final String TABLE_NAME = "metrics";
	private static final String KEY_NAME = "value";
	static AmazonDynamoDBClient _dynamoDB;

	/**
	 * The only information needed to create a client are security credentials
	 * consisting of the AWS Access Key ID and Secret Access Key. All other
	 * configuration, such as the service endpoints, are performed
	 * automatically. Client parameters, such as proxies, can be specified in an
	 * optional ClientConfiguration object when constructing a client.
	 * @throws InterruptedException 
	 *
	 * @see com.amazonaws.auth.BasicAWSCredentials
	 * @see com.amazonaws.auth.ProfilesConfigFile
	 * @see com.amazonaws.ClientConfiguration
	 */
	static void init() {
		try {
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
			_dynamoDB = new AmazonDynamoDBClient(credentials);
			Region usWest2 = Region.getRegion(Regions.US_WEST_2);
			_dynamoDB.setRegion(usWest2);
			
			createMetricsTable();
			
		} catch (AmazonServiceException ase) {
			System.err.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.err.println("Error Message:    " + ase.getMessage());
			System.err.println("HTTP Status Code: " + ase.getStatusCode());
			System.err.println("AWS Error Code:   " + ase.getErrorCode());
			System.err.println("Error Type:       " + ase.getErrorType());
			System.err.println("Request ID:       " + ase.getRequestId());
			System.exit(-1);
		} catch (AmazonClientException ace) {
			System.err.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.err.println("Error Message: " + ace.getMessage());
			System.exit(-1);
		} catch (InterruptedException exception) {
			exception.printStackTrace();
			System.exit(-1);
		}
	}

	static void describeMetrics() {
		// Describe our new table
		DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(TABLE_NAME);
		TableDescription tableDescription = _dynamoDB.describeTable(describeTableRequest).getTable();
		System.out.println("Table Description: " + tableDescription);
	}

	static void createMetricsTable()
			throws AmazonServiceException, AmazonClientException, InterruptedException {
		// Get all table names from DynamoDB
		ListTablesResult listTablesResult = _dynamoDB.listTables(new ListTablesRequest());

		// Create table if it does not exist yet
		if (!listTablesResult.getTableNames().contains(TABLE_NAME)) {
			// Create a table with a primary hash key named 'name', which
			// holds a string
			CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(TABLE_NAME)
					.withKeySchema(new KeySchemaElement().withAttributeName(KEY_NAME).withKeyType(KeyType.HASH))
					.withAttributeDefinitions(new AttributeDefinition().withAttributeName(KEY_NAME)
							.withAttributeType(ScalarAttributeType.N))
					.withProvisionedThroughput(
							new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
			TableDescription createdTableDescription = _dynamoDB.createTable(createTableRequest)
					.getTableDescription();
			System.out.println("Created Table: " + createdTableDescription);

			// Wait for it to become active
			System.out.println("Waiting for " + TABLE_NAME + " to become ACTIVE...");
			TableUtils.waitUntilActive(_dynamoDB, TABLE_NAME);
		}
		System.out.println("Table " + TABLE_NAME + " is ACTIVE");
	}

	static Long[] getMetrics(String value)
			throws AmazonServiceException, AmazonClientException {
		// Scan items for movies with a year attribute greater than 1985
		HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
		Condition condition = new Condition().withComparisonOperator(ComparisonOperator.EQ.toString())
				.withAttributeValueList(new AttributeValue().withN(value));
		scanFilter.put(KEY_NAME, condition);
		ScanRequest scanRequest = new ScanRequest(TABLE_NAME).withScanFilter(scanFilter);
		ScanResult scanResult = _dynamoDB.scan(scanRequest);
		
		System.out.println("Scan Result:    " + scanResult);
		
		List<Map<String, AttributeValue>> items = scanResult.getItems();
		if(items.isEmpty()) {
			return null;
		}
		
		String[] encodedMetrics = items.get(0)
				.get(TABLE_NAME)
				.getS()
				.replace("[", "")
				.replace("]", "")
				.split(", ");
		int size = 4;
		Long[] metrics = new Long[size];
		for(int index = 0; index < size; ++index) {
			metrics[index] = new Long(Long.parseLong(encodedMetrics[index]));
		}
		
		return metrics;
	}

	static void addMetrics(String value, Long[] longs) {
		try {
			// Add an item
			Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
			item.put(KEY_NAME, 		new AttributeValue().withN(value));
			item.put(TABLE_NAME, 	new AttributeValue().withS(Arrays.toString(longs)));
			
			PutItemRequest putItemRequest = new PutItemRequest(TABLE_NAME, item);
			_dynamoDB.putItem(putItemRequest);
			System.out.println("Stored metrics from value " + value + ".");
		} catch (AmazonServiceException ase) {
			System.err.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.err.println("Error Message:    " + ase.getMessage());
			System.err.println("HTTP Status Code: " + ase.getStatusCode());
			System.err.println("AWS Error Code:   " + ase.getErrorCode());
			System.err.println("Error Type:       " + ase.getErrorType());
			System.err.println("Request ID:       " + ase.getRequestId());
			System.err.println("Recovering dynamoDB");
			init();
			System.err.println("Recovered");
		} catch (AmazonClientException ace) {
			System.err.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.err.println("Error Message: " + ace.getMessage());
			System.err.println("Recovering dynamoDB");
			init();
			System.err.println("Recovered");
		}
	}
}