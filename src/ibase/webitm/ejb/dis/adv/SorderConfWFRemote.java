package ibase.webitm.ejb.dis.adv;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Remote;

@Remote
public interface SorderConfWFRemote {
	public String confirm(String saleOrder, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException, ITMException;
}
