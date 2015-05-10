Vert.x Service Bus Module
===========

Service Bus module allows to receive events published by other Vert.x verticles and send those events to Azure Service Bus.


Dependencies
==========

This module requires a Service Bus queue (does only work with non-partitioned queues http://azure.microsoft.com/en-us/documentation/articles/service-bus-java-how-to-use-jms-api-amqp/).
After you have this module integrated into your application and a message has been sent to Service Bus,
you may test the results by creating a Service Bus consumer (take a look at ServiceBusMessageProcessorIT for inspiration).


Name
==========

The module name is mod-service-bus.


Configuration
===========

When deploying this module, you need to provide the following configuration:
```javascript
{
    "address": <address on the Vertx event bus>,
    "provider.url": <name of the environment property for specifying configuration information for the service provider to use>
}
```

For example:
```javascript
{
    "address": "service.bus.verticle",
    "provider.url": "file:///tmp/servicebus.properties"
}
```

The detailed description of each parameter:

* `address` (mandatory) - The address of Vert.x's EventBus, where the event has been sent by your application in order to be consumed by this module later on.
* `streamName` (mandatory) - The name of the environment property for specifying configuration information for the service provider to use

Installation
=======

```
vertx install com.sm.vertx~mod-service-bus~1.0.0
```

If you get a "not found" exception, you might need to edit the repos.txt of your Vert.x installation to use https.


Usage
=======

You can test this module locally, just deploy it in your application specifying necessary configuration.
Make sure you have a Service Bus queue running in your preferred region.

Then deploy mod-service-bus module in your application like specified below:
Example:

```java
        JsonObject config = new JsonObject();
        config.putString("address", "service.bus.verticle");
        config.putString("provider.url", "file:///tmp/servicebus.properties");

        container.deployModule("com.sm.vertx~mod-service-bus~1.0.0", config);

```

You can send messages from your application in Vert.x's JsonObject format, where the key must be `"payload"` string, and the value has to be byte array. See below for more details:

For Byte Array type
```java
JsonObject jsonObject = new JsonObject();
jsonObject.putString("payload", "your message goes here".getBytes());
```

Then you can verify that you receive those messages in Service Bus by creating a consumer.

Now you will see the messages being consumed.


License
=========
Copyright 2015 under Apache License. See `LICENSE`

Author: Sascha Möllering

Contributing
============
1. Fork the repository on Github
2. Create a named feature branch
3. Develop your changes in a branch
4. Write tests for your change (if applicable)
5. Ensure all the tests are passing
6. Submit a Pull Request using Github
