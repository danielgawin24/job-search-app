package com.jsa.jobsearchapp.mail;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;

import java.util.List;

public class MailtrapJavaSDKTest {

    private static final String TOKEN = "1131f08f6aebb354525917b4a47526a0";

    public static void main(String[] args) {
        final MailtrapConfig config = new MailtrapConfig.Builder()
                .sandbox(true)
                .inboxId(4534823L)
                .token(TOKEN)
                .build();

        final MailtrapClient client = MailtrapClientFactory.createMailtrapClient(config);

        final MailtrapMail mail = MailtrapMail.builder()
                .from(new Address("hello@demomailtrap.co", "Mailtrap Test"))
                .to(List.of(new Address("noreply.jobsearch@op.pl")))
                .subject("You are awesome!")
                .text("Congrats for sending test email with Mailtrap!")
                .category("Integration Test")
                .build();

        try {
            System.out.println(client.send(mail));
        } catch (Exception e) {
            System.out.println("Caught exception : " + e);
        }
    }
}