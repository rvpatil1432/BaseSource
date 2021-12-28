package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;

import javax.ejb.Local;

@Local
public interface PriceListTranConfLocal extends ActionHandlerLocal {

	@Override
	public String confirm(String arg0, String arg1, String arg2)
			throws RemoteException, ITMException;
}
