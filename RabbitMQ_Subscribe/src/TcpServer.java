/**
 * Created by wavegisAAA on 3/10/2015.
 */

import sun.rmi.runtime.Log;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class TcpServer {
    public final static int PORT = 13;

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

    public static void main(String[] args) {

        BitSet bitset1 = BitSet.valueOf(new byte[]{1,2,3});

        // print the sets
        System.out.println("Bitset1:" + bitset1);
        ExecutorService pool = Executors.newFixedThreadPool(1000);
        try  {
            ServerSocket server = new ServerSocket(PORT);
            while (true) {
                try {
                    Socket connection = server. accept();
                    Callable<Void> task = new CarMsgTask(connection);
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
        public Void call() {
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

                System.out.println("dataLength:"+String.valueOf(dataLength));
                System.out.println("data:  ");
                for (byte n:data)
                {
                    System.out.print(n);
                    System.out.print("   ");
                }
                System.out.println("******************************************");
                System.out.println();
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
                    System.out.println("pointer="+pointer);
                    for(int j=pointer;j<pointer+UID_LENGTH;j++)
                    {
                        uid.append((char)data[j]);
                    }
                    pointer+=UID_LENGTH;

                    for(int j=pointer;j<pointer+STATUS_LENGTH;j++)
                    {
                        status = String.valueOf(data[j]);
                    }
                    pointer+=STATUS_LENGTH;

                    for(int j=pointer;j<pointer+TOTAL_DISTANCE_LENGTH;j++)
                    {
                        totalGoDistance.append((char)data[j]);
                    }
                    pointer+=TOTAL_DISTANCE_LENGTH;

                    byte[] timeBytes=new byte[TIME_LENGTH];
                    for(int j=pointer;j<pointer+TIME_LENGTH;j++)
                    {
                        timeBytes[j-pointer]=data[j];
                    }
                    for(byte a:timeBytes)
                    {
                        System.out.print(a);
                        System.out.print("  ");
                    }
                    //boolean[] timeBools=byteArray2BitArray(timeBytes);
                    BitSet bitSet=BitSet.valueOf(timeBytes);
                    System.out.println("Bitset1:" + bitSet);
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
                    time.append("20"+String.valueOf(yy));
                    pointer+=YEAR_LENGTH;

                    for (int i=pointer;i<pointer+MONTH_LENGTH;i++)
                    {
                        if(bitSet.get(i-timeBaseLoc))
                            MM |= (1 << i-pointer);
                    }
                    time.append(String.valueOf(MM));
                    pointer+=MONTH_LENGTH;

                    for (int i=pointer;i<pointer+DAY_LENGTH;i++)
                    {
                        if(bitSet.get(i-timeBaseLoc))
                            dd |= (1 << i-pointer);
                    }
                    time.append(String.valueOf(dd));
                    pointer+=DAY_LENGTH;

                    for (int i=pointer;i<pointer+HOUR_LENGTH;i++)
                    {
                        if(bitSet.get(i-timeBaseLoc))
                            HH |= (1 << i-pointer);
                    }
                    time.append(String.valueOf(HH));
                    pointer+=HOUR_LENGTH;

                    for (int i=pointer;i<pointer+MINUTE_LENGTH;i++)
                    {
                        if(bitSet.get(i-timeBaseLoc))
                            mm |= (1 << i-pointer);
                    }
                    time.append(String.valueOf(mm));
                    pointer+=MINUTE_LENGTH;

                    for (int i=pointer;i<pointer+SECOND_LENGTH;i++)
                    {
                        if(bitSet.get(i-timeBaseLoc))
                            ss |= (1 << i-pointer);
                    }
                    time.append(String.valueOf(ss));
                    pointer+=SECOND_LENGTH;
                    System.out.println(time.toString());
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
                    mylong.append(String.valueOf((data[pointer]&0xff)+(data[pointer+1]&0xff)/60+(data[pointer+2]&0xff)/3600));
                    pointer+=LONG_LENGTH;
                    myLat.append(String.valueOf((data[pointer]&0xff)+(data[pointer+1]&0xff)/60+(data[pointer+2]&0xff)/3600));
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
                            int RPM=(data[pointer]&0xff)<<8+(data[pointer+1]&0xff);
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
                            break;
                        case 41:
                        case 42:
                            for(int j=pointer;j<pointer+VIDEO_FILENAME_LENGTH;j++)
                            {
                                fileName.append((char)data[j]);
                            }
                            pointer+=VIDEO_FILENAME_LENGTH;
                            break;
                        case 51:
                        case 52:
                        case 53:
                            //do nothing
                            break;
                    }
                    System.out.println("fileName=" + fileName.toString());
                    time.setLength(0);
                    uid.setLength(0);
                    totalGoDistance.setLength(0);
                    mylong.setLength(0);
                    myLat.setLength(0);
                    fileName.setLength(0);
                    if(pointer>dataLength)
                        break;
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
