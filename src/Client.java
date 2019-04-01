import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client
{
    public static void main( String[] args ) throws Exception
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
        Handler ch = new Handler(sock);
        ExecutorService pool = Executors.newCachedThreadPool();
        pool.execute(ch);


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


    static class Handler implements Runnable
    {
      Socket socket;
      BufferedReader in;

      Handler(Socket socket) throws IOException
      {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      }

      public void run()
      {
        String tampon;
        long compteur = 0;

        try
        {
          do {
              tampon = in.readLine();
              if(tampon != null)
              System.out.println(tampon);
          } while (socket.isConnected());
        } catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    }
}
