package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import ibase.webitm.utility.ITMException;
//import javax.ejb.CreateException;
//import javax.ejb.EJBHome;

//import ibase.webitm.ejb.ActionHandlerHome;
//import ibase.webitm.ejb.ActionHandler;
import ibase.webitm.ejb.ActionHandlerLocal;
import javax.ejb.Local; // added for ejb3

@Local // added for ejb3
public interface ChrgBckLocConfLocal extends ActionHandlerLocal
{
	//public ActionHandler create() throws RemoteException, CreateException;
	public String confirm() throws RemoteException,ITMException;
	public String confirm(String xmlString, String xtraParams, String objContext) throws RemoteException,ITMException;
}