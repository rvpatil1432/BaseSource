package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Remote;

@Remote
public interface CustomerMasterWfEJBRemote extends ValidatorRemote {
	public String insertCustomerMaster(String objName,String tranId,String xmlDataAll,String docId,String xtraParams,String entityCode) throws RemoteException, ITMException;
}
