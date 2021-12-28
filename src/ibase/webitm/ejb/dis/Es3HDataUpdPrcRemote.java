package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ProcessRemote;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Remote;

@Remote
public interface Es3HDataUpdPrcRemote extends ProcessRemote{
	public abstract String process(String arg0, String arg1, String arg2, String arg3) throws RemoteException, ITMException;
}
