import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client
{
    public static void main( String[] args )
    {
      InetAddress addr;
      int port;
      try
      {
        if (args.length == 2)
        {
            addr = InetAddress.getByName(args[0]);
            port = Integer.parseInt(args[1]);
        } else
        {
            System.err.println("serveur et/ou port manquant");
            return;
        }
        Socket sock = new Socket(addr, port);
        BufferedOutputStream bos = new BufferedOutputStream(sock.getOutputStream());
        Scanner input = new Scanner(System.in);


        System.out.println("Connected to server");

        do
        {
          byte[] line = (input.nextLine() + "\n").getBytes();
          bos.write(line);
          bos.flush();
        } while (input.hasNextLine());

        input.close();
        bos.close();
        sock.close();
      } catch (IOException e)
      {
          e.printStackTrace();
      }
    }
}
