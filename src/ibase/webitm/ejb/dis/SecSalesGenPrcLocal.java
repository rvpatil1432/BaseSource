package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ProcessLocal;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Local;

@Local
public interface SecSalesGenPrcLocal extends ProcessLocal{
	public String getData(String arg0, String arg1, String arg2, String arg3) throws RemoteException ,ITMException;
	public String process(String arg0, String arg1, String arg2, String arg3) throws RemoteException, ITMException;
}
