package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Remote;

@Remote
public interface PricelistGenerationWfEJBRemote extends ActionHandlerRemote {
	public String confirm(String paramString1, String paramString2,String paramString3) throws RemoteException, ITMException;
}
