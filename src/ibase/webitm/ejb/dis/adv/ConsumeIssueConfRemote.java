package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;

import javax.ejb.Remote;

import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;
@Remote
public interface ConsumeIssueConfRemote extends ActionHandlerRemote//, EJBObject
{
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
