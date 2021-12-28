package ibase.webitm.ejb.dis;  
  
/* REMOTE INTERFACE  WHICH CONTAINS BUSINESS METHODS */
 

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerRemote;
import javax.ejb.Remote; // added for ejb3


//public interface SordFormAttAmdConf extends ActionHandlerRemote,EJBObject
@Remote // added for ejb3
public interface SordFormAttAmdConfRemote extends ActionHandlerRemote
{
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String tranIDIns, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}

