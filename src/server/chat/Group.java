package server.chat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import server.Server.Conn;


public class Group {

    public String port;
    private ArrayList<User> users;
    private User admin;
    private ScheduledExecutorService executor;
    private boolean statusCheckRunning;
    private long periodicalCheckTimeSeconds = 30;

    public Group(String port) {
        this.port = port;
        this.users = new ArrayList<>();
        this.executor = Executors.newScheduledThreadPool(1);
        this.statusCheckRunning = false;
    }

    /**
     * Add a new user to the group
     * 
     * @param connection
     */
    public void addUser(Conn connection) {
        // If first user to connect, set admin to him
        boolean isAdmin = false;
        if (this.users.isEmpty()) {
            isAdmin = true;
        }
        User newUser = new User(connection, isAdmin);
        this.users.add(newUser);

        // Informing the user about current admin
        if (isAdmin) {
            this.admin = newUser;
            newUser.send("You are the admin of this group!");
        } else {
            newUser.send(this.admin.username + " is the admin.");
        }

        // Enables periodical checks if more than 1 member
        if (!this.statusCheckRunning) {
            this.enablePeriodicalStatusChecks(this.periodicalCheckTimeSeconds);
        }

    }

    /**
     * Removes user from the group and pick new admin
     * 
     * @param username
     */
    public void removeUser(String username) {
        // Removes user by username match, as it is unique
        Iterator<User> iterator = this.users.iterator();

        while (iterator.hasNext()) {
            User userIteration = iterator.next();
            if (userIteration.username.equals(username)) {
                iterator.remove();
                sendToAll(username + " disconnected.");
                
                // Picks new admin if necessary
                if (userIteration.isAdmin && !this.users.isEmpty()) {
                    User newAdmin = this.users.getLast();
                    newAdmin.makeAdmin();
                    sendToAll(newAdmin.username + " is the new admin.");
                }
            }
        }
        
        // Disabling status check if only 1 user
        if (this.users.size() == 1) {
            this.statusCheckRunning = false;
            this.executor.shutdownNow();
            this.executor = Executors.newScheduledThreadPool(1);
            System.out.println("Disabling periodical checks, users: " + this.users.size());
        } 
    }

    /**
     * Provide details (Username and address) of all group members
     * 
     * @return String detailsOutput 
     */
    public String getUsersDetails() {
        String detailsOutput = "";
        for (User u : this.users) {
            if (u.isAdmin) {
                detailsOutput = detailsOutput + "ADMIN - ";
            }
            detailsOutput = detailsOutput + u.username + " - Address: 127.0.0.1:" + this.port + "\n";
        }
        return detailsOutput;
    }

    /**
     * Broadcasts the parameter message to all users of the group
     * 
     * @param msg Message that will be broadcasted
     */
    public void sendToAll(String msg) {
        for (User u : this.users) {
            if (u == null) {
                continue;
            }
            u.send(msg);
        }
    }

    /**
     * Sends private message between users, true if sent
     * 
     * @param msg
     * @param fromUsername
     * @param toUsername
     */
    public boolean sendPrivateMessage(String msg, String fromUsername, String toUsername) {
        User receiver = findUserByUsername(toUsername);
        if (receiver == null) {
            return false;
        } else {
            receiver.send("(Private) " + fromUsername + ": " + msg);
            return true;
        }
    }

    /**
     * Gives the user object by searching its username in the group list
     * 
     * @param username
     * @return User || null
     */
    public User findUserByUsername(String username) {
        for (User u : this.users) {
            if (u == null) {
                continue;
            }
            if (u.username.equals(username)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Starts status-check threads if more than one user is in the group.
     * The status-check informs the admin about curent members connection
     * 
     * @param long seconds 
     */
    public void enablePeriodicalStatusChecks(long seconds) {
        if (this.admin != null && this.users.size() > 1) {
            this.executor.scheduleAtFixedRate(this::informMemberStatus, 0, seconds,TimeUnit.SECONDS);
            this.statusCheckRunning = true;
        }
    }

    /**
     * Informs the admin about curent members connection
     * 
     */
    public void informMemberStatus() {
        String info = "STATUS UPDATES: \n";
        for (User u : this.users) {
            info = info + u.username + " is online. \n";
        }
        this.admin.send(info);
    }
    
}
