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

    public final static int UID_LENGTH=8;
    public final static int STATUS_LENGTH=1;
    public final static int TOTAL_DISTANCE_LENGTH=6;
    public final static int TIME_LENGTH=4;

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
                StringBuilder sb = new StringBuilder();
                StringBuilder uid = new StringBuilder();
                String status;
                int myStatus;
                StringBuilder totalGoDistance=new StringBuilder();
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
                    boolean[] timeBools=byteArray2BitArray(timeBytes);
                    for(boolean a:timeBools)
                    {
                        System.out.print(a);
                        System.out.print("  ");
                    }
                    pointer+=TIME_LENGTH;
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