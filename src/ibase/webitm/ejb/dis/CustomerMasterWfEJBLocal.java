package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Local;

@Local
public interface CustomerMasterWfEJBLocal extends ValidatorLocal {
	public String insertCustomerMaster(String objName,String tranId,String xmlDataAll,String docId,String xtraParams,String entityCode) throws RemoteException, ITMException;
}
