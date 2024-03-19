package server;

import java.util.ArrayList;

import server.Server.Conn;

public class InputValidator {

    private ArrayList<Conn> conns;

    public InputValidator(ArrayList<Conn> conns) {
        this.conns = conns;
    }

    /**
     * Check username input validity
     * 
     * @param username
     */
    public boolean isUsernameValid(String username) {
        return !username.isEmpty();
    }

    /**
     * Check if port is not null and has exactly 4 digit characters
     * 
     * @param port
     */
    public boolean isPortValid(String port) {
        if (port != null && port.length() == 4) {
            for (char c : port.toCharArray()) {
                if (!Character.isDigit(c)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


}
