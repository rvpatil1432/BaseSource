/********************************************************
	Title : POBWizConfLocal[D15ESUN017]
	Date  : 22/09/15
	Developer: Chandrashekar

 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerLocal;
import javax.ejb.Local; // added for ejb3

@Local // added for ejb3
public interface POBWizConfLocal extends ActionHandlerLocal
{
   public String pobConfirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
   public String pobConfirm(String tranId, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException,ITMException;
   //public String submit(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
   public String confirm(String tranId, String xtraParams,String forcedFlag) throws RemoteException, ITMException;

}
