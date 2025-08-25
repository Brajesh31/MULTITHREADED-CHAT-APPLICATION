
import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    static CopyOnWriteArrayList<ClientHandler> clientList = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Server started on port 5000...");

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client connected: " + socket);

            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            String username = dis.readUTF();

            ClientHandler clientHandler = new ClientHandler(socket, username, dis, dos);
            Thread clientThread = new Thread(clientHandler);

            clientList.add(clientHandler);
            clientThread.start();
        }
    }
}

class ClientHandler implements Runnable {

    private final String username;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Socket socket;
    private boolean isLoggedIn;

    public ClientHandler(Socket socket, String username, DataInputStream dis, DataOutputStream dos) {
        this.socket = socket;
        this.username = username;
        this.dis = dis;
        this.dos = dos;
        this.isLoggedIn = true;
    }

    @Override
    public void run() {
        String received;

        while (isLoggedIn) {
            try {
                received = dis.readUTF();

                if (received.equalsIgnoreCase("logout")) {
                    try (this.socket) {
                        isLoggedIn = false;
                    }
                    removeClient();
                    System.out.println(username + " has logged out.");
                    break;
                } else if (received.equalsIgnoreCase("/list")) {
                    listActiveClients();
                } else {
                    broadcastMessage(username + ": " + received);
                }
            } catch (IOException e) {
                System.out.println("Connection error with client: " + username);
                isLoggedIn = false;
                removeClient();
                break;
            }
        }

        try {
            dis.close();
            dos.close();
        } catch (IOException e) {
            System.out.println("Error closing resources for client: " + username);
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler client : Server.clientList) {
            if (client.isLoggedIn && !client.username.equals(username)) {
                try {
                    client.dos.writeUTF(message);
                } catch (IOException e) {
                    System.out.println("Error broadcasting message to " + client.username);
                }
            }
        }
    }

    private void listActiveClients() {
        try {
            dos.writeUTF("Active clients:");
            for (ClientHandler client : Server.clientList) {
                if (client.isLoggedIn) {
                    dos.writeUTF("- " + client.username);
                }
            }
        } catch (IOException e) {
            System.out.println("Error listing active clients for: " + username);
        }
    }

    private void removeClient() {
        Server.clientList.remove(this);
    }
}
