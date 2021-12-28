/**
 * Developed by Ritesh On 07/02/14 
 * Purpose: Verify  charge back data (req : DI3FSUN023)
 * */
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;


import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerRemote;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3
public interface ChargeBackFormVerifyRemote extends ActionHandlerRemote//,EJBObject
{
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String actionType, String xmlString,String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException;
}