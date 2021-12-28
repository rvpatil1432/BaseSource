/********************************************************
	Title : CustStockOrderLocal[D16EBAS009]
	Date  : 27/08/16

 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerLocal;
import javax.ejb.Local; // added for ejb3

@Local // added for ejb3
public interface CustStockOrderLocal extends ActionHandlerLocal
{
   public String submit(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
