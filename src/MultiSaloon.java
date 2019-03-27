import java.net.*;
import java.nio.channels.*;
import java.io.*;
import java.util.Set;
import java.util.Iterator;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.nio.channels.SelectionKey;

class MultiSaloon {

    /* Démarrage et délégation des connexions entrantes */
    public void demarrer(int port) {

        ServerSocketChannel serv_socket; // socket d'écoute utilisée par le serveur

        System.out.println("Lancement du serveur sur le port " + port);
        try
        {
            serv_socket = ServerSocketChannel.open();
            serv_socket.socket().bind(new InetSocketAddress(port));
            //serv_socket.socket().setReuseAddress(true); /* rend le port réutilisable rapidement */
            serv_socket.configureBlocking(false);

            Selector selector = Selector.open();
            serv_socket.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer buffer;

            HashMap<SocketChannel,String> clients_Map = new HashMap<SocketChannel,String>();

            while (true)
            {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext())
                {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if(key.isAcceptable())
                    {
                        SocketChannel client = serv_socket.accept();
                        Data data = new Data();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, data);
                    }
                    if(key.isReadable())
                    {
                        Data data = new Data();
                        data = key.attachment();
                        SocketChannel client = (SocketChannel) key.channel();
                        buffer = ByteBuffer.allocate(128);
                        client.read(buffer);
                        buffer.flip();
                        data.checkConnection(new String(buffer.array()));
                        if(data.connectionAccepted)
                        {
                            String temp = new String(buffer.array());
                            String pseudo = temp.substring(7);
                            clients_Map.put(client, pseudo);
                        }
                        buffer.clear();
                        int len = buffer.remaining();
                        if(len > 0)
                            System.out.println(clients_Map.get(client)+">"+new String(buffer.array()));
                        buffer.clear();
                    }
                    if(key.isWritable())
                    {
                        SocketChannel client = (SocketChannel) key.channel();
                        buffer = ByteBuffer.allocate(128);
                        String msg = "truc";
                        buffer = msg.getBytes();
                        client.write(msg);
                    }
                }
            }
        } catch (IOException ex)
        {
            System.out.println("Arrêt anormal du serveur."+ ex);
            return;
        }
    }

    public static void main(String[] args) {
        int argc = args.length;
        MultiSaloon serveur;

        /* Traitement des arguments */
        if (argc == 1)
        {
            try
            {
                serveur = new MultiSaloon();
                serveur.demarrer(Integer.parseInt(args[0]));
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        } else
        {
            System.out.println("Usage: java EchoServer port");
        }
        return;
    }

    /*
       echo des messages reçus (le tout via la socket).
       NB classe Runnable : le code exécuté est défini dans la
       méthode run().
    */
    class Handler implements Runnable {

        SocketChannel socket;
        PrintWriter out;
        BufferedReader in;
        InetAddress hote;
        int port;

        Handler(SocketChannel socket) throws IOException
        {
            this.socket = socket;
            out = new PrintWriter(socket.socket().getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.socket().getInputStream()));
            hote = socket.socket().getInetAddress();
            port = socket.socket().getPort();
        }

        public void run()
        {
            String tampon;
            long compteur = 0;

            try
            {
                /* envoi du message d'accueil */
                out.println("Bonjour " + hote + "! (vous utilisez le port " + port + ")");

                do
                {
                    /* Faire echo et logguer */
                    tampon = in.readLine();
                    if (tampon != null)
                    {
                        compteur++;
                        /* log */
                        System.err.println("[" + hote + ":" + port + "]: " + compteur + ":" + tampon);
                        /* echo vers le client */
                        out.println("> " + tampon);
                    } else
                    {
                        break;
                    }
                } while (true);

                /* le correspondant a quitté */
                if(!socket.socket().isClosed())
                {
                    in.close();
                    out.println("Au revoir...");
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