package ibase.webitm.ejb.dis;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import java.rmi.RemoteException;
import java.sql.Connection;

@javax.ejb.Remote
public interface ConsIssuePostSaveRemote extends ValidatorRemote
{
	
	//public String postSaveRec()throws RemoteException,ITMException;
	//public String postSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;

	public String postSave()throws RemoteException,ITMException;
	//public String postSave( String domString,String tranId, String objContext, String editFlag, String xtraParams ) throws RemoteException,ITMException;
	public String postSave( String domString,String tranId, String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException;

}
 
