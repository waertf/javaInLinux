/**
 * Created by wavegisAAA on 3/10/2015.
 */

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import sun.rmi.runtime.Log;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class TcpServer {
    public final static int PORT = 2999;

    public final static byte UID_LENGTH=8;
    public final static byte STATUS_LENGTH=1;
    public final static byte TOTAL_DISTANCE_LENGTH=6;
    public final static byte TIME_LENGTH=4;
    public final static byte YEAR_LENGTH=6;
    public final static byte MONTH_LENGTH=4;
    public final static byte DAY_LENGTH=5;
    public final static byte HOUR_LENGTH=5;
    public final static byte MINUTE_LENGTH=6;
    public final static byte SECOND_LENGTH=6;
    public final static byte LONG_LENGTH=3;
    public final static byte LAT_LENGTH=3;
    public final static byte VIDEO_FILENAME_LENGTH=20;

    private static final String MQ_EXCHANGE_NAME = "alonso.fanout.test_20150402092434";
    private static final String MQ_IP_ADDRESS="192.168.1.133";

    public static String ReceiveMsg;

    static Channel channel = null;
    private static final String severity = "alonso";
    static Object mutex=new Object();
    public static void main(String[] args) {

        BitSet bitset1 = BitSet.valueOf(new byte[]{1,2,3});

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(MQ_IP_ADDRESS);

        factory.setAutomaticRecoveryEnabled(true);
        // connection that will recover automatically
        factory.setNetworkRecoveryInterval(10000);
        Connection connection = null;
        try {
            connection = factory.newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Channel channel = null;
        try {
            channel = connection.createChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            channel.exchangeDeclare(MQ_EXCHANGE_NAME, "fanout");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // print the sets
        //System.out.println("Bitset1:" + bitset1);
        ExecutorService pool = Executors.newFixedThreadPool(20000);
        try  {
            ServerSocket server = new ServerSocket(PORT);
            while (true) {
                try {
                    Socket connection1 = server. accept();
                    Callable<Void> task = new CarMsgTask(connection1);
                    pool. submit(task);
                } catch (IOException ex) {}
            }
        } catch (IOException ex) {
            System. err. println("Couldn't start server" );
        }
    }

    private static class CarMsgTask implements Callable<Void> {
        private Socket connection;

        CarMsgTask(Socket connection) {
            this. connection = connection;
        }
        @Override
        public Void call() throws InterruptedException {
            try {
                /*
                DataInputStream in = new DataInputStream(connection.getInputStream());
                int length=in.readUnsignedShort();
                System.out.println(length);
                // 0 <= length <= 65535
                byte[] msg = new byte[length];
                in.readFully(msg); // if exception, it's a framing error.
                */
                InputStream in = connection.getInputStream();
                byte[] length = new byte[2];
                in.read(length);
                //System.out.print("length[0]:");
                //System.out.print(length[0] & 0xff);
                //System.out.print("  length[1]:");
                //System.out.print(length[1] & 0xff);
                int dataLength = ((length[1] & 0xff) << 8) | (length[0] & 0xff);
                int pointer=0;
                byte[] data = new byte[dataLength];
                in.read(data);

                //System.out.println("dataLength:"+String.valueOf(dataLength));
                //System.out.println("data:  ");
                //for (byte n:data)
                //{
                //    System.out.print(n);
                //    System.out.print("   ");
                //}
                //System.out.println("******************************************");
                //System.out.println();
                StringBuilder sb = new StringBuilder();
                StringBuilder uid = new StringBuilder();
                String status = null;
                String DTC = null;
                int myStatus;
                StringBuilder totalGoDistance=new StringBuilder();
                StringBuilder time=new StringBuilder();
                StringBuilder mylong = new StringBuilder();
                StringBuilder myLat = new StringBuilder();
                StringBuilder fileName = new StringBuilder();
                /*
                for(int i=0;i<data.length;i++)
                {
                    if(i<UID_LENGTH)
                        uid.append((char)data[i]);
                    if(UID_LENGTH<=i&&i<(UID_LENGTH+STATUS_LENGTH)) {
                        status = String.valueOf(data[i]);
                        myStatus=data[i];
                    }
                    if((UID_LENGTH+STATUS_LENGTH)<=i && i<(UID_LENGTH+STATUS_LENGTH+TOTAL_DISTANCE_LENGTH))
                        totalGoDistance.append((char)data[i]);
                    if((UID_LENGTH+STATUS_LENGTH+TOTAL_DISTANCE_LENGTH)<=i && i<(UID_LENGTH+STATUS_LENGTH+TOTAL_DISTANCE_LENGTH+TIME_LENGTH))
                    {

                    }

                }
                */
                while (true)
                {
                    //System.out.println("pointer=" + pointer);
                    for(int j=pointer;j<pointer+UID_LENGTH;j++)
                    {
                        uid.append((char)data[j]);
                    }
                    sb.append(uid.toString()).append(",");
                    pointer+=UID_LENGTH;

                    for(int j=pointer;j<pointer+STATUS_LENGTH;j++)
                    {
                        status = String.valueOf(data[j]);
                    }
                    sb.append(status.toString()).append(",");
                    pointer+=STATUS_LENGTH;

                    for(int j=pointer;j<pointer+TOTAL_DISTANCE_LENGTH;j++)
                    {
                        totalGoDistance.append((char)data[j]);
                    }
                    sb.append(totalGoDistance.toString()).append(",");
                    pointer+=TOTAL_DISTANCE_LENGTH;

                    byte[] timeBytes=new byte[TIME_LENGTH];
                    for(int j=pointer;j<pointer+TIME_LENGTH;j++)
                    {
                        timeBytes[j-pointer]=data[j];
                    }
                    //for(byte a:timeBytes)
                    //{
                    //    System.out.print(a);
                    //    System.out.print("  ");
                    //}
                    //boolean[] timeBools=byteArray2BitArray(timeBytes);
                    BitSet bitSet=BitSet.valueOf(timeBytes);
                    //System.out.println("Bitset1:" + bitSet);
                    //for(boolean a:timeBools)
                    //{
                    //    System.out.print(a);
                    //    System.out.print("  ");
                    //}
                    short yy=0,MM=0,dd=0,HH=0,mm=0,ss=0;
                    int timeBaseLoc=pointer;
                    for (int i=pointer;i<pointer+YEAR_LENGTH;i++)
                    {
                            if(bitSet.get(i-timeBaseLoc))
                                yy |= (1 << i-pointer);
                    }
                    if(yy<10)
                        time.append("200" + String.valueOf(yy));
                    else
                        time.append("20" + String.valueOf(yy));
                    pointer+=YEAR_LENGTH;

                    for (int i=pointer;i<pointer+MONTH_LENGTH;i++)
                    {
                        if(bitSet.get(i-timeBaseLoc))
                            MM |= (1 << i-pointer);
                    }
                    if(MM<10)
                        time.append("0"+String.valueOf(MM));
                    else
                        time.append(String.valueOf(MM));
                    pointer+=MONTH_LENGTH;

                    for (int i=pointer;i<pointer+DAY_LENGTH;i++)
                    {
                        if(bitSet.get(i-timeBaseLoc))
                            dd |= (1 << i-pointer);
                    }
                    if(dd<10)
                        time.append("0"+String.valueOf(dd));
                    else
                        time.append(String.valueOf(dd));
                    pointer+=DAY_LENGTH;

                    for (int i=pointer;i<pointer+HOUR_LENGTH;i++)
                    {
                        if(bitSet.get(i-timeBaseLoc))
                            HH |= (1 << i-pointer);
                    }
                    if(HH<10)
                        time.append("0"+String.valueOf(HH));
                    else
                        time.append(String.valueOf(HH));
                    pointer+=HOUR_LENGTH;

                    for (int i=pointer;i<pointer+MINUTE_LENGTH;i++)
                    {
                        if(bitSet.get(i-timeBaseLoc))
                            mm |= (1 << i-pointer);
                    }
                    if(mm<10)
                        time.append("0"+String.valueOf(mm));
                    else
                        time.append(String.valueOf(mm));
                    pointer+=MINUTE_LENGTH;

                    for (int i=pointer;i<pointer+SECOND_LENGTH;i++)
                    {
                        if(bitSet.get(i-timeBaseLoc))
                            ss |= (1 << i-pointer);
                    }
                    if(ss<10)
                        time.append("0"+String.valueOf(ss));
                    else
                        time.append(String.valueOf(ss));
                    sb.append(time.toString()).append(",");
                    pointer+=SECOND_LENGTH;
                    //System.out.println(time.toString());
                    /*
                    System.out.println();
                    System.out.print("time: ");
                    System.out.println(yy);
                    System.out.println(MM);
                    System.out.println(dd);
                    System.out.println(HH);
                    System.out.println(mm);
                    System.out.println(ss);
                    System.out.println();
                    */
                    pointer=timeBaseLoc+4;
                    double myDlong,myDlat;
                    myDlong=(data[pointer] & 0xff) + (data[pointer + 1] & 0xff) / 60 + (data[pointer + 2] & 0xff) / 3600;
                    mylong.append(String.valueOf(myDlong));
                    sb.append(mylong.toString()).append(",");
                    pointer+=LONG_LENGTH;

                    myDlat=(data[pointer] & 0xff) + (data[pointer + 1] & 0xff) / 60 + (data[pointer + 2] & 0xff) / 3600;
                    myLat.append(String.valueOf(myDlat));
                    if(Integer.valueOf(status)<50)
                        sb.append(myLat.toString()).append(",");
                    else
                        sb.append(myLat.toString());
                    pointer+=LAT_LENGTH;
                    switch (Integer.valueOf(status))
                    {
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                            double engineCoolantTemperature=(data[pointer]&0xff)*100/255;
                            pointer++;
                            int fuelPressure=(data[pointer]&0xff)*3;
                            pointer++;
                            int intakeManifoldPressure=data[pointer]&0xff;
                            pointer++;
                            int RPM=(data[pointer]&0xff)*256+(data[pointer+1]&0xff);
                            pointer+=2;
                            int vehicleSpeed=(data[pointer]&0xff);
                            pointer++;
                            int intakeAirTemperature=(data[pointer]&0xff)-40;
                            pointer++;
                            int airFlowRate=(data[pointer]&0xff);
                            pointer++;
                            double throttlePosition=(data[pointer]&0xff)*100/255;
                            pointer++;
                            double batteryVoltage=(data[pointer]&0xff)/10;
                            pointer++;
                            double InstantFuel=(data[pointer]&0xff);
                            pointer++;

                            sb.append(String.valueOf(engineCoolantTemperature)).append(",");
                            sb.append(String.valueOf(fuelPressure)).append(",");
                            sb.append(String.valueOf(intakeManifoldPressure)).append(",");
                            sb.append(Integer.toUnsignedString(RPM)).append(",");
                            sb.append(String.valueOf(vehicleSpeed)).append(",");
                            sb.append(String.valueOf(intakeAirTemperature)).append(",");
                            sb.append(String.valueOf(airFlowRate)).append(",");
                            sb.append(String.valueOf(throttlePosition)).append(",");;
                            sb.append(String.valueOf(batteryVoltage)).append(",");
                            sb.append(String.valueOf(InstantFuel));
                            break;
                        case 20:
                            byte[] error = new byte[10];
                            for (int i = 0; i < 10; i++)
                                error[i] = data[pointer++];
                            StringBuffer stringBuffer = new StringBuffer();
                            for (int i = 0; i < 10; i += 2) {
                                byte first, second;
                                String header;
                                first = error[i];
                                second = error[i + 1];
                                                /*if(first+second!=0)*/
                                {
                                    if (first >> 6 == 0)
                                        header = "DTC" + (i / 2 + 1) + ":P";
                                    else if (first >> 6 == 1)
                                        header = "DTC" + (i / 2 + 1) + ":C";
                                    else if (first >> 6 == -1)
                                        header = "DTC" + (i / 2 + 1) + ":U";
                                    else
                                        header = "DTC" + (i / 2 + 1) + ":B";
                                    stringBuffer.append(header +
                                            toHexChar(15 & (first & 63) >> 4) + toHexChar(first & 15) + toHexChar(15 & second >> 4) + toHexChar(second & 15) + ",");
                                    //stringBuilderHttpPost.append(DTC);
                                }
                            }
                            if (DTC != null && stringBuffer.length() > 0)
                                DTC = stringBuffer.toString();
                            else
                                DTC = "DTC1:P0000,DTC2:P0000,DTC3:P0000,DTC4:P0000,DTC5:P0000,";
                            //stringBuilderHttpPost.append("ID:"+GetBT6000sBTName+",");
                            sb.append(DTC);
                            sb.setLength(sb.length()-1);
                            break;
                        case 30:
                            int RFTT=(data[pointer++]&0xff);
                            int RFTP=(data[pointer++]&0xff);
                            int LFTT=(data[pointer++]&0xff);
                            int LFTP=(data[pointer++]&0xff);

                            int RRTT=(data[pointer++]&0xff);
                            int RRTP=(data[pointer++]&0xff);
                            int LRTT=(data[pointer++]&0xff);
                            int LRTP=(data[pointer++]&0xff);

                            sb.append(String.valueOf(RFTT)).append(",");
                            sb.append(String.valueOf(RFTP)).append(",");
                            sb.append(String.valueOf(LFTT)).append(",");
                            sb.append(String.valueOf(LFTP)).append(",");

                            sb.append(String.valueOf(RRTT)).append(",");
                            sb.append(String.valueOf(RRTP)).append(",");
                            sb.append(String.valueOf(LRTT)).append(",");
                            sb.append(String.valueOf(LRTP));
                            break;
                        case 41:
                        case 42:
                        case 43:
                            for(int j=pointer;j<pointer+VIDEO_FILENAME_LENGTH;j++)
                            {
                                fileName.append((char)data[j]);
                            }
                            sb.append(fileName.toString());
                            pointer+=VIDEO_FILENAME_LENGTH;
                            break;
                        case 51:
                        case 52:
                        case 53:
                            //do nothing
                            break;
                    }
                    //System.out.println("fileName=" + fileName.toString());
                    time.setLength(0);
                    uid.setLength(0);
                    totalGoDistance.setLength(0);
                    mylong.setLength(0);
                    myLat.setLength(0);
                    fileName.setLength(0);
                    //sb.append(System.getProperty("line.separator"));
                    sb.append(";");
                    if(pointer>=dataLength) {
                        sb.append(System.getProperty("line.separator"));

                        String MQDataSend=null;
                        ReceiveMsg=sb.toString();
                        //System.out.println(ReceiveMsg);

                        String[] mysplit=ReceiveMsg.replace(System.getProperty("line.separator"),"").split(";");
                        for(int i=mysplit.length-1;i>=0;i--)
                        {
                            String[] myrow= mysplit[i].split(",");
                            if(myrow.length>1)
                            if(myrow[1].compareTo("11")==0)
                            {
                                MQDataSend=mysplit[i];
                                System.out.println(MQDataSend);
                                break;
                            }
                        }
                        final String finalMQDataSend = MQDataSend;
                        Runnable WriteToMQ = () -> {
                            try {
                                synchronized (mutex)
                                {
                                    //channel.basicPublish(MQ_EXCHANGE_NAME, severity, MessageProperties.PERSISTENT_TEXT_PLAIN, ReceiveMsg.getBytes());
                                    if(finalMQDataSend !=null)
                                    channel.basicPublish(MQ_EXCHANGE_NAME, severity, MessageProperties.PERSISTENT_TEXT_PLAIN, finalMQDataSend.getBytes());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        };

                        Runnable SendToWeb = () -> {
                            try {
                                runExternalProcess(""+" "+ReceiveMsg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        };
                        Runnable WriteToDB = () -> {
                            try {
                                runExternalProcess(""+" "+ReceiveMsg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        };
                        Runnable CheckPowerOffEvent = () -> {
                            try {
                                runExternalProcess(""+" "+ReceiveMsg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        };

                        new Thread(WriteToMQ).start();
                        //new Thread(SendToWeb).start();
                        //new Thread(WriteToDB).start();
                        //new Thread(CheckPowerOffEvent).start();
                        
                        //System.out.println(ReceiveMsg);
                        sb.setLength(0);
                        break;
                    }
                }
                /*
                System.out.print("uid:");
                for(int i=0;i<8;i++)
                {
                    System.out.print(data[i]);
                    System.out.print("  ");
                }
                System.out.println();
                */
                //System.out.print("  val:");
                //System.out.print(val);
                //System.out.print("\n");
            } catch (IOException ex) {
                System. err. println(ex);
            } finally {
                try {
                    connection. close();
                } catch (IOException e) {
                    // ignore;
                }
            }
            return null;
        }

        private void runExternalProcess(String command) throws IOException, InterruptedException {
            Process proc = Runtime.getRuntime().exec(command);

            // Read the output

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line = "";
            while((line = reader.readLine()) != null) {
                System.out.print(line + "\n");
            }

            proc.waitFor();
        }

        int booleansToInt(boolean[] arr){
            int n = 0;
            for (boolean b : arr)
                n = (n << 1) | (b ? 1 : 0);
            return n;
        }
        /**
         * Convert a byte array to a boolean array. Bit 0 is represented with false,
         * Bit 1 is represented with 1
         *
         * @param bytes
         *            byte[]
         * @return boolean[]
         */
        boolean[] byteArray2BitArray(byte[] bytes) {
            boolean[] bits = new boolean[bytes.length * 8];
            for (int i = 0; i < bytes.length * 8; i++) {
                if ((bytes[i / 8] & (1 << (7 - (i % 8)))) > 0)
                    bits[i] = true;
            }
            return bits;
        }

        char toHexChar(int input) {
            return input >= 0 && input <= 9?(char)(input + 48):(char)(65 + (input - 10));
        }
    }
}
