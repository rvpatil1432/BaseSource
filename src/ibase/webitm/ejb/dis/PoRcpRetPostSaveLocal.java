package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;

import javax.ejb.Local;

import ibase.webitm.utility.ITMException;

@Local
public interface PoRcpRetPostSaveLocal
{
	public String postSave(String xmlString,String tranId ,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException;
}
