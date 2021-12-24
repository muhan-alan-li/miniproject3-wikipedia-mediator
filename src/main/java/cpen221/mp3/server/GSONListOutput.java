package cpen221.mp3.server;

import java.util.List;

public class GSONListOutput {

    private String id;
    private String status;
    private List<String> response;

    /**
     *
     * @param id the identification of GSON output, not null
     * @param status the status of the operation (success/fail), not null
     * @param response a list output of the operation, not null
     */
    public GSONListOutput(String id, String status, List<String> response) {

        this.id = id;
        this.status = status;
        this.response = response;
    }

}
