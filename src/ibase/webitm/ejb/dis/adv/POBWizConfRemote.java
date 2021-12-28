/********************************************************
	Title : POBWizConfRemote[D15ESUN017]
	Date  : 22/09/15
	Developer: Chandrashekar

 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3
public interface POBWizConfRemote extends ActionHandlerRemote
{
		public String pobConfirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
		public String pobConfirm(String tranId, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException,ITMException;
		//public String submit(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
		public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException;
}
