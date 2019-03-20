import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main( String[] args ){
        try {
            InetAddress addr;
            int port;

            if (args.length == 2) {
                addr = InetAddress.getByName(args[0]);
                port = Integer.parseInt(args[1]);
            } else {
                System.err.println("serveur et/ou port manquant");
                return;
            }

            Socket sock = new Socket(addr, port);
            BufferedOutputStream bos = new BufferedOutputStream(sock.getOutputStream());
            Scanner input = new Scanner(System.in);


            System.out.println("Connected to server");
            byte[] pseudo = idRequest(input);
            bos.write(pseudo);
            bos.flush();

            do {
                byte[] line = (input.nextLine()).getBytes();
                bos.write(line);
                bos.flush();
            } while (input.hasNextLine());

            input.close();
            bos.close();
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static byte[] idRequest(Scanner sc){
        String pseudo;
        byte[] tab = "defaultPseudo".getBytes();
        try {
            sc = new Scanner(System.in);
            System.out.print("Saisissez votre pseudo : ");
            pseudo = sc.nextLine();
            tab = pseudo.getBytes();
        } catch(Exception e)
        {
            e.printStackTrace();
        }
        return tab;
    }
}
