package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import java.sql.Connection;

import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3


//public interface CustSerCreditPrs extends ValidatorRemote, EJBObject
@Remote // added for ejb3
public interface CustSerCreditPrsRemote extends ValidatorRemote
{
	public String preSaveForm()throws RemoteException,ITMException;
	public String preSaveForm(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}