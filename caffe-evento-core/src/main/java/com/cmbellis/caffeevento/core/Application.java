package com.cmbellis.caffeevento.core;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.AbstractMap;
import java.util.Map;

/**
 * Central management for the event queue and how everything gets plugged into one another.
 * Created by chris on 8/30/16.
 */
public class Application {
    public static int port = 1234;

    private static Log log = LogFactory.getLog("Root Application");
    private static ExceptionLogger logger = new ExceptionLogger(log);

    public static void main(String[] args) throws Exception {
        startTcpBroker(port);
        createQueueConsumer(port, "main.consumer").setMessageListener(message -> {
            try {
                if(message instanceof TextMessage) {
                    TextMessage tMessage = (TextMessage)message;
                    System.out.println("Got message: " + tMessage.getText());
                }
            } catch (JMSException e) {
                log.error("Error while receiving message",  e);
            }
        });

        Map.Entry<Session, MessageProducer> producer = createQueueProducer(port, "main.consumer");
        for(int i = 0; i<10; i++) {
            producer.getValue().send(producer.getKey().createTextMessage("Hello From Me!"));
            Thread.sleep(1000);
        }

    }

    public static void startTcpBroker(int port) throws Exception {
        BrokerService broker = new BrokerService();
        // todo(cmb): add configurable port
        broker.addConnector("tcp://localhost:" + port);
        broker.start();
    }

    public static MessageConsumer createQueueConsumer(int port, String name) throws Exception {
        Session session = createSession(port);
        Destination destination = session.createQueue(name);
        return session.createConsumer(destination);
    }

    public static Map.Entry<Session, MessageProducer> createQueueProducer(int port, String name) throws Exception {
        Session session = createSession(port);
        Destination destination = session.createQueue(name);
        return new AbstractMap.SimpleImmutableEntry<>(session, session.createProducer(destination));
    }

    private static Session createSession(int port) throws Exception {
        Connection connection = new ActiveMQConnectionFactory("tcp://localhost:"+port).createConnection();
        connection.start();
        connection.setExceptionListener(logger);
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }
}
