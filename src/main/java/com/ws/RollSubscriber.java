package com.ws;


import com.rabbitmq.client.*;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.ws.impl.Module;

import java.io.IOException;



/**
 * Created by Akronys on 25/02/2015.
 */
public class RollSubscriber implements Runnable {

    private static final String EXCHANGE_NAME = "logs";

    @Override
    public void run() {

        System.out.println("RUN");

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, EXCHANGE_NAME, "");

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queueName, true, consumer);

            while (true) {
                QueueingConsumer.Delivery delivery =  consumer.nextDelivery();
                String message = new String(delivery.getBody());;

                new Module().Scores(message);

                System.out.println(" [x] Received '" + message + "'");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
