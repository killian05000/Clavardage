import java.net.*;
import java.util.concurrent.*;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.lang.Exception;

class SalonCentral
{

  /* Démarrage et délégation des connexions entrantes */
  public void demarrer(int port) throws Exception
  {
    ServerSocket serv_socket; // socket d'écoute utilisée par le serveur
    Socket cli_socket;

    System.out.println("Lancement du serveur sur le port " + port);
    try
    {
      serv_socket = new ServerSocket(port);
      serv_socket.setReuseAddress(true); /* rend le port réutilisable rapidement après fermeture du serveur */
      ExecutorService pool = Executors.newCachedThreadPool();

      while (true)
      {
	       //(new Handler(serv_socket.accept())).run();
         cli_socket = serv_socket.accept();
         Handler ch = new Handler(cli_socket);
         pool.execute(ch);
      }
    } catch (IOException ex)
    {
      System.out.println("Arrêt anormal du serveur."+ ex);
      return;
    }
  }

  public static void main(String[] args)
  {
    int argc = args.length;
    SalonCentral serveur;

    /* Traitement des arguments */
    if (argc == 1)
    {
      try
      {
        serveur = new SalonCentral();
        serveur.demarrer(Integer.parseInt(args[0]));
      } catch (Exception e)
      {
        e.printStackTrace();
      }
    } else
    {
      System.out.println("Usage: java SalonCentral port");
    }
    return;
  }

  /*
     echo des messages reçus (le tout via la socket).
     NB classe Runnable : le code exécuté est défini dans la
     méthode run().
  */
  class Handler implements Runnable
  {
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    InetAddress hote;
    int port;
    String pseudo;

    Handler(Socket socket) throws IOException
    {
      this.socket = socket;
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      hote = socket.getInetAddress();
      port = socket.getPort();
    }

    public void run()
    {
      String tampon;
      long compteur = 0;

      try
      {
        tampon = in.readLine();
        System.out.println("1");
        String[] lign = tampon.split("\\s+");
        System.out.println("2");

        if(!lign[0].equals("CONNECT") || lign.length==1)
        {
          System.out.println("3");
          out.println("ERROR CONNECT aborting clavardamu protocol.");
          System.out.println("4");
          socket.close();
          System.out.println("5");
          return;
        }

        System.out.println("6");

        pseudo = lign[1];

        do
        {
          tampon = in.readLine();

          if (tampon != null)
          {
            compteur++;
            /* log */
            System.out.println("["+pseudo+"]: " + tampon);
            /* echo vers le client */
            out.println(pseudo+"> " + tampon);
          } else
          {
            break;
          }
        } while (true);

        /* le correspondant a quitté */
        if(!socket.isClosed())
        {
          in.close();
          out.println("Fermeture de connexion");
          out.close();
          socket.close();

          System.err.println("[" + hote + ":" + port + "]: Terminé...");
        }
      } catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }
}
