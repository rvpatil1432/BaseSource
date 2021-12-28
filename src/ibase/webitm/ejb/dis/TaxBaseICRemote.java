package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;

import javax.ejb.Remote;

@Remote
public interface TaxBaseICRemote extends ValidatorRemote{

	@Override
	public String wfValData(String arg0, String arg1, String arg2, String arg3,
			String arg4) throws RemoteException, ITMException;
	
	@Override
	public String itemChanged(String arg0, String arg1, String arg2,
			String arg3, String arg4, String arg5) throws RemoteException,
			ITMException;
}
