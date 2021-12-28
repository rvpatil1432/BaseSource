package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.CreateException;
//import javax.ejb.EJBHome;

//import ibase.webitm.ejb.ActionHandlerHome;
//import ibase.webitm.ejb.ActionHandler;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerLocal;
import javax.ejb.Local; // added for ejb3

@Local // added for ejb3
public interface DistIssueActLocal extends ActionHandlerLocal
{
	//public ActionHandler create() throws RemoteException, CreateException;
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException;
}