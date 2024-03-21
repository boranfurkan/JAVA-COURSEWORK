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

    public void send(String msg) {
        this.conn.output.println(msg);
    }

    public void makeAdmin() {
        this.isAdmin = true;
    }
}
