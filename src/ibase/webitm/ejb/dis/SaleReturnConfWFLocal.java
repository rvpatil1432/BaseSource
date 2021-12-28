package ibase.webitm.ejb.dis;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Local;

@Local
public interface SaleReturnConfWFLocal {
	public String confirm(String tran_id, String empCodeAprv, String loginSiteCode, String keyFlag, String userInfoStr) throws RemoteException, ITMException;
}
