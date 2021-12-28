package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Local;

@Local
public abstract interface DistOpenPrcLocal
{
  public abstract String actionHandler(String paramString1, String paramString2, String paramString3)
    throws RemoteException, ITMException;
  
  public String opendistOrder(String distOrder, String xtraParams, String forcedFlag) throws RemoteException, ITMException;
}