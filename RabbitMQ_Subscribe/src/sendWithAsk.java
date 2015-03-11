import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wavegisAAA on 1/8/2015.
 */
public class sendWithAsk {
    private static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] args) throws java.io.IOException, InterruptedException {
        double counter=0;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.1.45");
        factory.setAutomaticRecoveryEnabled(true);
        // connection that will recover automatically
        factory.setNetworkRecoveryInterval(10000);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        byte[] sendMsg= "%%913132,A,140626185738,N2452.32648E12054.89046,0,0,NA,00000000,181,power_on-".getBytes();//new byte[10000000];
        while (true) {
            String sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            //Channel channel = connection.createChannel();

            //boolean durable = true;
            //channel.queueDeclare("task_queue", durable, false, false, null);
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            String message = "no."+ ++counter;
            //byte[] sendMsg= new byte[10000000];//new byte[10000000];
            byte[] c = new byte[sdf.getBytes().length + sendMsg.length];
            System.arraycopy(sdf.getBytes(), 0, c, 0, sdf.getBytes().length);
            System.arraycopy(sendMsg, 0, c, sdf.getBytes().length, sendMsg.length);
            //Arrays.fill(sendMsg, (byte) 1);
            //channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
            for (int i=0;i<500;i++) {
                byte[] cc=Integer.toString(i).getBytes();
                byte[] b = new byte[c.length + cc.length];
                System.arraycopy(c, 0, b, 0, c.length);
                System.arraycopy(cc, 0, b, c.length, cc.length);
                String severity = "alonso";
                channel.basicPublish(EXCHANGE_NAME, severity, MessageProperties.PERSISTENT_TEXT_PLAIN, b);
                //channel.basicPublish(EXCHANGE_NAME, "", null, b);
            }
            System.out.println(sdf+" [x] Sent '" + message + "' "+sendMsg.length);

            //channel.close();
            //connection.close();
            Thread.sleep(1000);
        }
    }
}
