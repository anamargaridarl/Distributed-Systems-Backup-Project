package base;

public class SucessorInfo {


    int port;
    String address;

    public SucessorInfo(String address, int port)
    {
        this.address = address;
        this.port = port;
    }

    public int getPort()
    {
        return port;
    }

    public String getAddress()
    {
        return address;
    }
}
