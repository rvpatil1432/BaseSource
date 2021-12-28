package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
//import javax.ejb.EJBObject; 

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ValidatorLocal;
import javax.ejb.Local; // added for ejb3
//public interface PriceListPos extends ValidatorLocal, EJBObject
@Local // added for ejb3
public interface PriceListPosLocal extends ValidatorLocal
{
	public String postSaveRec()throws RemoteException,ITMException;
	public String postSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}