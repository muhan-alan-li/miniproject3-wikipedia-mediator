package cpen221.mp3.WikiMediatorServerTests;

import cpen221.mp3.server.WikiMediatorClient;
import cpen221.mp3.server.WikiMediatorServer;
import cpen221.mp3.wikimediator.WikiMediator;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

public class WikiMediatorServerTests {

    private static final String searchObama = "{ \"id\": \"1\", \"type\": \"search\", \"query\": \"Barack Obama\", \"limit\": \"5\" }";
    private static final String searchSathish = "{ \"id\": \"gopalakrishnan\", \"type\": \"search\", \"query\": \"Sathish\", \"limit\": \"10\" }";
    private static final String zeitgeistLimit5 = "{ \"id\": \"two\", \"type\": \"zeitgeist\", \"limit\": \"5\" }";

    private static final String getPage = "{ \"id\": \"3\", \"type\": \"getPage\", \"pageTitle\": \"Barack Obama\"}";
    private static final String trending = "{ \"id\": \"trending\", \"type\": \"trending\", \"timeLimitInSeconds\": \"30\", \"maxItems\": \"2\" }";
    private static final String windowedPeakLoad = "{ \"id\": \"trending\", \"type\": \"windowedPeakLoad\", \"timeWindowInSeconds\": \"25\"}";
    private static final String windowedPeakLoadDefault = "{ \"id\": \"trending\", \"type\": \"windowedPeakLoad\"}";

