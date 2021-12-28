package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;

import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; 

@Remote 
public interface SaleOrderReleaseRemote extends ActionHandlerRemote
{
	public String actionHandler(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
