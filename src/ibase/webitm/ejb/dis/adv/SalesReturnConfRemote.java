package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.Connection;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerRemote;

@javax.ejb.Remote
public interface SalesReturnConfRemote extends ActionHandlerRemote
{
/*	public String postSave()throws RemoteException,ITMException;
	public String postSave(String domString, String tranId,String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException;*/
	public String confirm(String pOrder, String xtraParams, String forcedFlag)throws RemoteException, ITMException;
	public String confirmSalesReturn(String msalereturn,String xtraParams, Connection conn) throws ITMException;
}