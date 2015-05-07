package com.sm.vertx.mods;

import com.sm.vertx.mods.internal.EventProperties;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

public class ByteArraySerializerIT extends TestVerticle {

	private static final String ADDRESS = "default-address";
	private static final String MESSAGE = "Test message!";

	@Override
	public void start() {

		JsonObject config = new JsonObject();
		config.putString("address", ADDRESS);
        config.putString("provider.url", "file:///tmp/servicebus.properties");

		System.setProperty("vertx.modulename", "com.sm.vertx~mod-service-bus~1.0.0");

		container.deployModule(System.getProperty("vertx.modulename"), config, asyncResult -> {
            System.out.println(asyncResult.cause());
            assertTrue(asyncResult.succeeded());
            assertNotNull("DeploymentID should not be null", asyncResult.result());
            ByteArraySerializerIT.super.start();
        });
	}


	@Test
	public void sendMessage() throws Exception {
		JsonObject jsonObject = new JsonObject();
		jsonObject.putBinary(EventProperties.PAYLOAD, MESSAGE.getBytes());

		Handler<Message<JsonObject>> replyHandler = message -> {
            assertEquals("error", message.body().getString("status"));
            assertTrue(message.body().getString("message").equals("Failed to send message to Kafka broker..."));
            testComplete();
        };
		vertx.eventBus().send(ADDRESS, jsonObject, replyHandler);
	}

}
