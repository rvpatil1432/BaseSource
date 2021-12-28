package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote;

@Remote
public interface SOrderAmdConfWFRemote {
	public String confirm(String saleOrderAmd, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException, ITMException;
}
