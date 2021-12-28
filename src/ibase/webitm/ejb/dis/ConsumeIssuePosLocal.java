package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import java.sql.Connection;
import javax.ejb.Local; // added for ejb3
import ibase.webitm.utility.ITMException;

//public interface ConsumeIssuePos extends ValidatorLocal, EJBObject //commented for ejb3
@Local // added for ejb3
public interface ConsumeIssuePosLocal extends ValidatorLocal // added for ejb3
{
	public String postSaveRec()throws RemoteException,ITMException;
	public String postSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}