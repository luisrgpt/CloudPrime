package pt.ulisboa.tecnico.cnv.cloudprime;

import java.io.IOException;

public class WebServerStarter {

    public static void main(String[] args) throws IOException, InterruptedException {
    	new WorkerServer(8000);
        Runtime.getRuntime().exec("firefox localhost:8000/f.html?n=2346223345");
    }
}
