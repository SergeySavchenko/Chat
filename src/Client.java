import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Created by SergeySavchenko on 07.02.2018.
 */
public class Client {
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    public static final int PORT = 9001;
    public static void main(String[] args) {
        new Client();
    }
    public Client() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter IP address to connect to the server.");
        try {
            while (true) {
                try {
                    System.out.print("Format: xxx.xxx.xxx.xxx ");
                    String ip = scan.nextLine();
                    if (ip.equals("")) {
                        throw new UnknownHostException("Empty IP address. Try again");
                    }
                    socket = new Socket(ip, PORT);
                    break;
                } catch (SocketException e){
                    System.out.println("Can't connect to the server. Try again");
                } catch (UnknownHostException e) {
                    System.out.println("Incorrect IP address! Try again");
                }
            }
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            String result;
            while (true) {
                System.out.print("Enter yours nickname: ");
                out.println(scan.nextLine());
                result = in.readLine();
                if(result.equals("ACCEPTED")){
                    break;
                } else {
                    System.out.println(result);
                }
            }
            Resender resend = new Resender();
            resend.start();
            String str = "";
            System.out.println("Enter \"exit\" to EXIT THE CHAT");
            while (!str.equals("exit")) {
                str = scan.nextLine();
                out.println(str);
            }
            resend.setStop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }
    private void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("Threads haven't closed!!!");
        }
    }
    private class Resender extends Thread {
        private boolean stoped;
        public void setStop() {
            stoped = true;
        }
        @Override
        public void run() {
            try {
                while (!stoped) {
                    String str = in.readLine();
                    System.out.println(str);
                }
            } catch (IOException e) {
                System.err.println("Exception while receiving message");
                e.printStackTrace();
            }
        }
    }
}