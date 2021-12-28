/********************************************************
	Title : SOrderAmdConf[D16EBAS005]
	Date  : 08/08/16
	Developer: Bhushan Lad
	
 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerLocal;
import javax.ejb.Local; // added for ejb3

@Local // added for ejb3
public interface SOrderAmdConfLocal extends ActionHandlerLocal
{
   public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;

}
