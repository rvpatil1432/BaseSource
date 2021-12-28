/********************************************************
	Title 	 : 	PContractPostSaveRemote[]
	Date  	 : 	25/JUN/18
	Developer:  Pankaj R.

 ********************************************************/
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ValidatorRemote;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3
public interface PContractPostSaveRemote extends ValidatorRemote
{
	public String postSave() throws RemoteException,ITMException;
	public String postSave(String xmlStringAll, String tranId, String editFlag, String xtraParams, Connection conn) throws RemoteException,ITMException;
}