package server.chat;

import java.util.ArrayList;

import server.Server.Conn;


public class Group {

    public String port;
    private ArrayList<User> users;

    public Group(String port) {
        this.port = port;
        this.users = new ArrayList<>();
    }

    public void addUser(Conn connection) {
        // If first user to connect, set admin to him
        boolean isAdmin = false;
        if (this.users.isEmpty()) {
            isAdmin = true;
        }
        User newUser = new User(connection, isAdmin);
        this.users.add(newUser);

        // Informing the user that he is the admin
        newUser.send("You are the admin of this group!");
    }

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
    
}
