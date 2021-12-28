/********************************************************
	Title : ShipmentLocDefaultActLocal
	Date  : 25/04/2014
	Developer: chandrakant patil
	req id : DI3GSUN047
 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;

@javax.ejb.Local
public interface ShipmentLocDefaultActLocal extends ActionHandlerLocal
{
	public String actionHandler( String actionType, String xmlString, String xmlString1, String xmlString2, String objContext, String xtraParams) throws RemoteException,ITMException;
}
