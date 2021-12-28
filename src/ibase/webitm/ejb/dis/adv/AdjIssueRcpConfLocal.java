/********************************************************
	Title : AdjIssueRcpConfLocal[D16ASUN021]
	Date  : 09/05/16
	Developer: Chandrashekar

 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerLocal;
import javax.ejb.Local; // added for ejb3

@Local // added for ejb3
public interface AdjIssueRcpConfLocal extends ActionHandlerLocal
{
   public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;

}
