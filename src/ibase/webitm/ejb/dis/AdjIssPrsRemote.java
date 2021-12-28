package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
//import javax.ejb.EJBObject; // commented for ejb3
import javax.ejb.Remote; // added for ejb3
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ValidatorRemote;

//public interface AdjIssPrs extends ValidatorRemote, EJBObject // commented for ejb3
@Remote // added for ejb3
public interface AdjIssPrsRemote extends ValidatorRemote //added for ejb3
{
	public String preSaveRec()throws RemoteException,ITMException;
	public String preSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}