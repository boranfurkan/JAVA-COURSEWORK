package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

    private Socket client;
    private BufferedReader input;
    private PrintWriter output;
    private boolean quit;

    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 3331);

            output = new PrintWriter(client.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));

            Inputs inputHandler  = new Inputs();
            Thread thread = new Thread(inputHandler);
            thread.start();

            String inputMsg;
            while ((inputMsg = input.readLine()) != null) {
                System.out.println(inputMsg);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            off();
        }
    }

    class Inputs implements Runnable {

        @Override
        public void run() {
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                while (!quit) {
                    String msg = inputReader.readLine();
                    if (msg.equals("/quit")) {
                        inputReader.close();
                        off();
                    } else {
                        output.println(msg);
                    }
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void off() {
        quit = true;
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
    
    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
