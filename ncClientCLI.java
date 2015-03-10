import java.net.*;
import java.io.*;

public class ncClientCLI {
      // Create a new client and establish a connection to the given host
      public static void main(String[] args) {
            try {
                  BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                  System.out.print("Connect to host: ");

                  String host = br.readLine();
                  int port = 1337;

                  ncClient client = null;

                  // Debug flag prints messages to syso
                  if(args.length >0){
                        // Should enable debug messages
                  	if(args[0].toUpperCase().equals("-DEBUG")){
                              client = new ncClient(host,port,true);
                        }
                  }else{
                        // Do not enable debug messages
                        client = new ncClient(host,port,false);
                  }


                  msgHandler(client);

                  // Connected successfully, query user for messages
                  boolean exit = false;
                  do {
                        String msg = br.readLine();
                        // Aborted by user?
                        if(msg.equals("/quit") || msg.equals("/exit")) {
                              exit = true;
                              break;
                        } else if (msg.equals("/mute")) {
                              client.setNotificationStatus(!client.getNotificationStatus());
                        } else {
                              client.sendMessage(msg);
                        }
                  } while(!exit);
                  client.connection().close();
            } catch (Exception ex) {
                  System.out.println("An error has occurred! Aborting...");
            }
            System.exit(0);
      }

      // Listens to incoming messages from server, on a new thread
      public static void msgHandler(ncClient client) {
            (new Thread(){
                  @Override
                  public void run() {
                  while(true) {
                        try {
                              // Gets message from server
                              System.out.println(client.readMessage());

                        } catch (Exception ex) {
                              System.out.println("Connection Error! Aborting...");
                              System.exit(0);
                        }
                  }
                  }
            }).start();
      }
}
