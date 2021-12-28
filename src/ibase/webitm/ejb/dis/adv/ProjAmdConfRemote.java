package ibase.webitm.ejb.dis.adv;


import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Remote;

import org.w3c.dom.Document;

@Remote
public interface ProjAmdConfRemote extends ActionHandlerRemote {

	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException, ITMException;
	
}
