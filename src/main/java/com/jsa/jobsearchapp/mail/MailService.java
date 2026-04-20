package com.jsa.jobsearchapp.mail;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class MailService {

    public MailService() {
    }

    public HttpResponse<String> sendSimpleMailAPI(String subject, String contents) {
        HttpClient client = HttpClient.newHttpClient();

        String json = """
                {
                  "from": {
                    "email": "hello@demomailtrap.co",
                    "name": "JobSearchApp"
                  },
                  "to": [
                    {
                      "email": "noreply.jobsearch@op.pl"
                    }
                  ],
                  "subject": "%s",
                  "text": "%s",
                  "category": "Sent from Job Search App"
                }
                """.formatted(subject, contents);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://send.api.mailtrap.io/api/send"))
                .header("Authorization", "Bearer 1131f08f6aebb354525917b4a47526a0")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
