package ibase.webitm.ejb.dis;
import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import javax.ejb.Local;

@Local
// Changed By Nasruddin 23-SEP-16 extends ValidatorLocal
public interface POrderAmdPostSaveLocal extends ValidatorLocal{
	public String postSave(String xmlString,String tranid,String editFlag,String xtraParams,Connection conn) throws RemoteException,ITMException;

}
