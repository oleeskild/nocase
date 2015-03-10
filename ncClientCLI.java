import java.net.*;
import java.io.*;

public class ncClientCLI {
      // Create a new client and establish a connection to the given host
      public static void main(String[] args) {
            try {
                  BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                  System.out.print("Connect to host:");

                  String host = br.readLine();
                  int port = 1337;

                  ncClient client = new ncClient(host,port);

                  // Connected successfully, query user for messages
                  boolean exit = false;
                  do {
                        String msg = br.readLine();
                        // Aborted by user?
                        if(msg.equals("/quit") || msg.equals("/exit")) {
                              exit = true;
                              break;
                        }
                        client.sendMessage(msg);
                  } while(!exit);
                  client.connection().close();
            } catch (Exception ex) {
                  System.out.println("An error has occurred! Aborting...");
            }
            System.exit(0);
      }
}
