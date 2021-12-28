package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3
public interface InvHoldConfRemote extends ActionHandlerRemote//,EJBObject
{
    public String confirm(String xmlString, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
