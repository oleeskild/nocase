import java.net.*;
import java.io.*;
import sun.audio.*;

public class ncClient {
      // This clients socket connection that got accepted by the server
      private Socket connection;
      // The port which the server is listening on
      private int port;
      // Read messages sent by the server
      private BufferedReader response;
      // Send messages to the server
      private DataOutputStream client;
      // Constant string containing the name of our sound file
      private final String SOUND_FILE = "notification.wav";
      // Play a sound when a message is recieved
      private boolean useMsgSound;

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
            this.useMsgSound = true;
      }

      // Listens to incoming messages from server, on a new thread
      public void msgHandler() {
            (new Thread(){
                  @Override
                  public void run() {
                  while(true) {
                        try {
                              System.out.println(readMessage());

                              // Does this client want to have a sound notification?
                              if(useMsgSound) {
                                    playSound();
                              }
                        } catch (Exception ex) {
                              System.out.println("Connection Error! Aborting...");
                              System.exit(0);
                        }
                  }
                  }
            }).start();
      }

      // Set a new value for wether we should play notification sounds or not
      public void setNotificationStatus(boolean status) {
            this.useMsgSound = status;
      }

      // Return the current value of message notification
      public boolean getNotificationStatus() {
            return this.useMsgSound;
      }

      // Plays a sound to let the user know a new message was recieved
      public void playSound() {
            try {
                  // Opens an audiostream from the file specified at in SOUND_FILE
                  AudioStream audioStream = new AudioStream(new FileInputStream(new File(SOUND_FILE)));
                  AudioPlayer.player.start(audioStream);
            } catch (Exception e) {
                  // Sound could not be played
            }
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
