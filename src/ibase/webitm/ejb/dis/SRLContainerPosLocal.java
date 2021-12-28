package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;
import java.sql.Connection;
import javax.ejb.Local;

@Local
public interface SRLContainerPosLocal extends ValidatorLocal {
	public String postSave(String xmlString,String serialNo,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException;
}
