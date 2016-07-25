package kr.cometkim.codebeamer.client;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by comet on 2016-07-20.
 */
public class MattermostClient {
    private static final Logger logger = Logger.getLogger(MattermostClient.class);

    @Getter @Setter
    private String endpoint;

    @Getter @Setter
    private String userName;

    @Getter @Setter
    private String userIcon;

    @Getter @Setter
    private String channel;

    public boolean post(@NotNull String message){
        JSONObject payload = buildPayload(message);

        HttpPost jsonPost = new HttpPost(endpoint);
        jsonPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        jsonPost.setEntity(new StringEntity(payload.toString(), StandardCharsets.UTF_8));

        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response;

        Integer status;

        try {
            response = client.execute(jsonPost);
            response.close();

            status = response.getStatusLine().getStatusCode();

        } catch (IOException e) {
            logger.error("Failed to execute http post");
            e.printStackTrace();

            return false;
        }

        return status.equals(HttpStatus.SC_OK);
    }

    private JSONObject buildPayload(@NotNull String message){
        JSONObject payload = new JSONObject();

        try {
            payload.put("text", message);

            if(userName != null) payload.put("username", userName);
            if(userIcon != null) payload.put("icon_url", userIcon);
            if(channel  != null) payload.put("channel",  channel);

        } catch (JSONException e) {
            logger.error("Failed to build payload object");
            e.printStackTrace();
        }

        return payload;
    }
}
