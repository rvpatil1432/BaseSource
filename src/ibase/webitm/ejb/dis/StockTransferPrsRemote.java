package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
//import javax.ejb.EJBObject;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ValidatorRemote;
import javax.ejb.Remote; // added for ejb3

//public interface StockTransferPrs extends ValidatorRemote, EJBObject
@Remote // added for ejb3
public interface StockTransferPrsRemote extends ValidatorRemote
{
	public String preSaveRec()throws RemoteException,ITMException;
	public String preSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}