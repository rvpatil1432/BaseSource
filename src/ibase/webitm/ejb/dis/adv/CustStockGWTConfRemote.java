/********************************************************
	Title : CustStockGWTConfRemote[D15ESUN013]
	Date  : 27/10/15
	Developer: Chandrashekar

 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3
public interface CustStockGWTConfRemote extends ActionHandlerRemote
{
		public String submit(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
		public String open(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
