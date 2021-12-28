/********************************************************
	Title : CustStockOrderRemote[D16EBAS009]
	Date  : 27/08/16

 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3
public interface CustStockOrderRemote extends ActionHandlerRemote
{
		public String submit(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
