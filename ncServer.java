import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class ncServer {
      private final ServerSocket listener;

      // List of all threads, each client has their own thread to
      // handle incoming and outgoing messages.
      private final ArrayList<Thread> threads;

      // List of peers that are connected, needed / handles the broadcastMessage
      // function. Might also be needed to handle disconnection of clients.
      private final ArrayList<peers> connections;

      // Port this server is listening on
      private final int port;

      public ncServer(int port) throws Exception {
            // Sets port to listen on
            this.port = port;
            // Initialize our listening socket
            listener = new ServerSocket(port);
            // Initialize our thread array
            threads = new ArrayList<Thread>();
            // Initialize connections/peer array
            connections = new ArrayList<peers>();
            // Prints now listening for peers
            System.out.println("Listening for peers...");

            // Start listening for incoming connections on a separate thread.
            listen();
      }

      // Listening for incoming connections (clients requesting)
      public void listen() throws Exception {
            // Start new thread for listening
            (new Thread(){
                  @Override
                  public void run() {
                        while(true) {
                        try {

                        //removeDeadConnections();

                        // Accept incoming connection
                        Socket newClient = listener.accept();
                        // Add to our peer object
                        peers p = new peers(newClient);

                        // Create a working thread for this peer
                        Thread t = new Thread() {
                              public void run() {
                                    // Loop forever
                                    while(true) {
                                          try {
                                                // Reads message and adds sender IP as name.
                                                String msg = p.connection().getInetAddress() + ": " + p.readMessage();
                                                // Empty messages are not allowed
                                                if(!msg.equals(p.connection().getInetAddress() + ": ")) {
                                                      // Broadcast message to every client connected.
                                                      broadcastMessage(msg);
                                                      // DEBUG -- Prints message to server terminal
                                                      System.out.println(msg);
                                                }
                                          } catch (Exception ex) {
                                                try {
                                                      // Tell sender that his or her message could not be sent
                                                      p.sendMessage("An exception occurred and your message was not sent!");
                                                } catch (Exception exx) {
                                                }
                                          }
                                    }
                              }
                        };

                        // Update threadID of peer
                        p.setThreadID(t.getId());
                        
                        // Add the thread for this new client to our list
                        threads.add(t);
                        // Start this thread
                        t.start();
                        // Broadcast to all connected clients that a new peer has joined the chat!
                        broadcastMessage(newClient.getInetAddress() + " connected!");
                        // Add our new client to the list of connected peers
                        connections.add(p);
                        // Prints the IP of the client that connected to server console
                        System.out.println(newClient.getInetAddress() + " connected!");

                  } catch (Exception ex) {
                        //Exception dont add new connection
                  }
                  }
            }
            }).start();
      }

      // Broadcasts the message "msg" to all connected peers
      public void broadcastMessage(String msg) throws Exception {
            for(peers p : connections) {
                  p.sendMessage(msg);
            }
      }

      // Handle disconnection of clients
      public void removeDeadConnections() {
            // TODO
      }

      // Info stored from a peer/client that connects to the server
      private static class peers {
            // Socket connection that was accepted by server
            private final Socket connection;
            // Read messages sent by the server
            private BufferedReader response;
            // Send a message to the server
            private DataOutputStream client;
            // ID of the thread this peer belongs to
            private int threadID;

            // Initialize connection to let our peer be able to interact with the server
            public peers(Socket connection) throws Exception {
                  this.connection = connection;
                  this.response = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
                  this.client = new DataOutputStream(this.connection.getOutputStream());
            }

            // Update thread id
            public void setThreadID(int id) {
                  this.threadID = id;
            }

            // Get this peers thread id
            public int getThreadID() {
                  return this.threadID;
            }

            // Read a message from server
            public String readMessage() throws Exception {
                  return this.response.readLine();
            }

            // Send a message to the server
            public void sendMessage(String s) throws Exception {
                  this.client.writeBytes(s+"\n");
            }

            // Returns the Socket connection for this peer
            public Socket connection() {
                  return this.connection;
            }
      }

      // Start server on the given port
      public static void main(String[] args) throws Exception {
            int port = 1337;
            ncServer server = new ncServer(1337);
      }
}
