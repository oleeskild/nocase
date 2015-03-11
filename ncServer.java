import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

// self signed pki

public class ncServer {
      // Socket-Listener for this server
      private final ServerSocket listener;

      // List of all threads, each client has their own thread to
      // handle incoming and outgoing messages.
      private final ArrayList<Thread> threads;

      // List of peers that are connected, needed / handles the broadcastMessage
      // function. Might also be needed to handle disconnection of clients.
      private final HashMap<String,Peer> connections;

      // Port this server is listening on
      private final int port;

      // Enables debug
      private final boolean debug;

      // Message of the day
      private String motd;

      // Set default date format to use to prefix messages
      private final static DateFormat dateFormat = new SimpleDateFormat("HH:mm");

      public ncServer(int port, boolean debug) throws Exception {
            // Sets port to listen on
            this.port = port;
            // Sets debug flag
            this.debug = debug;
            // Initialize our listening socket
            this.listener = new ServerSocket(port);
            // Initialize our thread array
            this.threads = new ArrayList<Thread>();
            // Initialize connections/peer array
            this.connections = new HashMap<String,Peer>();
            // Set default startup message of the day
            this.motd  = "Welcome to Operation Nocase.";

            // DEBUG -- Prints now listening for peers
            if(debug)
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
                        final Peer p = new Peer(newClient);

                        // Create a working thread for this peer
                        Thread t = new Thread() {
                        public void run() {
                              // Loop forever
                              while(true) {
                                    try {
                                          String msg = p.readMessage();
                                	         //Checks for commands
                                        	 	if(msg.startsWith("/")){
                                                // Processes the commands and performs the approriate action
                                  	  		processCommands(p,msg);
                                  	  	}else{
                                                // Processes the message and passes it to the broadcast function
                                               processMessage(p,msg);
                                  	  	}
                                    } catch (Exception ex) {
                                          try {
                                                // Connection error, closing connnection and stopping thread
                                                userDisconnected(p);
                                          } catch (Exception exx) {
                                                // Exception while closing socket?
                                          }
                                    }
                              }
                        }};

                        // Add the thread for this new client to our list
                        threads.add(t);
                        // Start this thread
                        t.start();

                        // Check for duplicate ips, give random nickname if needed
                        String nick = newClient.getInetAddress().toString();
                        while(connections.containsKey(nick)) {
                              nick = "guest_" + (new Random()).nextInt(100);
                        }
                        // Give this peer a nickname
                        p.setNickname(nick);
                        // Broadcast to all connected clients that a new peer has joined the chat!
                        broadcastMessage(nick + " connected!");
                        // Add our new client to the list of connected peers
                        connections.put(nick, p);

                        // DEBUG -- Prints the IP of the client that connected to server console
                        if(debug)
                              System.out.println(newClient.getInetAddress() + " connected!");

                  } catch (Exception ex) {
                        //Exception dont add new connection
                  }
                  }
            }
            }).start();
      }


      // Processes the commands and performs the requested action
      public void processCommands(Peer p, String msg) throws Exception {
            if(msg.startsWith("/nick ")){
                  // Store old name temporarily
                  String oldName = p.getNickname();
                  // Check if name exists
                  String newName = msg.substring(6, msg.length());
                  if(!connections.containsKey(newName)) {
                        // Remove old nick-entry from hashmap (list of connected peers)
                        connections.remove(oldName);
                        // Set the new name
                        p.setNickname(newName);
                        // Create a key for the new nickname in our hashmap
                        connections.put(newName, p);
                        // Let other Peer know who this person is/was
                        broadcastMessage("<" + oldName + "> is now known as <" + p.getNickname() + ">");
                  } else {
                        p.sendMessage("Requested nickname is taken!");
                  }
            } else if(msg.equals("/motd")) {
                  // Message of the day was requested from this specific user
                  requestMotd(p);
            } else if (msg.startsWith("/setmotd ")) {
                  // Updates the message of the day and prints it to all connected Peer
                  setMotd(p.getNickname(), msg.substring(9, msg.length()));
            } else if (msg.equals("/list")) {
                  StringBuilder list = new StringBuilder();
                  // Adds the 'LIST' prefix
                  list.append("LIST ");
                  // Adds the nickname of all connected peers
                  int cnt = 0;
                  for(Peer pList : connections.values()) {
                        list.append(pList.getNickname());
                        if(cnt < connections.values().size())
                              list.append(" ");
                        cnt++;
                  }
                  // Sends a messsage with all connected users as requested
                  p.sendMessage(list.toString());
            } else if (msg.startsWith("/pm ")) {
                  String info[] = msg.split(" ");
                  // Check that a reciever was given
                  if(info.length > 1) {
                        String reciever = info[1];
                        // If reciever exists process message
                        if(!reciever.equals("") && connections.containsKey(reciever)) {
                              Peer pTar = connections.get(reciever);
                              StringBuilder msgTar = new StringBuilder();
                              msgTar.append("PM <" + p.getNickname() + ">: ");

                              for(int i = 2; i < info.length;i++) {
                                    msgTar.append(info[i] + " ");
                              }

                              // Broadcast message to both reciever and sender
                              p.sendMessage(msgTar.toString());
                              pTar.sendMessage(msgTar.toString());
                        }
                  }
            }
      }

      // Processes the message and passes it to the broadcastMessage function
      public void processMessage(Peer p, String msg) throws Exception {
            // Empty messages are not allowed
            if(!msg.equals("")) {
                  // Reads message and adds sender IP/nickname as name.
                  msg = "<" + p.getNickname() + ">: " + msg;
                  // Broadcast message to every client connected.
                  broadcastMessage(msg);
                  // DEBUG -- Prints message to server terminal
                  if(debug)
                        System.out.println(msg);
            }
      }

      // Broadcasts the message "msg" to all connected peers
      public void broadcastMessage(String msg) throws Exception {
            for(Peer p : connections.values()) {
                  p.sendMessage(msg);
            }
      }

      // Message of the day has changed, broadcast to all peers
      public void setMotd(String changedBy, String newMsg) throws Exception {
            this.motd = newMsg;
            broadcastMessage("<Motd> -> " + this.motd + " <- changed by " + changedBy);
      }

      // A specific peer wants to know the message of the day
      public void requestMotd(Peer p) throws Exception {
            p.sendMessage("<Motd> -> " + this.motd);
      }

      // When a user disconnects, close connection and remove from the appropriate lists
      public void userDisconnected(Peer p) throws Exception {
            // Broadcast message - user has left
            String abortMsg = "User " + p.getNickname() + " disconnected!";
            // Remove thread from active peer
            threads.remove(Thread.currentThread());
            // Close connection
            p.connection().close();
            // Remove peer from list
            connections.remove(p.getNickname());
            // Tell other peers that you left
            broadcastMessage(abortMsg);

            // DEBUG -- Print disconnect message to server console
            if(debug)
                  System.out.println(abortMsg);

            // Stop thread
            Thread.currentThread().stop();
      }

      // Returns timestamp, used to display when messages was sent
      public static String getTimeStamp() {
            return dateFormat.format(new Date());
      }

      // Info stored from a peer/client that connects to the server
      private static class Peer {
            // Socket connection that was accepted by server
            private final Socket connection;
            // Read messages sent by the server
            private BufferedReader response;
            // Send a message to the server
            private DataOutputStream client;
            // Nickname for this user
            private String nickname;

            // Initialize connection to let our peer be able to interact with the server
            public Peer(Socket connection) throws Exception {
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
                  this.client.write(("[" + getTimeStamp() +"] " + s + "\n").getBytes("UTF-8"));
            }

            // Returns the Socket connection for this peer
            public Socket connection() {
                  return this.connection;
            }
      }

      // Start server on the given port
      public static void main(String[] args) throws Exception {
            // Listen on port
            int port = 1337;
            // Check for command line parameters
            if(args.length >0){
                  // Should enable debug messages
            	if(args[0].toUpperCase().equals("-DEBUG")){
            		ncServer server = new ncServer(port, true);
                  }
            }else{
                  // Do not enable debug messages
            	ncServer server = new ncServer(port, false);
            }
      }
}
