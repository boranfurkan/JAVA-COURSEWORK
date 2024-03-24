package tests;

import org.junit.Test;
import static org.junit.Assert.*;

import server.InputValidator;


public class Tester {
    
    private InputValidator inputHelper;

    public Tester() {
        this.inputHelper = new InputValidator(null);
    }

    @Test
    /**
     * Tests InputValidator.isPortValid method to check user inputs 
     */
    public void isPortValidTest() {
        // Invalid port parameters
        String parameter1 = ""; 
        boolean isValid1 = this.inputHelper.isPortValid(parameter1);

        String parameter2 = "11111"; 
        boolean isValid2 = this.inputHelper.isPortValid(parameter2);

        String parameter3 = "111";
        boolean isValid3 = this.inputHelper.isPortValid(parameter3);
        
        String parameter4 = "    "; 
        boolean isValid4 = this.inputHelper.isPortValid(parameter4);

        String parameter5 = "111a";
        boolean isValid5 = this.inputHelper.isPortValid(parameter5);

        assertFalse(isValid1);
        assertFalse(isValid2);
        assertFalse(isValid3);
        assertFalse(isValid4);
        assertFalse(isValid5);

        // Valid port paramters
        String parameter6 = "1111";
        boolean isValid6 = this.inputHelper.isPortValid(parameter6);

        String parameter7 = "9191";
        boolean isValid7 = this.inputHelper.isPortValid(parameter7);
        
        assertTrue(isValid6);
        assertTrue(isValid7);
    }

    @Test
    public void isUsernameValidTest() {
        // Invalid parameters
        String parameter1 = "".trim(); 
        boolean isValid1 = this.inputHelper.isUsernameValid(parameter1, true);

        String parameter2 = "     ".trim(); 
        boolean isValid2 = this.inputHelper.isUsernameValid(parameter2, true);

        assertFalse(isValid1);
        assertFalse(isValid2);

        // Valid parameters
        String parameter3 = "John".trim(); 
        boolean isValid3 = this.inputHelper.isUsernameValid(parameter3, true);

        String parameter4 = "Super John".trim(); 
        boolean isValid4 = this.inputHelper.isUsernameValid(parameter4, true);

        String parameter5 = "John-111111".trim(); 
        boolean isValid5 = this.inputHelper.isUsernameValid(parameter5, true);

        assertTrue(isValid3);
        assertTrue(isValid4);
        assertTrue(isValid5);
    }
    
}
