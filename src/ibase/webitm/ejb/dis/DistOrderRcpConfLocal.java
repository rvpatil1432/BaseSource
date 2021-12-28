package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;

//import javax.ejb.EJBObject;
import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;

import javax.ejb.Local; // added for ejb3
@Local // added for ejb3

public interface DistOrderRcpConfLocal extends ActionHandlerLocal//,EJBObject
{
    public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
    public String confirm(String tranID, String xtraParams, String forcedFlag,Connection conn) throws RemoteException,ITMException;
    public String actionHandler(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
