package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3
public interface InvPackPrcRemote extends ActionHandlerRemote
{
	
	public String actionHandler(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
	
}
