/**
Title : StockTransferPosLocal
Date  : 29/11/11
Author: Chitranjan Pandey

*/

package ibase.webitm.ejb.dis;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import java.rmi.RemoteException;
import java.sql.Connection;

@javax.ejb.Remote
public interface StockTransferWizPosRemote extends ValidatorRemote
{
	public String postSave()throws RemoteException,ITMException;
	public String postSave( String domString,String tranId, String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException;

}
