package rmi;
import java.rmi.Remote;
import java.rmi.RemoteException;

public abstract interface ASCISInterf extends Remote
{
    public abstract String sayHello(String paramString)
            throws RemoteException;

    public abstract String login(Object paramObject)
            throws RemoteException;
}
