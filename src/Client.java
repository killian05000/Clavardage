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
import java.util.concurrent.ArrayBlockingQueue;

public class Client
{
    //ArrayBlockingQueue<String> receivingQueue = new ArrayBlockingQueue<String>(10);
    Socket sock;
    InetAddress addr;
    int port;
    BufferedOutputStream bos;
    Scanner input;
    String pseudo;


    public void start( String[] args ) throws Exception
    {
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
        sock = new Socket(addr, port);
        bos = new BufferedOutputStream(sock.getOutputStream());
        input = new Scanner(System.in);


        System.out.println("Connected to server");
        Handler ch = new Handler(sock);
        ExecutorService pool = Executors.newCachedThreadPool();
        pool.execute(ch);


        while (input.hasNextLine() && !sock.isClosed())
        {
          byte[] line = (input.nextLine() + "\n").getBytes();
          bos.write(line);
          bos.flush();
          if(sock.isOutputShutdown())
          {
            System.out.println("outpt shutdown");
            input.close();
            bos.close();
            sock.close();
          }
        }

        input.close();
        bos.close();
        sock.close();
      } catch (IOException e)
      {
          e.printStackTrace();
          System.exit(1);
      }
    }



    public class Handler implements Runnable
    {
      Socket sock;
      BufferedReader in;
      //Client cli;

      Handler(Socket socket) throws IOException
      {
        //System.out.println("TA MERE JAVA");
        sock = socket;
        //System.out.println("TA MERE JAVA 2");
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      }

      public void run()
      {
        long compteur = 0;

        try
        {
          do {
        	  String tampon = in.readLine();
              //System.out.println("reseau fdp");

              if(tampon != null)
                System.out.println(tampon);;
                if(tampon=="ERROR CONNECT aborting clavardamu protocol.")
                {
                  sock.shutdownOutput();
                  sock.shutdownInput();
                  sock.close();
                }
          } while (compteur==0);
          sock.close();
        } catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    }
}
