import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/f.html", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(final HttpExchange t) throws IOException {
            final String query = t.getRequestURI().getQuery();
            final OutputStream os = t.getResponseBody();
            if(query.charAt(0) == 'n' && query.charAt(1) == '=') {
                // TODO: verify if query.substring(2) is a number
                new Thread() {
                    public void run() {
                        try {
                            String response = IntFactorization.getResponse(new String[]{query.substring(2)});
                            t.sendResponseHeaders(200, response.length());
                            os.write(response.getBytes());
                            os.close();
                        } catch (IOException e) {
                            System.err.println("Error. The socket might have been closed. "
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
