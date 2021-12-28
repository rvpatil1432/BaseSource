package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;


import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerRemote;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3
public interface EnquiryActRemote extends ActionHandlerRemote//,EJBObject
{
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException;
}