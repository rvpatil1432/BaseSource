/**
* PURPOSE : Remote Interface 
* Author : Sumit Sarkar
*/

package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;

@javax.ejb.Remote
public interface SRLContainerPrsRemote extends ValidatorRemote//, EJBObject
{
	public String preSave()throws RemoteException,ITMException;
	public String preSave(String xmlString, String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}
