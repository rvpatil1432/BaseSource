/********************************************************
	Title 	 : 	GRNTransfWizPosRemote[D14HFRA001]
	Date  	 : 	10/11/14
	Developer:  Chandrashekar

********************************************************/
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
//import javax.ejb.EJBObject;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ValidatorRemote;
import javax.ejb.Remote; // added for ejb3


//public interface StockTransferPos extends ValidatorRemote, EJBObject
@Remote // added for ejb3
public interface GRNTransfWizPosRemote extends ValidatorRemote
{
	public String postSave()throws RemoteException,ITMException;
	public String postSave( String domString,String tranId, String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException;
}