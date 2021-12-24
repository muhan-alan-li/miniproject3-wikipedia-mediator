package cpen221.mp3.server;

public class GSONOutput {
    
    private String id;
    private String status;
    private String response;

    /**
     *
     * @param id the identification of GSON output, not null
     * @param status the status of the operation (success/fail), not null
     * @param response the output of the operation, not null
     */
    public GSONOutput(String id, String status, String response) {

        this.id = id;
        this.status = status;
        this.response = response;
    }

    /**
     *
     * @param id the identification of GSON output, not null
     * @param response the output of the operation, not null
     */
    public GSONOutput(String id, String response) {

        this.id = id;
        this.response = response;
    }

}
