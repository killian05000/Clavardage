import java.net.*;
import java.util.concurrent.*;
import java.io.*;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

class SalonCentral
{
  ArrayList<Handler> tabHandler = new ArrayList<Handler>();
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
         tabHandler.add(ch);
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

  public void clientSendAll(String pseudo, String msg)
  {
      for(int i=0; i< tabHandler.size(); i++)
      {/*
        if(tabHandler.get(i).pseudo != pseudo && tabHandler.get(i).pseudo != null)
          tabHandler.get(i).out.println(pseudo+"> "+msg);
          */
          if(tabHandler.get(i).pseudo != null)
          {
            tabHandler.get(i).out.println(pseudo+"> "+msg);
          }
      }
  }

  public void serverSendAll(String msg)
  {
      for(int i=0; i< tabHandler.size(); i++)
      {
        if(tabHandler.get(i).pseudo != null)
          tabHandler.get(i).out.println("[server] : "+msg);
      }
  }

  /*
     echo des messages reçus (le tout via la socket).
     NB classe Runnable : le code exécuté est défini dans la
     méthode run().
  */
  class Handler implements Runnable
  {

    ArrayBlockingQueue<String> queue;

    //Socket socket;
    PrintWriter out;
    BufferedReader in;
    //InetAddress hote;
    //int port;
    //String pseudo;
    Socket sock;
    InetAddress addr;
    int port;
    String pseudo;

    Handler(Socket socket) throws IOException
    {
	queue = new ArrayBlockingQueue<String>(10);
      sock = socket;
      addr = socket.getInetAddress();
      port = socket.getPort();

      //this.socket = socket;
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      //hote = socket.getInetAddress();
      //port = socket.getPort();
    }

    public void run()
    {
      String tampon;
      long compteur = 0;

      try
      {
        tampon = in.readLine();
        String[] lign = tampon.split("\\s+");

        if(!lign[0].equals("CONNECT") || lign.length==1)
        {
          out.println("ERROR CONNECT aborting clavardamu protocol.");
          System.out.println("--connection attempt failed--");
          String mendiant = "Un mendiant a essayé de s'introduire dans le chateau, n'ayez craintes, nos gardes l'on repoussé";
          serverSendAll(mendiant);
          sock.close();
          return;
        }

        pseudo = lign[1];
        System.out.println(pseudo+" vient de se connecter");
        out.println("--Accepted by server--");
        serverSendAll(pseudo+" vient de se connecter");

        do
        {
          tampon = in.readLine();

          if (tampon != null)
          {
            compteur++;
            /* log */
            System.out.println("["+pseudo+"]: " + tampon);
            /* echo vers le client */
            clientSendAll(pseudo, tampon);
            //out.println(">msg send");
          } else
          {
            break;
          }
        } while (true);

        /* le correspondant a quitté */
        if(!sock.isClosed())
        {
          in.close();
          out.println("Fermeture de connexion");
          out.close();
          sock.close();
          serverSendAll(pseudo+" nous a quitté...");
          System.err.println(pseudo+" nous a quitté...");
        }
      } catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }
}
