package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;

import javax.ejb.Local;

@Local
public interface POrderPostSaveLocal extends ValidatorLocal {
	public String postSave(String xmlString,String tranId,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException;

}
