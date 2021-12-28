package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3
@Local // added for ejb3

public interface CustDocVerificationLocal extends ActionHandlerLocal	//,EJBObject
{
    public String actionHandler(String xmlString, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
