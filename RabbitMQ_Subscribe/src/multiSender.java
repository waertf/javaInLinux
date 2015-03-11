import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class multiSender {
    private static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] args) throws java.io.IOException, InterruptedException {
        double counter=0;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.1.24");
        factory.setAutomaticRecoveryEnabled(true);
        // connection that will recover automatically
        factory.setNetworkRecoveryInterval(10000);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //byte[] sendMsg= "%%913132,A,140626185738,N2452.32648E12054.89046,0,0,NA,00000000,181,power_on-".getBytes();//new byte[10000000];
        boolean durable = true;
        Map argss = new HashMap();
        argss.put("x-ha-policy", "all");
        channel.queueDeclare("task_queue", durable, false, false, argss);

        int START = 100000;
        int END = 999999;
        Random random = new Random();
        while (true) {
            for (int i=0;i<500;i++) {
            String sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            String id=  String.valueOf(showRandomInteger(START,END,random));
            byte[] sendMsg= ("%%"+id+",A,140626185738,N"+getGaussian(2500f, 500f)+"E"+getGaussian(123f, 3f)+",0,0,NA,00000000,181,power_on-").getBytes();//new byte[10000000];
            //Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            String message = "no."+ ++counter;
            //byte[] sendMsg= new byte[10000000];//new byte[10000000];
            byte[] c = new byte[sdf.getBytes().length + sendMsg.length];
            System.arraycopy(sdf.getBytes(), 0, c, 0, sdf.getBytes().length);
            System.arraycopy(sendMsg, 0, c, sdf.getBytes().length, sendMsg.length);
            //Arrays.fill(sendMsg, (byte) 1);
            //channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
            //for (int i=0;i<500;i++) {
                byte[] cc=Integer.toString(i).getBytes();
                byte[] b = new byte[c.length + cc.length];
                System.arraycopy(c, 0, b, 0, c.length);
                System.arraycopy(cc, 0, b, c.length, cc.length);
                //channel.basicPublish(EXCHANGE_NAME, "task_queue", MessageProperties.PERSISTENT_TEXT_PLAIN, b);
                channel.basicPublish(EXCHANGE_NAME, "1", MessageProperties.PERSISTENT_TEXT_PLAIN, b);
                System.out.println(" [x] Sent '"+new String(b, "UTF-8"));
            }
            //System.out.println(sdf+" [x] Sent '" + message + "' "+sendMsg.length);

            //channel.close();
            //connection.close();
            Thread.sleep(1000);
        }
    }

    private static int showRandomInteger(int aStart, int aEnd, Random aRandom){
        if (aStart > aEnd) {
            throw new IllegalArgumentException("Start cannot exceed End.");
        }
        //get the range, casting to long to avoid overflow problems
        long range = (long)aEnd - (long)aStart + 1;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long)(range * aRandom.nextDouble());
        int randomNumber =  (int)(fraction + aStart);
        log("Generated : " + randomNumber);
        return randomNumber;
    }

    private static void log(String aMessage){
        System.out.println(aMessage);
    }

    private static Random fRandom = new Random();

    private static double getGaussian(double aMean, double aVariance){
        return aMean + fRandom.nextGaussian() * aVariance;
    }
}
