package cpen221.mp3.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import cpen221.mp3.wikimediator.WikiMediator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class WikiMediatorServer {

    /**
     * Default port number where the server listens for connections.
     */
    public static final int WIKI_PORT = 9001;

    private ServerSocket serverSocket;
    private final int maxRequests;
    private final WikiMediator wikiMediator;

    /*-------- Representation invariant --------*/
    // serverSocket: Represents a valid socket that will be used by the server. Can't be null
    // maxRequests:  Represents how many clients can be handled simultaneously. Must be positive, can't be null.
    // wikiMediator: Represents an instance of a wikiMediator object that enables the server to utilize
    /*---------- Abstraction function ----------*/
    // The wikiMediator server is an instance of wikiMediator that can handle
    // multiple clients at once to handle requests using the jWiki API.

    /**
     * Start a server at a given port number, with the ability to process
     * up to n requests concurrently.
     *
     * @param port         the port number to bind the server to, 9000 <= {@code port} <= 9999
     * @param n            the number of concurrent requests the server can handle, 0 < {@code n} <= 32
     * @param wikiMediator the WikiMediator instance to use for the server, {@code wikiMediator} is not {@code null}
     */
    public WikiMediatorServer(int port, int n, WikiMediator wikiMediator) throws IOException {

        serverSocket = new ServerSocket(port);
        maxRequests = n;
        this.wikiMediator = wikiMediator;
    }

    /**
     * Starts the server and handles the client(s).
     *
     * @throws IOException if server can't run
     */
    public void serve() throws IOException {

        while (true) {
            // block until a client connects
            // new thread
            new Thread(() -> {

                try {

                    try (Socket socket = serverSocket.accept()) {
                        handle(socket);
                    }
                } catch (IOException ioe) {

                    throw new RuntimeException();
                }
            }).start();
        }
    }

    private void handle(Socket socket) throws IOException {

        System.err.println("client connected");
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

            for (String jsonRequest = in.readLine(); jsonRequest != null; jsonRequest = in.readLine()) {

                System.err.println(jsonRequest);
                JsonObject parsedJSON = JsonParser.parseString(jsonRequest).getAsJsonObject();
                Gson gson = new Gson();
                String timeout = gson.fromJson(parsedJSON.get("timeout"), String.class);
                String id = gson.fromJson(parsedJSON.get("id"), String.class);
                ExecutorService executorService = Executors.newFixedThreadPool(2);

                Callable<String> timer = () -> {

                    if (timeout != null) {

                        Thread.sleep(Integer.parseInt(timeout) * 1000L);
                        GSONOutput output = new GSONOutput(id, "failed", "Operation timed out");
                        String jason = gson.toJson(output);
                        out.println(jason);
                        out.close();
                        in.close();
                    } else {
                        Thread.sleep(Integer.MAX_VALUE); // limit time ot 2 mil seconds
                    }

                    return "timer finished first";
                };

                Callable<String> run = () -> {

                    String json = ""; // output
                    String type = gson.fromJson(parsedJSON.get("type"), String.class);
                    GSONOutput output;
                    GSONListOutput listOutput;
                    int limit;

                    switch (type) {

                        case "search":

                            String query = gson.fromJson(parsedJSON.get("query"), String.class);
                            limit = Integer.parseInt(gson.fromJson(parsedJSON.get("limit"), String.class));
                            List<String> searchResults = wikiMediator.search(query, limit);

                            if (!searchResults.isEmpty()) {
                                listOutput = new GSONListOutput(id, "success", searchResults);
                                json = gson.toJson(listOutput);
                            } else {
                                output = new GSONOutput(id, "failed", "No results for " + query);
                                json = gson.toJson(output);
                            }
                            break;

                        case "getPage":

                            String pageTitle = gson.fromJson(parsedJSON.get("pageTitle"), String.class);
                            String pageText = wikiMediator.getPage(pageTitle);
                            if (pageText.isEmpty() || pageTitle.isEmpty()) {
                                output = new GSONOutput(id, "failed", "No results for " + pageTitle);
                            } else {
                                output = new GSONOutput(id, "success", pageText);
                            }
                            json = gson.toJson(output);
                            break;

                        case "zeitgeist":

                            limit = Integer.parseInt(gson.fromJson(parsedJSON.get("limit"), String.class));
                            List<String> commonStrings = wikiMediator.zeitgeist(limit);

                            if (commonStrings.isEmpty()) {
                                output = new GSONOutput(id, "failed", "No common strings exist yet");
                                json = gson.toJson(output);
                            } else {
                                listOutput = new GSONListOutput(id, "success", commonStrings);
                                json = gson.toJson(listOutput);
                            }
                            break;

                        case "trending":

                            int timeLimitInSeconds = Integer.parseInt(gson.fromJson(parsedJSON.get("timeLimitInSeconds"), String.class));
                            int maxItems = Integer.parseInt(gson.fromJson(parsedJSON.get("maxItems"), String.class));
                            List<String> trendingRequests = wikiMediator.trending(timeLimitInSeconds, maxItems);

                            if (trendingRequests.isEmpty()) {
                                output = new GSONOutput(id, "failed", "No trending items");
                                json = gson.toJson(output);
                            } else {
                                listOutput = new GSONListOutput(id, "success", trendingRequests);
                                json = gson.toJson(listOutput);
                            }
                            break;

                        case "windowedPeakLoad":

                            String timeWindowInSeconds = gson.fromJson(parsedJSON.get("timeWindowInSeconds"), String.class);
                            int numRequests;
                            System.err.println("this the time window in seconds" + timeWindowInSeconds);

                            if (timeWindowInSeconds != null) {

                                numRequests = wikiMediator.windowedPeakLoad(Integer.parseInt(timeWindowInSeconds));
                                if (numRequests == 0) {
                                    output = new GSONOutput(id, "failed", "No requests found in time window: " + timeWindowInSeconds);
                                } else {
                                    output = new GSONOutput(id, "success", String.valueOf(numRequests));
                                }

                            } else {

                                numRequests = wikiMediator.windowedPeakLoad(); // call overloaded windowedPeakLoad
                                System.err.println("got here");
                                if (numRequests == 0) {
                                    output = new GSONOutput(id, "failed", "No requests found in default time window of 30s");
                                } else {
                                    output = new GSONOutput(id, "success", String.valueOf(numRequests));
                                }
                            }
                            json = gson.toJson(output);
                            break;

                        case "shortestPath":

                            String pageTitle1 = gson.fromJson(parsedJSON.get("pageTitle1"), String.class);
                            String pageTitle2 = gson.fromJson(parsedJSON.get("pageTitle2"), String.class);
                            try {

                                List<String> response = wikiMediator.shortestPath(pageTitle1, pageTitle2, Integer.parseInt(timeout));
                                listOutput = new GSONListOutput(id, "success", response);
                                json = gson.toJson(listOutput);
                            } catch (TimeoutException t) {

                                new GSONOutput(id, "failed", "Operation timed out");
                            }
                            break;

                        case "stop":

                            output = new GSONOutput(id, "bye");
                            json = gson.toJson(output);
                            out.println(json);
                            out.close();
                            in.close();
                            serverSocket.close();
                            break;
                    }

                    out.println(json);
                    return "run finished first";
                };

                // return result of fastest
                String result = executorService.invokeAny(Arrays.asList(timer, run));
                System.err.println(result);
                executorService.shutdown();
            }

        } catch (IOException | JsonSyntaxException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.err.println("done");
        }
    }
}
