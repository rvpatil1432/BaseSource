/********************************************************
	Title : PreturnRefDataLocal
	Date  : 08/04/11
	Author: vpatil

********************************************************/

package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3



@Local // added for ejb3
public interface PReturnActLocal extends ActionHandlerLocal
{
	public String actionHandler(String actionType, String xmlString, String xmlString1,String objContext, String xtraParams) throws RemoteException,ITMException;
}
