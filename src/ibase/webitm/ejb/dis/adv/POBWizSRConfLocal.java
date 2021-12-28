package ibase.webitm.ejb.dis.adv;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Local;

@Local
public abstract interface POBWizSRConfLocal extends ActionHandlerLocal
{
   public String pobConfirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
   public String pobsrConfirm(String tranId, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException,ITMException;
   //public String submit(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
   public String confirm(String tranId, String xtraParams,String forcedFlag) throws RemoteException, ITMException;
}