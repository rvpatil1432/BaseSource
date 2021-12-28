package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerRemote;
import javax.ejb.Remote; 

@Remote

public interface PoamdDlvDefaultRemote extends ActionHandlerRemote
{
	public String actionHandler() throws RemoteException,ITMException;
	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException;
}
