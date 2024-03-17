package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server implements Runnable {

    private ArrayList<Conn> conns;
    private ExecutorService threads;
    private ServerSocket srv;
    private boolean running;

    public Server() {
        conns = new ArrayList<>();
        running = true;
    }

    @Override
    public void run() {
        try {
            // Listening to port 3331
            srv = new ServerSocket(3331);
            threads = Executors.newCachedThreadPool();

            while (running) {
                // Getting client obj
                Socket client = srv.accept();

                // Creating and executing connection obj
                Conn connection = new Conn(client);
                conns.add(connection);
                threads.execute(connection);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
    /**
     * Broadcasts the parameter message to all users of the group
     * 
     * @param msg Message that will be broadcasted
     */
    public void sendToAll(String msg) {
        for (Conn c : conns) {
            if (c == null) {
                continue;
            }
            c.send(msg);
        }
    }

    /**
     *  Shutdown the server
     */
    public void off() {
        running = false;
        try {
            threads.shutdown();
            if (!srv.isClosed()) {
                srv.close();
            }
            for (Conn c : conns) {
                c.off();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

    public class Conn implements Runnable {
    
        private InputValidator helper;
        private Socket client;
        private BufferedReader input;
        private PrintWriter output;

        private String username;

        public Conn(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                output = new PrintWriter(client.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                
                helper = new InputValidator();
                username = "";
                System.out.println("Asking username...");
                output.println("Enter your unique username: ");
                
                while(!helper.isUsernameValid(username)) {
                    username = input.readLine().trim();
                    if (!helper.isUsernameValid(username)) {
                        output.println("Username cannot be blank!");
                    }
                }
                System.out.println(username + " Connected - SERVER INFO.");

                output.println("Hello! " + username);
                sendToAll(username + " connected!");

                String msg;
                while ((msg = input.readLine()) != null) {
                    System.out.println("hey");
                    if (msg.equals("/quit")) {
                        off();
                    }
                    sendToAll(username + ": "+ msg);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                off();
            }
        }

        public void send(String msg) {
            output.println(msg);
        }

        public void off() {
            try {
                input.close();
                output.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        Server srv = new Server();
        srv.run();
    }

}