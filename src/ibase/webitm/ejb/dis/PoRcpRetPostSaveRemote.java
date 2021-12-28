package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;

import javax.ejb.Remote;

import ibase.webitm.utility.ITMException;

@Remote
public interface PoRcpRetPostSaveRemote 
{
	public String postSave(String xmlString,String tranId,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException;
}
