package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
import java.sql.Connection;

import javax.ejb.Remote;

import org.w3c.dom.Document;

import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException; 
@Remote 
public interface DistIssueSyncRemote extends ActionHandlerRemote
{
	public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException ,ITMException;
	public String confirm(String tranId, String xtraParams, String forcedFlag, Connection conn ) throws RemoteException,ITMException;
}
