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

import server.chat.Group;


/**
 * Uses dependency injection pattern to run a server
 * that handles client connections into chat grops
 * 
 */
public class Server implements Runnable {

    private ArrayList<Group> groups;
    private ArrayList<Conn> conns;
    private ExecutorService threads;
    private ServerSocket srv;
    private boolean running;

    public Server() {
        conns = new ArrayList<>();
        groups = new ArrayList<>();
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
     *  Shutdown the server
     */
    public void off() {
        this.running = false;
        try {
            this.threads.shutdown();
            if (!this.srv.isClosed()) {
                this.srv.close();
            }
            for (Conn c : this.conns) {
                c.off();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

    /**
     * 
    * @return list of all connections of the server
     */
    protected ArrayList<Conn> getCurrentConns() {
        return this.conns;
    }

    /**
     * Remove a connection from list of all connections
     * 
     * @param connection
     */
    protected void removeConn(Conn connection) {
        this.conns.remove(connection);
    }

    /**
     * 
     * @return list of all groups of the server
     */
    protected ArrayList<Group> getCurrentGroups() {
        return this.groups;
    }

    /**
     * Adds new group to list
     * 
     * @param group
     */
    protected void addNewGroup(Group group) {
        this.groups.add(group);
    }

    public class Conn implements Runnable {
    
        private InputValidator inputHelper;
        private Socket client;
        private Group group;

        protected String port;

        public BufferedReader input;
        public PrintWriter output;
        public String username;


        public Conn(Socket client) {
            this.client = client;
            this.username = "";
            this.port = "";
            this.inputHelper = new InputValidator(getCurrentConns());
            try {
                this.output = new PrintWriter(client.getOutputStream(), true);
                this.input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                if (this.client.isClosed()) {
                    System.out.println("USERNAME TERMIANTEDDDD");
                }

                // Getting valid username
                String usernameTemp = "";
                this.output.println("Enter your unique ID:");
                while(!this.inputHelper.isUsernameValid(usernameTemp, false)) {
                    usernameTemp = this.input.readLine().trim();
                    if (!this.inputHelper.isUsernameValid(usernameTemp, false)) {
                        this.output.println(inputHelper.getErrorDetails());
                    }
                }
                this.username = usernameTemp;

                // Getting valid port
                this.output.println("Enter the port you want to connect:");
                this.port = "";
                while(!this.inputHelper.isPortValid(this.port)) {
                    this.port = input.readLine().trim();
                    if (!this.inputHelper.isPortValid(this.port)) {
                        this.output.println("Port must contain 4 digits!");
                    }
                }
                
                // Adding user to respective group
                for (Group g : getCurrentGroups()) {
                    if (g.port.equals(this.port)) {
                        this.group = g;
                    }
                }

                // If group did not exist
                if (this.group == null) {
                    Group g = new Group(this.port);
                    addNewGroup(g);
                    this.group = g;
                }

                // Informing new user
                this.group.addUser(this);
                this.group.sendToAll(this.username + " connected!");
                
                // Connection stays here now until it leaves
                String msg;
                while ((msg = this.input.readLine()) != null) {
                    if (msg.equals("/quit")) {
                        this.output.println("Goodbye!"); 
                        removeConn(this);
                        this.group.removeUser(this.username);
                        this.inputHelper.updateConns(getCurrentConns());
                        off();
                    } else if (msg.equals("/details")) {

                        // Getting details of group members
                        this.output.println("Users connected to your group: \n" + this.group.getUsersDetails()); 
                    } else if (msg.substring(0, Math.min(msg.length(), 4)).equals("/to ")) {

                        // Private mesages
                        String[] splitMsg = msg.trim().split("\\s+");
                        boolean sent = this.group.sendPrivateMessage(splitMsg[2], this.username, splitMsg[1]);
                        if (sent) {
                            this.output.println("Message sent");
                        } else {
                            this.output.println("User not found. Plese check the users: \n" + this.group.getUsersDetails());
                        }
                    }
                    else {
                        // Broadcasting
                        this.group.sendToAll(username + ": "+ msg);
                    }
                }

                // Client socket suddenly disconnected
                this.group.removeUser(this.username);
                removeConn(this);
                this.inputHelper.updateConns(getCurrentConns());
                this.off();
            } catch (IOException exception) {
                exception.printStackTrace();
                this.off();
            }
        }

        /**
         * Closes IO and client socker
         */
        public void off() {
            try {
                this.input.close();
                this.output.close();
                if (!this.client.isClosed()) {
                    this.client.close();
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