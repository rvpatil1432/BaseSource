/**
* PURPOSE : Remote Interface for StockAmdConf component
* AUTHOR : Sneha Mestry
* DATE : 12-12-2016
*/

package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;

@javax.ejb.Remote
public interface StockAmdConfRemote extends ActionHandlerRemote
{
	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException;
	
	public String confirm( String tranId, String xtraParams, String forcedFlag, Connection conn) throws RemoteException,ITMException;
	
	//public String postSave()throws RemoteException,ITMException;
	
	//public String postSave( String domString, String tranId, String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException;

}
