package ibase.webitm.ejb.dis.adv;
//new component 
import java.rmi.RemoteException;
import java.sql.Connection;
import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;

public interface DissIssuePosConfLocal extends ValidatorLocal
{
	public String postConfirm() throws RemoteException,ITMException;
	public String postConfirm(String xmlStringAll, String tranId, String xtraParams) throws RemoteException,ITMException;
  

}
