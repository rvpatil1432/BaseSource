package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Remote;

@Remote
public interface ProofOfDeliveryDefaultRemote extends ActionHandlerRemote
{
	public abstract String actionHandler()  throws RemoteException, ITMException;

	  public abstract String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException, ITMException;

	  //public String confirm() throws RemoteException, ITMException;
	//
	  public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException;
}
