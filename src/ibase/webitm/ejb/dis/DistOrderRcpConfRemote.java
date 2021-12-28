package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;

import javax.ejb.EJBObject;

import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;

import javax.ejb.Remote; // added for ejb3
@Remote // added for ejb3

public interface DistOrderRcpConfRemote extends ActionHandlerRemote//,EJBObject
{
    public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
    public String confirm(String tranID, String xtraParams, String forcedFlag,Connection conn) throws RemoteException,ITMException;
    public String actionHandler(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
