package pt.ulisboa.tecnico.cnv.cloudprime;

class WorkerServer extends WebServer {
	WorkerServer(int port) {
		super(port);
	}

	public static void main(String[] args) {
		//// for debug ////
		/*
		 * for (String arg : args) System.out.println(arg);
		 */
		///////////////////

		// if launched from maven, instrument. Else, run the server!
		if (args.length > 0)
			BytecodeAnalyser.instrument();
		else
			new WorkerServer(8000);
	}

	@Override
	String processRequest(String query) {
		String response = IntFactorization.getResponse(new String[] { query.substring(2) });
		/*
		 * try (Writer writer = new BufferedWriter( new OutputStreamWriter(new
		 * FileOutputStream(query.substring(2) + ".txt"), "utf-8"))) {
		 * writer.write(response); }
		 */
		return response;
	}
}