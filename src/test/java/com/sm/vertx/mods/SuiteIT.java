package com.sm.vertx.mods;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ServiceBusMessageProcessorTest.class,
        ByteArraySerializerIT.class,
        ServiceBusMessageProcessorIT.class
})
public class SuiteIT {
}
