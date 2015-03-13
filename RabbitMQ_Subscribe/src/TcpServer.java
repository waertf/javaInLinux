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
                System.out.println();
                StringBuilder sb = new StringBuilder();
                StringBuilder uid = new StringBuilder();
                String status;
                int myStatus;
                StringBuilder totalGoDistance=new StringBuilder();
                StringBuilder time=new StringBuilder();
                StringBuilder mylong = new StringBuilder();
                StringBuilder myLat = new StringBuilder();
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
                    mylong.append(String.valueOf((data[pointer]&0xff)+(data[pointer+1]&0xff)/60+(data[pointer+2]&0xff)/3600));
                    pointer+=LONG_LENGTH;
                    myLat.append(String.valueOf((data[pointer]&0xff)+(data[pointer+1]&0xff)/60+(data[pointer+2]&0xff)/3600));
                    pointer+=LAT_LENGTH;
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
    }
}
