package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;

import ibase.webitm.utility.ITMException;
//import ibase.webitm.ejb.ActionHandler;
import ibase.webitm.ejb.ActionHandlerRemote;
import javax.ejb.Remote; // added for ejb3

@Remote
public interface InvFreezeActRemote extends ActionHandlerRemote
{
	public String confirm() throws RemoteException,ITMException;
	public String confirm(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException;
}