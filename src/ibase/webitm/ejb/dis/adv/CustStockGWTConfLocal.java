/********************************************************
	Title : CustStockGWTConfLocal[D15ESUN013]
	Date  : 27/10/15
	Developer: Chandrashekar

 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerLocal;
import javax.ejb.Local; // added for ejb3

@Local // added for ejb3
public interface CustStockGWTConfLocal extends ActionHandlerLocal
{
   public String submit(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
   public String open(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