    @Before
    public void setup() {

        try {

            WikiMediatorServer server = new WikiMediatorServer(WikiMediatorServer.WIKI_PORT, 1, new WikiMediator());
            new Thread(() -> {
                try {

                    server.serve();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            Thread.sleep(2000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
       These tests assume you already have a server instance up, because otherwise you cant see results in terminal lol
     */
    @Test
    public void sequentialEverything() throws InterruptedException, IOException {

        try {

            WikiMediatorClient client1 = new WikiMediatorClient("127.0.0.1", WikiMediatorServer.WIKI_PORT);
            WikiMediatorClient client2 = new WikiMediatorClient("127.0.0.1", WikiMediatorServer.WIKI_PORT);
            try {

                // jason for search
                String jason = "{ \"id\": \"1\", \"type\": \"search\", \"query\": \"Barack Obama\", \"limit\": \"5\", \"timeout\": \"1\" }\n" +
                        "{ \"id\": \"1\", \"type\": \"search\", \"query\": \"Barack Obama\", \"limit\": \"5\", \"timeout\": \"1\" }\n" +
                        "{ \"id\": \"3\", \"type\": \"search\", \"query\": \"mom\", \"limit\": \"5\", \"timeout\": \"1\" }\n" +
                        "{ \"id\": \"4\", \"type\": \"search\", \"query\": \"dad\", \"limit\": \"5\", \"timeout\": \"1\" }\n" +
                        "{ \"id\": \"5\", \"type\": \"search\", \"query\": \"dog\", \"limit\": \"5\", \"timeout\": \"1\" }\n" +
                        "{ \"id\": \"6\", \"type\": \"search\", \"query\": \"cat\", \"limit\": \"5\", \"timeout\": \"1\" }\n" +
                        "{ \"id\": \"gopalakrishnan\", \"type\": \"search\", \"query\": \"Sathish\", \"limit\": \"10\" }\n" +
                        "{ \"id\": \"7\", \"type\": \"getPage\", \"pageTitle\": \"Barack Obama\"}\n" +
//                                "{ \"id\": \"8\", \"type\": \"getPage\", \"pageTitle\": \"Bibliography of Barack Obama\"}" +
                        "{ \"id\": \"10\", \"type\": \"zeitgeist\", \"limit\": \"5\" }\n" +
                        "{ \"id\": \"trending\", \"type\": \"trending\", \"timeLimitInSeconds\": \"30\", \"maxItems\": \"5\" }\n" +
                        "{ \"id\": \"windowedPeakLoad1\", \"type\": \"windowedPeakLoad\", \"timeWindowInSeconds\": \"25\"}\n" +
                        "{ \"id\": \"windowedPeakLoad2\", \"type\": \"windowedPeakLoad\"}\n" +
                        "{ \"id\": \"stop\", \"type\": \"stop\" } \n";
                String jason2 = "{ \"id\": \"one\", \"type\": \"search\", \"query\": \"Obama Barack\", \"limit\": \"5\", \"timeout\": \"1\" }\n" +
                        "{ \"id\": \"two\", \"type\": \"search\", \"query\": \"september 11\", \"limit\": \"5\", \"timeout\": \"1\" }\n" +
                        "{ \"id\": \"three\", \"type\": \"search\", \"query\": \"sister\", \"limit\": \"5\", \"timeout\": \"1\" }\n" +
                        "{ \"id\": \"four\", \"type\": \"search\", \"query\": \"brother\", \"limit\": \"5\", \"timeout\": \"1\" }\n" +
                        "{ \"id\": \"five\", \"type\": \"search\", \"query\": \"puppy\", \"limit\": \"5\", \"timeout\": \"1\" }\n" +
                        "{ \"id\": \"six\", \"type\": \"search\", \"query\": \"kitten\", \"limit\": \"5\", \"timeout\": \"1\" }\n" +
                        "{ \"id\": \"Sathish\", \"type\": \"search\", \"query\": \"gopalakrishnan\", \"limit\": \"10\" }\n";

                System.out.println(jason);
                client1.sendRequest(jason);

                Thread.sleep(1000);

                System.err.println(jason2);
                client2.sendRequest(jason2);

                for (int i = 1; i <= 7; i++) {
                    System.out.println(client2.getReply());
                }
                for (int i = 1; i <= 20; i++) {
                    System.out.println(client1.getReply());
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                client1.close();
                client2.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    @Test
    public void testLoadData() {

        try {

            WikiMediatorClient client = new WikiMediatorClient("127.0.0.1", WikiMediatorServer.WIKI_PORT);
            client.sendRequest("{ \"id\": \"two\", \"type\": \"zeitgeist\", \"limit\": \"5\" }");
            System.out.println(client.getReply());
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    @Test
    public void shortestPath() {

        try {

            WikiMediatorClient client = new WikiMediatorClient("127.0.0.1", WikiMediatorServer.WIKI_PORT);
            client.sendRequest("{ \"id\": \"3\", \"type\": \"shortestPath\", \"pageTitle1\": \"Philosophy\", \"pageTitle2\": \"Barack Obama\", \"timeout\": \"100\" }");
            System.out.println(client.getReply());
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @Test
    public void timeout() {

        try {

            WikiMediatorClient client = new WikiMediatorClient("127.0.0.1", WikiMediatorServer.WIKI_PORT);
            client.sendRequest("{ \"id\": \"3\", \"type\": \"shortestPath\", \"pageTitle1\": \"Philosophy\", \"pageTitle2\": \"Barack Obama\", \"timeout\": \"5\" }");
            System.out.println(client.getReply());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // delete all first before running this test
    @Test
    public void WPLDNE() {

        try {

            WikiMediatorClient client = new WikiMediatorClient("127.0.0.1", WikiMediatorServer.WIKI_PORT);

            client.sendRequest("{ \"id\": \"windowedPeakLoad2\", \"type\": \"windowedPeakLoad\"}");
            System.out.println(client.getReply());

            client.sendRequest("{ \"id\": \"windowedPeakLoad1\", \"type\": \"windowedPeakLoad\", \"timeWindowInSeconds\": \"25\"}");
            System.out.println(client.getReply());

            client.sendRequest("{ \"id\": \"trending\", \"type\": \"trending\", \"timeLimitInSeconds\": \"30\", \"maxItems\": \"5\" }");
            System.out.println(client.getReply());

            client.sendRequest("{ \"id\": \"two\", \"type\": \"zeitgeist\", \"limit\": \"5\" }");
            System.out.println(client.getReply());

            client.sendRequest("{ \"id\": \"1\", \"type\": \"search\", \"query\": \"fjeawiovnureaj\", \"limit\": \"5\", \"timeout\": \"1\" }");
            System.out.println(client.getReply());

            client.sendRequest("{ \"id\": \"9\", \"type\": \"getPage\", \"pageTitle\": \"Barack Obama: Der schwarze\"}");
            System.out.println(client.getReply());

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

}
