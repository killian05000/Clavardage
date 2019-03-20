import java.util.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;


public class Saloon
{
    public static void main(String[] args)
    {
        int port;

        try
        {
            if (args.length==1)
            {
                port = Integer.parseInt(args[0]);
            }
            else
            {
                System.out.println("Port manquant");
                System.out.println("Veuillez passer le port en premier argument.");
                return;
            }

            ServerSocket sock = new ServerSocket(port);
            Socket clientSocket = sock.accept();
            BufferedInputStream in = new BufferedInputStream(clientSocket.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());

            byte[] data = new byte[1024];
            String msg;

            List cliList = new LinkedList<String>();
            int l = in.read(data);
            String pseudo = new String(data,0,l);
            cliList.add(pseudo);
            System.out.println(pseudo+" connected");

            do
            {
                int len = in.read(data);
                if(len > 0)
                {
                    msg = pseudo+">" + new String(data, 0, len);
                    System.out.print(msg);
                }
            } while(clientSocket.isConnected());

            sock.close();
            clientSocket.close();
            in.close();
            out.close();

        }
        catch(Exception e)
        {
            System.out.println("Une erreur s'est produite");
            e.printStackTrace();
        }
    }
}
