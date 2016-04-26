package pt.ulisboa.tecnico.cnv.cloudprime;

import java.io.IOException;

public class WebServerStarter {

    public static void main(String[] args) throws IOException, InterruptedException {
    	new MultiProcessedServer(8000);
    	//new MultiThreadedServer();
        Runtime.getRuntime().exec("firefox localhost:8000/f.html?n=2346223345");
    }
}
