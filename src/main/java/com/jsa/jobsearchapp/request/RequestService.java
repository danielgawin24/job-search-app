package com.jsa.jobsearchapp.request;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Getter
@Service
public class RequestService {

    public Document requestConnection(String url) {
        try {
            Connection connection = Jsoup.connect(url);
            return connection.get();
        } catch (Exception e) {
            System.err.println("Unable to fetch HTML of: " + url);
        }
        return null;
    }

    public HttpResponse<String> fetchResponseBody(String link) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(link))
                .GET()
                .build();
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to fetch response body in RequestService for: " + link);
        }
        return null;
    }
}
