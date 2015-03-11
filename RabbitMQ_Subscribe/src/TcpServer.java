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
    public static void main(String[] args) {
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
                byte[] data = new byte[dataLength];
                in.read(data);
                StringBuilder sb = new StringBuilder();
                for(int i=0;i<data.length;i++)
                {

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
    }
}
