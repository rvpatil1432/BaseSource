package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;


import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerLocal;
import javax.ejb.Local; // added for ejb3

import org.w3c.dom.Document;

@Local // added for ejb3
public interface SorderActLocal extends ActionHandlerLocal//,EJBObject
{
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException;
	public String getFreeSchemeaction(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException;
	public String getFreeSchemeaction(Document dom,Document dom1, String objContext, String xtraParams)throws ITMException;
}