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

            // Request message of the day when connecting
            this.sendMessage("/motd");
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
                              System.out.println("Connection Error! Aborting...");
                              System.exit(0);
                        }
                  }
                  }
            }).start();
      }

      // Read message from the server
      public String readMessage() throws Exception {
            String msg = this.response.readLine();
            if(msg == null) {
                  System.out.println("Connection Error! Aborting...");
                  System.exit(0);
            }
            return msg;
      }

      // Send message to the server
      public void sendMessage(String s) throws Exception {
            this.client.write((s+"\n").getBytes("UTF-8"));
      }

      // Returns this sockect connection that was accepted by the server
      public Socket connection() throws Exception {
            return connection;
      }
}
