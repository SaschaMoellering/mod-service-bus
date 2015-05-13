package io.autoscaling.mods;

import io.autoscaling.mods.ServiceBusMessageProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import javax.jms.BytesMessage;
import javax.jms.Session;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by sascha.moellering on 09/05/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceBusMessageProcessorTest {
    @Mock
    private Logger logger;

    @Mock
    private Message<JsonObject> event;

    @Mock
    private Session sendSession;

    @Mock
    private BytesMessage msg;

    @InjectMocks
    private ServiceBusMessageProcessor serviceBusMessageProcessor;

    @Test
    public void sendMessageToServiceBus() throws Exception {
        ServiceBusMessageProcessor serviceBusMessageProcessorSpy = spy(serviceBusMessageProcessor);

        JsonObject jsonObjectMock = mock(JsonObject.class);
        when(event.body()).thenReturn(jsonObjectMock);
        when(jsonObjectMock.getString(anyString())).thenReturn("test");


        serviceBusMessageProcessorSpy.handle(event);
        verify(serviceBusMessageProcessorSpy).sendMessageToServiceBus(event);
    }
}
