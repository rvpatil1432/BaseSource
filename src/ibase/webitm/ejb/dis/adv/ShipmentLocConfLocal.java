/********************************************************
	Title : ShipmentLocConfLocal
	Date  : 25/04/2014
	Developer: chandrakant patil
	req id : DI3GSUN047
 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;

@javax.ejb.Local
public interface ShipmentLocConfLocal extends ActionHandlerLocal
{
	public String confirm(String tranId, String xtraParam, String forcedFlag) throws RemoteException,ITMException;
}
