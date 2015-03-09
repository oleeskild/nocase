import java.net.*;
import java.io.*;

public class ncClient {
      // This clients socket connection that got accepted by the server
      private Socket connection;
      // The port which the server is listening on
      private int port;
      // Read messages sent by the server
      private BufferedReader response;
      // Send messages to the server
      private DataOutputStream client;

      // Try to connect to server
      public ncClient(String host, int p) throws Exception {
            // Set port to  connect to
            this.port = p;
            // Prints info
            System.out.println("Connecting to " + host + " on port " + this.port);

            try {
                  // Tries to establish a connection to host, on port
                  connection = new Socket(host,p);
            } catch(Exception ex) {
                  // Connection to host failed, abort
                  System.out.println("Connection to " + host + " failed!");
                  System.exit(0);
            }

            // Connection was successfully established
            System.out.println("Connected...");
            // Update streams for this connection
            this.response = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
            this.client = new DataOutputStream(this.connection.getOutputStream());

            // Start new thread to listen for incoming messages from server
            msgHandler();
      }

      // Listens to incoming messages from server, on a new thread
      public void msgHandler() {
            (new Thread(){
                  @Override
                  public void run() {
                  while(true) {
                        try {
                              System.out.println(readMessage());
                        } catch (Exception ex) {
                        }
                  }
                  }
            }).start();
      }

      // Read message from the server
      public String readMessage() throws Exception {
            return this.response.readLine();
      }

      // Send message to the server
      public void sendMessage(String s) throws Exception {
            this.client.writeBytes(s+"\n");
      }

      // Returns this sockect connection that was accepted by the server
      public Socket connection() throws Exception {
            return connection;
      }

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
