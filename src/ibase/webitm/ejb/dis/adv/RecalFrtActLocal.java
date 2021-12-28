/********************************************************
	Title : RecalFrtActLocal
	Date  : 25/04/2014
	Developer: chandrakant patil
	req id : DI3GSUN047
 ********************************************************/


package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import org.w3c.dom.*;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3


@Local // added for ejb3
public interface RecalFrtActLocal extends ActionHandlerLocal
{
	public String actionHandler(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
