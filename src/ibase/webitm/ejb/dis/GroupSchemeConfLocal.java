package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import javax.ejb.Local;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;

@Local
public interface GroupSchemeConfLocal extends ActionHandlerLocal {
	
	
	 public String confirm(String schemeCode, String xtraParams,String forcedFlag) throws RemoteException, ITMException;

}
