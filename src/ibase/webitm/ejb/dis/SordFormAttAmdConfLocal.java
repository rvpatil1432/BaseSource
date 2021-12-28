package ibase.webitm.ejb.dis;  
  
/* REMOTE INTERFACE  WHICH CONTAINS BUSINESS METHODS */
 

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerLocal;
import javax.ejb.Local; // added for ejb3


//public interface SordFormAttAmdConf extends ActionHandlerLocal,EJBObject
@Local // added for ejb3
public interface SordFormAttAmdConfLocal extends ActionHandlerLocal
{
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String tranIDIns, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}

