/********************************************************
	Title : StockAllocPrc
	Date  : 04/12/2011
	Developer: Dipak Chattar

********************************************************/

package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
import java.sql.Connection;

import javax.ejb.Local; // added for ejb3
@Local 
	
public interface StockAllocPrcLocal extends ActionHandlerLocal//,EJBObject 
{
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
	public String confirm(String tranId,String xtraParams, String forcedFlag,Connection conn) throws RemoteException,ITMException;

}
