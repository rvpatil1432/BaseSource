/**
* PURPOSE : Local Interface
* Author :Sumit Sarkar
*/

package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;

@javax.ejb.Local
public interface SRLContainerPrsLocal extends ValidatorLocal
{
	public String preSave()throws RemoteException,ITMException;
	public String preSave(String xmlString, String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}
