package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
//import javax.ejb.EJBObject;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ValidatorLocal;
import javax.ejb.Local; // added for ejb3


//public interface StockTransferPrs extends ValidatorLocal, EJBObject
@Local // added for ejb3
public interface StockTransferPrsLocal extends ValidatorLocal
{
	public String preSaveRec()throws RemoteException,ITMException;
	public String preSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}