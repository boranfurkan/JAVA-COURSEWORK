package server;

import java.util.ArrayList;
import server.Server.Conn;

public class InputValidator {

    private ArrayList<Conn> conns;
    private String errorDetails;

    public InputValidator(ArrayList<Conn> conns) {
        this.conns = conns;
        this.errorDetails = null;
    }

    /**
     * Checks username validity
     * 
     * @param username
     * @return boolean
     * @throws IllegalArgumentException
     */
    public boolean isUsernameValid(String username) {
        try {
            if (isUsernameNotBlank(username) && isUsernameUnique(username)) {
                return true;
            }
        } catch (IllegalArgumentException exception) {
            this.errorDetails = exception.getMessage();
        }
        
        return false;
    }

    /**
     * Ensures username is not blank nor null
     * 
     * @return boolean
     * @throws IllegalArgumentException
     */
    private boolean isUsernameNotBlank(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username can't be blank!");
        }
        return true;
    }

    /**
     * Ensures username is unique
     * 
     * @param username
     * @return boolean
     * @throws IllegalArgumentException
     */
    private boolean isUsernameUnique(String username) {
        // Ensure it is unique
        for (Conn c : this.conns) {
            System.out.println("checking username... " + username + " =? " + c.username);
            if (c.username.equals(username)) {
                throw new IllegalArgumentException("Username already connected, please choose another one.");
            } 
        }
        return true;
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

    public String getErrorDetails() {
        return this.errorDetails;
    }

}
