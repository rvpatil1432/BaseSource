package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;
import java.sql.Connection;
import javax.ejb.Local;

@Local
public interface SaleOrderPostSaveLocal extends ValidatorLocal {
	public String postSave(String xmlString,String tranId,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException;
}
