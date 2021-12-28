package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Local;

@Local
public interface PricelistGenerationWfEJBLocal extends ActionHandlerLocal {
	public String confirm(String paramString1, String paramString2,String paramString3) throws RemoteException, ITMException;
}
