package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
import java.sql.Connection;

import javax.ejb.Local;

import org.w3c.dom.Document;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException; 
@Local 
public interface DistIssueSyncLocal extends ActionHandlerLocal 
{
	public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException ,ITMException;
	public String confirm(String tranId, String xtraParams, String forcedFlag, Connection conn ) throws RemoteException,ITMException;
}

