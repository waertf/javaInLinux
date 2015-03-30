import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by wavegisAAA on 3/30/2015.
 */
public class SendDataToWebServer {


    public static void main(String[] args)  {

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        String sendData = args[2];

        Socket echoSocket  = null;
        try {
            echoSocket = new Socket(hostName, portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter out =
                null;
        try {
            out = new PrintWriter(echoSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.println(sendData);
        System.out.println(sendData);
        try {
            echoSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
