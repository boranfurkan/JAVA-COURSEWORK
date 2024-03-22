package server.chat;

import java.util.ArrayList;

import server.Server.Conn;


public class Group {

    public String port;
    private ArrayList<User> users;
    private User admin;

    public Group(String port) {
        this.port = port;
        this.users = new ArrayList<>();
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
    }

    /**
     * Removes user from the group and pick new admin
     * 
     * @param username
     */
    public void removeUser(String username) {
        // Removes user by username match, as it is unique
        for (User u : this.users) {
            if (u.username.equals(username)) {
                this.users.remove(u);
                sendToAll(username + " disconnected.");
                
                // Picks new admin if necessary
                if (u.isAdmin && !this.users.isEmpty()) {
                    User newAdmin = this.users.getFirst();
                    newAdmin.makeAdmin();
                    sendToAll(newAdmin.username + " is the new admin.");
                }
            }
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
    
}
