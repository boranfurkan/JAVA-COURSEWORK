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

// Tests
import org.junit.Test;


/**
 * Uses dependency injection pattern
 */
public class Server implements Runnable {

    private ArrayList<Conn> conns;
    private ArrayList<User> users;
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

    public String getUsersDetails() {
        String detailsOutput = "";
        for (Conn c : conns) {
            detailsOutput = detailsOutput + c.username + " - Address: 127.0.0.1:" + c.port + "\n";
        }
        return detailsOutput;
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

    protected ArrayList<Conn> getCurrentConns() {
        return this.conns;
    }

    public class Conn implements Runnable {
    
        private InputValidator inputHelper;
        private Socket client;
        private BufferedReader input;
        private PrintWriter output;

        protected String username;
        protected String usernameTemp;
        protected String port;

        public Conn(Socket client) {
            this.client = client;
            this.username = "";
            this.port = "";
        }

        @Override
        public void run() {
            try {
                output = new PrintWriter(client.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(client.getInputStream()));

                // Initiate helper to avoid wrong inputs and ensure unique users
                inputHelper = new InputValidator(getCurrentConns());
                
                output.println("Enter your unique username:");

                while(!inputHelper.isUsernameValid(usernameTemp)) {
                    usernameTemp = input.readLine().trim();
                    if (!inputHelper.isUsernameValid(usernameTemp)) {
                        output.println(inputHelper.getErrorDetails());
                    }
                }
                this.username = usernameTemp;

                output.println("Enter the port you want to connect:");
                port = "";
                while(!inputHelper.isPortValid(port)) {
                    port = input.readLine().trim();
                    if (!inputHelper.isPortValid(port)) {
                        output.println("Port must contain 4 digits!");
                    }
                }
                this.port = port;
                
                // Informing new user 
                System.out.println(this.username + " Connected - SERVER INFO.");
                output.println("Hello! " + this.username);
                sendToAll(username + " connected!");

                String msg;
                while ((msg = input.readLine()) != null) {
                    if (msg.equals("/quit")) {
                        // TODO: remove connection....
                        output.println("Goodbye!"); 
                        sendToAll(this.username + " disconnected.");
                        off();
                    } else if (msg.equals("/details")) {
                        String detailsOutput = getUsersDetails();
                        output.println("Users connected to your group: \n" + detailsOutput); 
                    } else {
                        sendToAll(username + ": "+ msg);
                    }
                    
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