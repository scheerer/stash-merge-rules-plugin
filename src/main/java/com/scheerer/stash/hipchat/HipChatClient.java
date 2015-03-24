package com.scheerer.stash.hipchat;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;

public class HipChatClient {
    private static final Logger log = LoggerFactory.getLogger(HipChatClient.class);

    private String apiToken;

    public HipChatClient(String apiToken) {
        this.apiToken = apiToken;
    }

    public void sendRoomNotification(HipChatNotification hipChatNotification) {
        log.warn("Sending HipChat notification: " + hipChatNotification.getMessage());

        String url = String.format("https://api.hipchat.com/v1/rooms/message?notify=1&auth_token=%s&room_id=%s&from=%s&message=%s&color=%s",
                apiToken,
                URLEncoder.encode(hipChatNotification.getRoomId()),
                URLEncoder.encode(hipChatNotification.getFrom()),
                URLEncoder.encode(hipChatNotification.getMessage()),
                hipChatNotification.getColor().toString().toLowerCase());
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        try {
            client.execute(request);
        } catch (Exception e) {
            log.error("Could not send notification to HipChat.", e);
        }
    }
}
