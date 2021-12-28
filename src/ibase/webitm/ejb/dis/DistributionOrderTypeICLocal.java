package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;

import javax.ejb.Local;

@Local
public interface DistributionOrderTypeICLocal extends ValidatorLocal {

	@Override
	public String wfValData(String arg0, String arg1, String arg2, String arg3,
			String arg4) throws RemoteException, ITMException				;
}	