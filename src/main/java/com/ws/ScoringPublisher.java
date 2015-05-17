package com.ws;

import com.rabbitmq.client.*;

/**
 * Created by Akronys on 25/02/2015.
 */
public class ScoringPublisher {

    /** TODO : Récuperer l'id de la piste à partir de la game et publier en fonction ! **/
    private static final String EXCHANGE_NAME = "scoring";

    public  void publishMessage(String message, String routing_key) throws java.io.IOException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        channel.basicPublish(EXCHANGE_NAME, routing_key, null,message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");

        channel.close();
        connection.close();
    }
}
