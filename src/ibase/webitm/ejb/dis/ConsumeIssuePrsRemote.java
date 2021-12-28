package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import java.sql.Connection;

import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3

//public interface ConsumeIssuePrs extends ValidatorRemote, EJBObject //commented for ejb3
@Remote // added for ejb3
public interface ConsumeIssuePrsRemote extends ValidatorRemote // added for ejb3
{
	public String preSaveRec()throws RemoteException,ITMException;
	public String preSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}