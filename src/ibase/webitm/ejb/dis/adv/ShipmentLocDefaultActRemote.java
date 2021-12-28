/********************************************************
	Title : ShipmentLocDefaultActRemote
	Date  : 25/04/2014
	Developer: chandrakant patil
	req id : DI3GSUN047
 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerRemote;

@javax.ejb.Remote
public interface ShipmentLocDefaultActRemote extends ActionHandlerRemote
{
	public String actionHandler( String actionType, String xmlString, String xmlString1, String xmlString2, String objContext, String xtraParams) throws RemoteException,ITMException;
}