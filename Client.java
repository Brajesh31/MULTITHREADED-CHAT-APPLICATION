
import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    public static int ServerPort = 5000;
    private static boolean running = true;

    public static void main(String[] args) throws UnknownHostException, IOException {
        Scanner scn = new Scanner(System.in);

        // Get the username
        System.out.print("Enter your username: ");
        String username = scn.nextLine();

        // Getting localhost IP
        InetAddress ip = InetAddress.getByName("localhost");

        // Establish the connection
        Socket socket = new Socket(ip, ServerPort);

        // Create input and output streams
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        // Inform server about username
        dos.writeUTF(username);

        // Thread to read messages from the server
        Thread readThread = new Thread(() -> {
            while (running) {
                try {
                    String msg = dis.readUTF();
                    System.out.println(msg);
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                    running = false;
                }
            }
        });
        readThread.start();

        // Thread to send messages to the server
        Thread sendThread = new Thread(() -> {
            while (running) {
                String line = scn.nextLine();
                try {
                    if (line.equalsIgnoreCase("/logout")) {
                        dos.writeUTF("logout");
                        running = false;
                        socket.close();
                        break;
                    } else {
                        dos.writeUTF(line);
                    }
                } catch (IOException e) {
                    System.out.println("Error sending message to server.");
                    running = false;
                }
            }
        });
        sendThread.start();
    }
}
