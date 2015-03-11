import java.net.*;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.sound.sampled.*;

public class ncClient {
      // This clients socket connection that got accepted by the server
      private Socket connection;
      // The port which the server is listening on
      private int port;
      // Read messages sent by the server
      private BufferedReader response;
      // Send messages to the server
      private DataOutputStream client;
      // Clients nickname given by server
      private String nickname;
      // Constant string containing the name of our sound file
      private final String SOUND_FILE = "notification.wav";
      // Play a sound when a message is recieved
      private boolean useMsgSound;
      // Debug flag
      private final boolean debug;
      // Set default date format to use to prefix messages
      private final static DateFormat dateFormat = new SimpleDateFormat("HH:mm");


      // Try to connect to server
      public ncClient(String host, int p, boolean debug) throws Exception {
            // Set port to  connect to
            this.port = p;
            // Set debug
            this.debug = debug;

            // Prints info
            if(debug)
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
            if(debug)
                  System.out.println("Connected...");

            // Update streams for this connection
            this.response = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
            this.client = new DataOutputStream(this.connection.getOutputStream());

            // Request message of the day when connecting
            this.sendMessage("/motd");
            this.useMsgSound = true;
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
      public void playNotificationSound() {
            try {
                  AudioInputStream stream = AudioSystem.getAudioInputStream(new File(SOUND_FILE));
                  AudioFormat format = stream.getFormat();
                  DataLine.Info info = new DataLine.Info(Clip.class, format);
                  Clip clip = (Clip) AudioSystem.getLine(info);
                  clip.open(stream);
                  clip.start();
            } catch (Exception e) {
                  // Sound could not be played
            }
      }

      // Query server for nickname
      public void queryNickname() throws Exception {
            sendMessage("/getNick");
      }

      // Return clients nickname
      public String getNickname() {
            return this.nickname;
      }

      // Updates local nickname variable
      // PS: can, but SHOULD not be used without queryNickname()
      public void setNickname(String nick) {
            this.nickname = nick;
      }

      // Read message from the server
      public String readMessage() throws Exception {
            String msg = this.response.readLine();
            // could not read message from server, possible connection error
            if(msg == null) {
                  if(debug) {
                        System.out.println("Connection Error! Aborting...");
                        System.exit(0);
                  } else {
                        return "Connection Error!";
                  }
            }
            return msg;
      }

      // Send message to the server
      public void sendMessage(String s) throws Exception {
            this.client.write((s + "\n").getBytes("UTF-8"));
      }

      // Returns timestamp, used to display when messages was sent
      public static String getTimeStamp() {
            return dateFormat.format(new Date());
      }

      // Returns this socket connection that was accepted by the server
      public Socket connection() throws Exception {
            return connection;
      }
}
