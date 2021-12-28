/*
	Purpose: Local Interface created for StockTransferConf Component
	Author: Gulzar on 13/09/11
*/

package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import java.rmi.RemoteException;
import java.sql.Connection;

@javax.ejb.Local
public interface StockTransferConfLocal extends ActionHandlerLocal
{
	public String confirm( String tranId, String xtraParams, String forcedFlag )throws RemoteException,ITMException;
	//added by chitranjan for call confirm method
	public String confirm( String tranId, String xtraParams,String forcedFlag, Connection conn, boolean connStatus) throws RemoteException,ITMException;
}
