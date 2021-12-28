package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import java.sql.Connection;
//import javax.ejb.EJBObject; 
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ValidatorRemote;
import javax.ejb.Remote;// added for ejb3
//public interface AdjIssPos extends ValidatorRemote, EJBObject //commented for ejb3

@Remote // added for ejb3
public interface AdjIssPosRemote extends ValidatorRemote
{
	public String postSaveRec()throws RemoteException,ITMException;
	public String postSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}