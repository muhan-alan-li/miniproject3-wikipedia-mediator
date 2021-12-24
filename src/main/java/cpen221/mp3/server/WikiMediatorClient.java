package cpen221.mp3.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class WikiMediatorClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Make a WikiMediatorClient and connect it to a server running on
     * hostname at the specified port.
     *
     * @throws IOException if can't connect
     */
    public WikiMediatorClient(String hostname, int port) throws IOException {

        socket = new Socket(hostname, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }
    /**
     * Send a request to the server. Requires this is "open".
     *
     * @param json a string representing a json
     * @throws IOException
     */
    public void sendRequest(String json) throws IOException {

        out.println(json);
        out.flush(); // important! make sure x actually gets sent
    }

    /**
     * Get a reply from the next request that was submitted.
     * Requires this is "open".
     *
     * @return A JSON String with details of id, status, and output
     * @throws IOException if network or server failure
     */
    public String getReply() throws IOException {

        String reply = in.readLine();
        System.out.println("getting reply");
        if (reply == null) {

            throw new IOException("connection terminated unexpectedly");
        }

        try {

            return reply;
        } catch (NumberFormatException nfe) {

            throw new IOException("misformatted reply: " + reply);
        }
    }

    /**
     * Closes the client's connection to the server.
     * This client is now "closed". Requires this is "open".
     *
     * @throws IOException if close fails
     */
    public void close() throws IOException {

        in.close();
        out.close();
        socket.close();
    }

}
