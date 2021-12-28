package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;

import javax.ejb.Local;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;
@Local
public interface ConsumeIssueConfLocal  extends ActionHandlerLocal//, EJBObject
{
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}

