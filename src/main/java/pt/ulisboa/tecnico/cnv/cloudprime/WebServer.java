package pt.ulisboa.tecnico.cnv.cloudprime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {

    public static void main(String[] args) throws IOException, InterruptedException {
    	BytecodeAnalyser.instrumentalizeClass(IntFactorization.class);
    	
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/f.html", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private static class MyHandler implements HttpHandler {
        @Override
        public void handle(final HttpExchange t) throws IOException {
            final String query = t.getRequestURI().getQuery();
            final OutputStream os = t.getResponseBody();
            if(query.charAt(0) == 'n' && query.charAt(1) == '=') {
                // TODO: verify if query.substring(2) is a number
                new Thread() {
                    public void run() {
                        try {
                            //String response = IntFactorization.main(new String[]{query.substring(2)});
                            //t.sendResponseHeaders(200, response.length());
                            //os.write(response.getBytes());
                        	Process p = Runtime.getRuntime().exec("java -cp target/classes/:target/classes/pt/ulisboa/tecnico/cnv/cloudprime/ -XX:-UseSplitVerifier " + IntFactorization.class.getName() + " " + query.substring(2));
                		    p.waitFor();
                		    
                		    BufferedReader outputReader = new BufferedReader(new InputStreamReader(p.getInputStream())),
                		    		       errorReader  = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                		    String outputLine = "";		
                		    StringBuffer outputStringBuffer = new StringBuffer();
                		    while ((outputLine = outputReader.readLine())!= null) {
                		    	outputStringBuffer.append(outputLine + "\n");
                		    }
                		    
                		    //String response = IntFactorization.getResponse(new String[]{query.substring(2)});
							/*try (Writer writer = new BufferedWriter(new OutputStreamWriter(
									new FileOutputStream(query.substring(2) + ".txt"), "utf-8"))) {
								writer.write(response + "\n" + BytecodeAnalyser.getMetricsString());
							}*/
                        	String outputResponse = outputStringBuffer.toString();
                        	//System.out.println(outputResponse);
                            t.sendResponseHeaders(200, outputResponse.length());
                            os.write(outputResponse.getBytes());
                            os.close();
                            
                		    String errorLine = "";		
                		    StringBuffer errorStringBuffer = new StringBuffer();
                		    while ((errorLine = errorReader.readLine())!= null) {
                		    	errorStringBuffer.append(errorLine + "\n");
                		    }
                		    
                		    String errorResponse = errorStringBuffer.toString();
                		    System.out.println(errorResponse);
                        } catch (IOException | InterruptedException e) {
                        	System.err.println(e.getMessage());
                            System.err.println("Error " + e.getClass().getName() + ". The socket might have been closed. "
                                             + "Make sure you don't close the previous request to issue a new one.");
                        }
                    }
                }.start();
            } else {
                t.sendResponseHeaders(404, 5); // size of string "Error" = 5
                os.write("Error".getBytes());
                os.close();
            }
        }
    }
}
