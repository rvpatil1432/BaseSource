package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.Connection;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;

@javax.ejb.Local
public interface SalesReturnConfLocal extends ActionHandlerLocal
{
	/*public String postSave()throws RemoteException,ITMException;
	public String postSave(String domString, String tranId,String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException;*/
	public String confirm(String pOrder, String xtraParams, String forcedFlag)throws RemoteException, ITMException;
	public String confirmSalesReturn(String msalereturn,String xtraParams, Connection conn) throws ITMException;
}
