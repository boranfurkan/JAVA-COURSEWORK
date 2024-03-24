package server.chat;

import server.Server.Conn;

public class User {
    
    public String username;
    public Integer port;
    public boolean isAdmin;
    private Conn conn;

    public User(Conn connection, boolean isAdmin) {
        this.conn = connection;
        this.username = connection.username;
        this.isAdmin = isAdmin;
    }

    /**
     * Displays the message parameters to this user
     * 
     * @param msg
     */
    public void send(String msg) {
        this.conn.output.println(msg);
    }

    /**
     * User gains Admin attribute
     */
    public void makeAdmin() {
        this.isAdmin = true;
    }
}
