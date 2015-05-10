package com.sm.vertx.mods;

import com.googlecode.junittoolbox.PollingWait;
import com.googlecode.junittoolbox.RunnableAssert;
import com.sm.vertx.mods.internal.EventProperties;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * Created by sascha.moellering on 10/05/2015.
 * <p/>
 * THIS TEST ONLY WORKS WITH A SERVICE BUS QUEUE
 * AND PROPERTIES.
 */
public class ServiceBusMessageProcessorIT extends TestVerticle implements MessageListener {

    private static final String ADDRESS = "default-address";
    private PollingWait wait = new PollingWait().timeoutAfter(10, TimeUnit.SECONDS).pollEvery(1000, TimeUnit.MILLISECONDS);
    private List<String> messagesReceived = new ArrayList<>();
    private String providerUrl;
    private Connection connection;
    private Session receiveSession;
    private MessageConsumer receiver;
    private static Logger logger = LogManager.getLogger(ServiceBusMessageProcessorIT.class);


    public void before() {
        try {
            providerUrl = System.getProperty("provider.url");

            consumeMessages();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public void after() {

    }

    @Override
    public void start() {

        before();

        JsonObject config = new JsonObject();
        config.putString("address", ADDRESS);
        config.putString("provider.url", providerUrl);

        container.deployModule(System.getProperty("vertx.modulename"), config, asyncResult -> {
            assertTrue(asyncResult.succeeded());
            assertNotNull("DeploymentID should not be null", asyncResult.result());
            ServiceBusMessageProcessorIT.super.start();
        });
    }

    @Override
    public void stop() {
        after();
    }

    @Test
    public void shouldReceiveMessage() throws Exception {

        JsonObject jsonObject = new JsonObject();
        UUID requestUuid = UUID.randomUUID();

        jsonObject.putBinary(EventProperties.PAYLOAD, "foobar".getBytes());
        jsonObject.putString("uuid", requestUuid.toString());

        Handler<Message<JsonObject>> replyHandler = message -> assertEquals("ok", message.body().getString("status"));
        vertx.eventBus().send(ADDRESS, jsonObject, replyHandler);

        wait.until(new RunnableAssert("shouldReceiveMessage") {
            @Override
            public void run() throws Exception {
                assertThat(messagesReceived.contains("foobar"), is(true));
            }
        });

        testComplete();
    }

    private void consumeMessages() throws NamingException, JMSException {
        // Configure JNDI environment
        Hashtable<String, String> env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.qpid.amqp_1_0.jms.jndi.PropertiesFileInitialContextFactory");
        env.put(Context.PROVIDER_URL, providerUrl);
        Context context = new InitialContext(env);

        // Lookup ConnectionFactory and Queue
        ConnectionFactory cf = (ConnectionFactory) context.lookup("SBCF");
        Destination queue = (Destination) context.lookup("QUEUE");

        // Create Connection
        connection = cf.createConnection();

        // Create receiver-side Session, MessageConsumer,and MessageListener
        receiveSession = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        receiver = receiveSession.createConsumer(queue);
        receiver.setMessageListener(this);
        connection.start();
    }

    public void onMessage(javax.jms.Message message) {
        try {
            logger.info("Received message with JMSMessageID = " + message.getJMSMessageID());
            BytesMessage bytesMessage = (BytesMessage) message;
            byte data[] = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(data);
            logger.info("Payload: " + new String(data));

            messagesReceived.add(new String(data));

            message.acknowledge();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
