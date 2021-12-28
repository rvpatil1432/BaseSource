package ibase.webitm.ejb.dis.adv;


import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Local;

import org.w3c.dom.Document;

@Local
public interface ProjAmdConfLocal extends ActionHandlerLocal {

	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException, ITMException;
	
	
}
