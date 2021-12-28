package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Local;

@Local
public interface SorderConfWFLocal {
	public String confirm(String saleOrder, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException, ITMException;
}
