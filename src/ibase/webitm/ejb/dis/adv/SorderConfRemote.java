/********************************************************
	Title : SorderConfRemote[D16BSUN008]
	Date  : 23/05/16
	Developer: Chandrashekar

 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3
public interface SorderConfRemote extends ActionHandlerRemote
{
		public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
