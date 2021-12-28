package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;//commented for ejb3
import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local;//added for ejb3

@Local // added for ejb3
public interface DistOrderAmdConfLocal extends ActionHandlerLocal //,EJBObject //commented for ejb3
{
    public String confirm(String xmlString, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
