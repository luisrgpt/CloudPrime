package pt.ulisboa.tecnico.cnv.cloudprime;

class WorkerServer extends WebServer {
	WorkerServer(int port) {
		super(port);
	}

	public static void main(String[] args) {
		new WorkerServer(8000);
	}

	@Override
	String processRequest(String query) {
		String value = query.substring(2);
		
		BytecodeAnalyser.setValue(value);
		return IntFactorization.getResponse(value);
	}
}