import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class ReceiveLogs {

    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] argv)
            throws java.io.IOException,
            java.lang.InterruptedException {
        double counter=0;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.1.45");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        //boolean durable = true;
        //String queueName = channel.queueDeclare("task_queue", durable, false, false, null).getQueue();
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        //QueueingConsumer consumer = new QueueingConsumer(channel);
        //channel.basicConsume(queueName, true, consumer);
        channel.basicQos(1);
        QueueingConsumer consumer = new QueueingConsumer(channel);
        boolean autoAck = true;
        channel.basicConsume(queueName, autoAck, consumer);
        while (!Thread.currentThread().isInterrupted()) {
            //channel.basicQos(1);
            //QueueingConsumer consumer = new QueueingConsumer(channel);
            //channel.basicConsume(queueName, true, consumer);
            try {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                //channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                //String message = new String(Arrays.copyOfRange(delivery.getBody(),0,22));
                String message = new String(delivery.getBody());
                //channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                System.out.println(LocalDateTime.now().toString()+" [x] Received '" + ++counter + "' "+message);
            }
            catch (Exception ex)
            {
                Thread.currentThread().interrupt();
                break;
            }
            //QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            //String message = new String(Arrays.copyOfRange(delivery.getBody(),0,22));


            //System.out.println(LocalDateTime.now().toString()+" [x] Received '" + ++counter + "' "+message);
        }
    }
}
