package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import org.w3c.dom.*;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3


@Local // added for ejb3
public interface SaleOrderHoldLocal extends ActionHandlerLocal
{
	public String actionHandler(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
