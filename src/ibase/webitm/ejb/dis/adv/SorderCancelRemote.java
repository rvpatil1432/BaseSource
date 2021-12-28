/********************************************************
	Title 	 : SorderCancelRemote
	Date  	 : 26/SEP/16
	Developer: Nasruddin Khan
 ********************************************************/


package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import java.rmi.RemoteException;
import javax.ejb.Remote; 
@Remote 				   
public interface SorderCancelRemote extends ActionHandlerRemote
{
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}

