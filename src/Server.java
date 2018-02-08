import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by SergeySavchenko on 07.02.2018.
 */
public class Server {
    public static final int PORT = 9001;
    public static void main(String[] args) {
        new Server();
    }
    private Set<Connection> connections = new HashSet<>();
    private Set<String> names = new HashSet<>();
    private ServerSocket server;
    public Server() {
        try {
            System.out.println("The chat server is running...");
            server = new ServerSocket(PORT);
            while (true) {
                Socket socket = server.accept();
                Connection con = new Connection(socket);
                connections.add(con);
                con.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private class Connection extends Thread {
        private BufferedReader in;
        private PrintWriter out;
        private Socket socket;
        private String name;
        public Connection(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }
        @Override
        public void run() {
            try {
                while(true) {
                    name = in.readLine();
                    if (name.equals("")) {
                        out.println("Empty name. Try again.");
                    } else if(names.contains(name)) {
                        out.println("Nickname \"" + name + "\" is already exists. Try again.");
                    } else {
                        out.println("ACCEPTED");
                        synchronized (names) {
                            names.add(name);
                        }
                        break;
                    }
                }
                System.out.println(name + " entered the chat," + " IP address: " + socket.getInetAddress());
                synchronized(connections) {
                    Iterator<Connection> iter = connections.iterator();
                    while(iter.hasNext()) {
                        iter.next().out.println(name + " entered the chat");
                    }
                }
                String str = "";
                while (true) {
                    str = in.readLine();
                    if(str.equals("exit")) break;
                    synchronized(connections) {
                        Iterator<Connection> iter = connections.iterator();
                        while(iter.hasNext()) {
                            iter.next().out.println(name + ": " + str);
                        }
                    }
                }
                synchronized(connections) {
                    System.out.println(name + " left the chat," + " IP address: " + socket.getInetAddress());
                    Iterator<Connection> iter = connections.iterator();
                    while(iter.hasNext()) {
                        iter.next().out.println(name + " left the chat");
                    }
                }
                synchronized(names) {
                    names.remove(name);
                }
            } catch (IOException e) {
                System.out.println(name + " left the chat," + " IP address: " + socket.getInetAddress());
                Iterator<Connection> iter = connections.iterator();
                while(iter.hasNext()) {
                    iter.next().out.println(name + " left the chat");
                }
                synchronized(names) {
                    names.remove(name);
                }
            } finally {
                close();
            }
        }
        public void close() {
            try {
                in.close();
                out.close();
                socket.close();
                connections.remove(this);
            } catch (Exception e) {
                System.err.println("Threads haven't closed!!!");
            }
        }
    }
}
