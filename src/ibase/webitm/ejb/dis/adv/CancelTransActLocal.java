package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3
@Local // added for ejb3

public interface CancelTransActLocal extends ActionHandlerLocal	//,EJBObject
{
    public String confirm(String xmlString, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
