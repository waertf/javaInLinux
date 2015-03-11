/**
 * Created by wavegisAAA on 12/19/2014.
 */
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class RabbitMQ_Subscribe {

    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] args) throws java.io.IOException, InterruptedException {
        double counter=0;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.1.45");
        factory.setAutomaticRecoveryEnabled(true);
        // connection that will recover automatically
        factory.setNetworkRecoveryInterval(10000);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        byte[] sendMsg= new byte[10000000];//new byte[10000000];
        while (true) {
            String sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            //Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

            String message = "no."+ ++counter;
            //byte[] sendMsg= new byte[10000000];//new byte[10000000];
            //byte[] c = new byte[sdf.getBytes().length + sendMsg.length];
            //System.arraycopy(sdf.getBytes(), 0, c, 0, sdf.getBytes().length);
            //System.arraycopy(sendMsg, 0, c, sdf.getBytes().length, sendMsg.length);
            //Arrays.fill(sendMsg, (byte) 1);
            //channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
            channel.basicPublish(EXCHANGE_NAME, "", null, sendMsg);
            System.out.println(sdf+" [x] Sent '" + message + "' "+sendMsg.length);

            //channel.close();
            //connection.close();
            Thread.sleep(1000);
        }
    }
}
