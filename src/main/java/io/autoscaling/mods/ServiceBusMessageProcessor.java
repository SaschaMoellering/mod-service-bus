package io.autoscaling.mods;

import io.autoscaling.mods.exception.EventBusException;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.UUID;

import static io.autoscaling.mods.internal.Constants.CONTEXT_FACTORY;
import static io.autoscaling.mods.internal.Constants.PROVIDER_URL;
import static io.autoscaling.mods.internal.EventProperties.PAYLOAD;

/**
 * Created by sascha.moellering on 05/05/2015.
 */
public class ServiceBusMessageProcessor extends BusModBase implements Handler<Message<JsonObject>> {

    private Connection connection;
    private MessageProducer sender;
    private Session sendSession;
    private Destination queue;
    private BytesMessage msg;
    private byte[] payload;

    @Override
    public void start() {
        super.start();

        try {
            createProducer();
        } catch (NamingException | JMSException e) {
            logger.error(e.getMessage(), e);
        }

        final String address = getMandatoryStringConfig("address");
        logger.info("Registered " + ServiceBusMessageProcessor.class.getName() + " at " + address);
        vertx.eventBus().registerHandler(address, this);
    }

    @Override
    public void stop() {
        super.stop();

        try {
            if (connection != null)
                connection.close();
        } catch (JMSException e) {
            logger.error(e.toString(), e);
        }
    }

    @Override
    public void handle(Message<JsonObject> jsonObjectMessage) {
        try {
            sendMessageToServiceBus(jsonObjectMessage);
        } catch (EventBusException exc) {
            logger.error(exc);
        }
    }

    private void createProducer() throws NamingException, JMSException {

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, getMandatoryStringConfig(PROVIDER_URL));
        InitialContext context = new InitialContext(env);

        // Lookup ConnectionFactory and Queue
        ConnectionFactory cf = (ConnectionFactory) context.lookup("SBCF");
        queue = (Destination) context.lookup("QUEUE");

        logger.info("Trying to connect to " + queue.toString());
        connection = cf.createConnection();

        connection.setExceptionListener(exception -> {
            logger.error("ExceptionListener triggered: " + exception.getMessage(), exception);
            try {
                Thread.sleep(5000); // Wait 5 seconds (JMS server restarted?)
                createProducer();
            } catch (InterruptedException | NamingException | JMSException e) {
                logger.error("Error pausing thread" + e.getMessage());
            }
        });

        sendSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        sender = sendSession.createProducer(queue);
    }

    protected void sendMessageToServiceBus(final Message<JsonObject> event) throws EventBusException {
        try {
            if (!isValid(event.body().getString(PAYLOAD))) {
                logger.error("Invalid message provided.");
                return;
            }

            JsonObject object = event.body();
            logger.debug(" --- Got event " + event.toString());
            logger.debug(" --- Got body + " + object.toString());

            payload = object.getBinary(PAYLOAD);

            if (payload == null) {
                logger.debug(" --- Payload is null, trying to get the payload as String");
                payload = object.getString(PAYLOAD).getBytes();
            }
            logger.debug("Binary payload size: " + payload.length);

            String messageId = UUID.randomUUID().toString();
            msg = sendSession.createBytesMessage();

            if (msg != null) {
                msg.writeBytes(payload);
                msg.setJMSMessageID("ID:" + messageId);
                sender.send(msg);
                logger.debug("Sent message with ID " + messageId);
            }

            sendOK(event);
        } catch (JMSException exc) {
            logger.error(exc);
            sendError(event, exc.toString(), exc);
        }
    }

    private boolean isValid(String str) {
        return str != null && !str.isEmpty();
    }
}
