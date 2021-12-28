package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import java.sql.Connection;

import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3


//public interface DespatchPos extends ValidatorLocal, EJBObject
@Local // added for ejb3
public interface DespatchPosLocal extends ValidatorLocal
{
	public String postSaveRec()throws RemoteException,ITMException;
	public String postSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
	//added by kunal on 09/007/13
	public String postSave(String winName,String editFlag,String tranId,  String xtraParams,Connection conn) throws RemoteException,ITMException;
	public String postSaveWiz(String xmlString,String tranId,String editFlag,  String xtraParams,Connection conn) throws RemoteException,ITMException;
}