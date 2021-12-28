package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;

import ibase.webitm.utility.ITMException;
//import ibase.webitm.ejb.ActionHandler;
import ibase.webitm.ejb.ActionHandlerRemote;
import javax.ejb.Remote; // added for ejb3

@Remote
public interface DistIssueActRemote extends ActionHandlerRemote
{
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException;
}