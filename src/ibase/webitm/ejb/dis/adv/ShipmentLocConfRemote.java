/*******************************************************
	Title : ShipmentLocConfRemote
	Date  : 25/04/2014
	Developer: chandrakant patil
	req id : DI3GSUN047
 ********************************************************/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerRemote;

@javax.ejb.Remote
public interface ShipmentLocConfRemote extends ActionHandlerRemote
{
	public String confirm(String tranId, String xtraParam, String forcedFlag) throws RemoteException,ITMException;
}