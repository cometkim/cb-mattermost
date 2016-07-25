package kr.cometkim.codebeamer.client;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by comet on 2016-07-20.
 */
public class MattermostClientTest {

    @Test
    public void testPost() throws Exception {
        MattermostClient client = new MattermostClient();
        // Test your incomming-url
        client.setEndpoint("http://your-mattermost-server/hooks/xxxxxxxxxxxxxxxxxxxxxxxxxx");
        Assert.assertTrue("Failed to post to endpoint", client.post("test"));
    }
}