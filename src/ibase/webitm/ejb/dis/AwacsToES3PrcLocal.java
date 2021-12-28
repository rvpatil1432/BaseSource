package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ProcessLocal;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Local;

@Local
public interface AwacsToES3PrcLocal extends ProcessLocal{
	public String process(String arg0, String arg1, String arg2, String arg3) throws RemoteException, ITMException;
}
