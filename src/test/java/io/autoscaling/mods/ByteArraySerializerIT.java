/*
 * Copyright 2013 ZANOX AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.autoscaling.mods;

import io.autoscaling.mods.internal.EventProperties;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.junit.Assert.*;
import static org.vertx.testtools.VertxAssert.testComplete;


/**
 * Tests mod-service-bus module with byte array serializer configuration.
 */
public class ByteArraySerializerIT extends TestVerticle {

    private static final String ADDRESS = "default-address";
    private static final String MESSAGE = "Test bytes message!";

    @Override
    public void start() {

        JsonObject config = new JsonObject();
        config.putString("address", ADDRESS);
        config.putString("provider.url", System.getProperty("provider.url"));

        container.deployModule(System.getProperty("vertx.modulename"), config, asyncResult -> {
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
            assertEquals("ok", message.body().getString("status"));
            testComplete();
        };
        vertx.eventBus().send(ADDRESS, jsonObject, replyHandler);
    }
}
