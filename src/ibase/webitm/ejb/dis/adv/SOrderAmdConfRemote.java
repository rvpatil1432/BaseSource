/********************************************************
	Title : SOrderAmdConf[D16EBAS005]
	Date  : 08/08/16
	Developer: Bhushan Lad
	
 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3
public interface SOrderAmdConfRemote extends ActionHandlerRemote
{
		public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
