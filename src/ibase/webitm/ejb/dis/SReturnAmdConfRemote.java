package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3
public interface SReturnAmdConfRemote extends ActionHandlerRemote
{
	public String actionHandler() throws RemoteException,ITMException;
	
	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException;
	
	public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
