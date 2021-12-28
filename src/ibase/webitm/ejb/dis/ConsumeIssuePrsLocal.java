package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import java.sql.Connection;

import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3

//public interface ConsumeIssuePrs extends ValidatorLocal, EJBObject //commented for ejb3
@Local // added for ejb3
public interface ConsumeIssuePrsLocal extends ValidatorLocal // added for ejb3
{
	public String preSaveRec()throws RemoteException,ITMException;
	public String preSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}