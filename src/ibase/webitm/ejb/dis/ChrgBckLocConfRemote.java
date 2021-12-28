package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;

import ibase.webitm.utility.ITMException;
//import ibase.webitm.ejb.ActionHandler;
import ibase.webitm.ejb.ActionHandlerRemote;
import javax.ejb.Remote; // added for ejb3

@Remote
public interface ChrgBckLocConfRemote extends ActionHandlerRemote
{
	public String confirm() throws RemoteException,ITMException;
	public String confirm(String xmlString, String xtraParams, String objContext) throws RemoteException,ITMException;
}