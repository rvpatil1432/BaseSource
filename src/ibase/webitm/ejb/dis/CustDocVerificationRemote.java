package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3
@Remote // added for ejb3

public interface CustDocVerificationRemote extends ActionHandlerRemote//,EJBObject
{
    public String actionHandler(String xmlString, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
