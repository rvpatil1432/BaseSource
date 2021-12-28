package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import java.sql.Connection;

import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3


//public interface DespatchPrs extends ValidatorLocal, EJBObject
@Local // added for ejb3
public interface DespatchPrsLocal extends ValidatorLocal
{
	public String preSaveRec()throws RemoteException,ITMException;
	public String preSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}