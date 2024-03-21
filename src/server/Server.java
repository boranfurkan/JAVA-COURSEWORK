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
import server.chat.User;


/**
 * Uses dependency injection pattern
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

    protected ArrayList<Group> getCurrentGroups() {
        return this.groups;
    }

    protected void addNewGroup(Group group) {
        this.groups.add(group);
    }

    public class Conn implements Runnable {
    
        private InputValidator inputHelper;
        private Socket client;
        private Group group;

        protected String usernameTemp;
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
                // Getting valid username
                this.output.println("Enter your unique username:");
                while(!this.inputHelper.isUsernameValid(usernameTemp)) {
                    this.usernameTemp = this.input.readLine().trim();
                    if (!this.inputHelper.isUsernameValid(this.usernameTemp)) {
                        this.output.println(inputHelper.getErrorDetails());
                    }
                }
                this.username = this.usernameTemp;

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
                        // TODO: remove connection....
                        output.println("Goodbye!"); 
                        this.group.removeUser(this.username);
                        off();
                    } else if (msg.equals("/details")) {
                        String detailsOutput = this.group.getUsersDetails();
                        output.println("Users connected to your group: \n" + detailsOutput); 
                    } else {
                        this.group.sendToAll(username + ": "+ msg);
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