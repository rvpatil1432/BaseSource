/**
Title : Local interface for StockTransferPos component
Date  : 29/11/11
Author: Chitranjan Pandey

*/

package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;

@javax.ejb.Local
public interface StockTransferWizPosLocal extends ValidatorLocal
{
	public String postSave()throws RemoteException,ITMException;
	public String postSave( String domString,String tranId, String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException;	
}