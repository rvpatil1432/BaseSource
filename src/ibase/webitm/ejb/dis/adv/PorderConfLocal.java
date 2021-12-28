/********************************************************
	Title : PorderConf[W16CKAT004]
	Date  : 22/06/16
	Developer: Poonam Gole
	
 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerLocal;
import javax.ejb.Local; // added for ejb3

@Local // added for ejb3
public interface PorderConfLocal extends ActionHandlerLocal
{
   public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;

}
