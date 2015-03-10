import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class ncServer {
      // Socket-Listener for this server
      private final ServerSocket listener;

      // List of all threads, each client has their own thread to
      // handle incoming and outgoing messages.
      private final ArrayList<Thread> threads;

      // List of peers that are connected, needed / handles the broadcastMessage
      // function. Might also be needed to handle disconnection of clients.
      private final ArrayList<Peers> connections;

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
            connections = new ArrayList<Peers>();

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

                        // Accept incoming connection
                        Socket newClient = listener.accept();
                        // Add to our peer object
                        Peers p = new Peers(newClient);

                        // Create a working thread for this peer
                        Thread t = new Thread() {
                              public void run() {
                                    // Loop forever
                                    while(true) {
                                          try {
                                                String msg = p.readMessage();
                                        	  	//Checks for commands
                                        	  	if(msg.startsWith("/")){
                                        	  		if(msg.startsWith("/nick")){
                                        	  			String oldName = p.getNickname();
                                        	  			p.setNickname(msg.substring(6, msg.length()));
                                        	  			broadcastMessage("<" + oldName + "> is now known as <" + p.getNickname() + ">");
                                        	  		}
                                        	  	}else{
	                                                // Reads message and adds sender IP/nickname as name.
	                                                msg = p.getNickname() + ": " + msg;
	                                                // Empty messages are not allowed
	                                                if(!msg.equals(p.getNickname() + ": ")) {
	                                                      // Broadcast message to every client connected.
	                                                      broadcastMessage(msg);
	                                                      // DEBUG -- Prints message to server terminal
	                                                      System.out.println(msg);
	                                                }
                                        	  	}
                                          } catch (Exception ex) {
                                                // Connection error, closing connnection and stopping thread
                                                try {
                                                      // Broadcast message - user has left
                                                      String abortMsg = "User " + p.getNickname() + " disconnected!";

                                                      // Remove thread from active peers
                                                      threads.remove(Thread.currentThread());
                                                      // Close connection
                                                      p.connection().close();
                                                      // Remove peer from list
                                                      connections.remove(p);
                                                      // Tell other peers that you left
                                                      broadcastMessage(abortMsg);
                                                      // Stop thread
                                                      Thread.currentThread().stop();

                                                      // DEBUG -- Print disconnect message to server console
                                                      System.out.println(abortMsg);
                                                } catch (Exception exx) {
                                                      // Exception while closing socket?
                                                }
                                          }
                                    }
                              }
                        };

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
            for(Peers p : connections) {
                  p.sendMessage(msg);
            }
      }

      // Info stored from a peer/client that connects to the server
      private static class Peers {
            // Socket connection that was accepted by server
            private final Socket connection;
            // Read messages sent by the server
            private BufferedReader response;
            // Send a message to the server
            private DataOutputStream client;
            // Nickname for this user
            private String nickname;

            // Initialize connection to let our peer be able to interact with the server
            public Peers(Socket connection) throws Exception {
                  this.connection = connection;
                  this.response = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
                  this.client = new DataOutputStream(this.connection.getOutputStream());
                  this.nickname = this.connection.getInetAddress().toString();
            }

            // Nickname for this peer
            public String getNickname() {
                  return this.nickname;
            }

            // Set new nickname
            public void setNickname(String nick) {
                  this.nickname = nick;
            }

            // Read a message from server
            public String readMessage() throws Exception {
                  return this.response.readLine();
            }

            // Send a message to the server
            public void sendMessage(String s) throws Exception {
                  this.client.write((s+"\n").getBytes("UTF-8"));
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
