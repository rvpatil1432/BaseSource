package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import java.sql.Connection;

import ibase.webitm.utility.ITMException;

import javax.ejb.Remote;

@Remote 


public interface DissIssuePosSaveRemote extends ValidatorRemote //, EJBObject
{
	public String postSave(String winName,String editFlag,String tranId,  String xtraParams,Connection conn) throws RemoteException,ITMException;
	public String postSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
	public String postSaveCall(String xmlString,String editFlag,String tranId,  String xtraParams,Connection conn) throws RemoteException,ITMException;
}