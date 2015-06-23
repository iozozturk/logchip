/**
 * Created by tr1o5087 on 17.02.2015.
 */

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A Logback appender that publishes log data to an AMQP topic
 * exchange for other applications to consume. Data is published
 * using the logging context and log level as the publishing key.
 * This allows many applications' log data to be aggregated into a
 * single AMQP exchange and allows consumers to selectively
 * subscribe to a subset of log data.
 */
public class SampleAmqpAppender extends AppenderBase<ILoggingEvent> {

    /**
     * The Layout used to format the log message body.
     */
    private Layout<ILoggingEvent> layout;

    /**
     * AMQP server connection parameters
     */
    private String host;
    private int port;
    private String virtualHost;
    private String username;
    private String password;
    private String exchangeName;
    private String routingKey;

    /**
     * Connection and Channel properties that will be
     * initialized using the above provided properties.
     */
    private Connection connection;
    private Channel channel;

    @Override
    public void start() {

        // Create a Connection and Channel to the AMQP server
        // using supplied properties.
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.host);
        factory.setUsername(this.username);
        factory.setPassword(this.password);
        factory.setVirtualHost(this.virtualHost);
        factory.setPort(this.port);
        factory.setRequestedHeartbeat(0);

//        Map<String, Object> args = new HashMap<String, Object>();
//        args.put("x-message-ttl", 10000);
        try {
            this.connection = factory.newConnection();
            this.channel = this.connection.createChannel();
            // exchangeDeclare() is idempotent. We use a "topic"
            // exchange for pub-sub style messaging.
            this.channel.exchangeDeclare(this.exchangeName, "direct", true);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        super.start();
    }

    @Override
    public void stop() {
        try {
            this.channel.close();
        } catch (IOException ignored) {
        }
        try {
            this.connection.close();
        } catch (IOException ignored) {
        }
        super.stop();
    }

    @Override
    protected void append(final ILoggingEvent event) {

        // Add useful information about the logging event
        // to AMQP message headers.
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("context", event.getLoggerContextVO().getName());
        headers.put("level", event.getLevel().levelStr);
        headers.put("timestamp", event.getTimeStamp());
        headers.put("loggerName", event.getLoggerName());
        headers.put("threadName", event.getThreadName());
        headers.put("message", event.getMessage());

        // Build a publishing key using the logging context and log level.
        // Logging context usually corresponds to application name.
        final String context = event.getLoggerContextVO().getName();
        final Level level = event.getLevel();

        ObjectMapper mapper = new ObjectMapper();

        // Use the provided layout to format the logging event
        // and set that as the AMQP message payload.
        String message = this.layout.doLayout(event);
        final byte[] payload = message.getBytes();

        BasicProperties props = new BasicProperties.Builder()
                .contentType("application/json")
                .deliveryMode(2)
                .priority(0)
                .headers(headers)
                .build();
        try {
            final byte[] valueAsBytes = mapper.writeValueAsBytes(headers);
            // Publish the log data.
            this.channel.basicPublish(this.exchangeName, this.routingKey, props, valueAsBytes);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }
    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("host: ");
        sb.append(this.host);
        sb.append(", port: ");
        sb.append(this.port);
        sb.append(", virtualHost: ");
        sb.append(this.virtualHost);
        sb.append(", username: ");
        sb.append(this.username);
        sb.append(", exchangeName: ");
        sb.append(this.exchangeName);
        sb.append(", routingKey: ");
        sb.append(this.routingKey);
        return sb.toString();
    }
}