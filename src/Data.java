public class Data
{
    boolean connectionAccepted = false;

    boolean checkConnection(String connect)
    {
        if(connect.substring(0,6)=="CONNECT")
        {
            if(connect.length()>8)
                connectionAccepted=true;
        }

        return connectionAccepted;
    }
}
