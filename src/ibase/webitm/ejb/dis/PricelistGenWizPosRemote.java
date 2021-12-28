package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;
import java.sql.Connection;
import javax.ejb.Remote;

@Remote
public interface PricelistGenWizPosRemote extends ValidatorRemote {
	public String postSave(String xmlString,String tranId,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException;
}
