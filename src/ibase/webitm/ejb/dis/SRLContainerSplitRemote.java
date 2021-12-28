/**
* PURPOSE : Remote Interface 
* Author : Sumit Sarkar
*/

package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import java.sql.*;
import java.rmi.RemoteException;

// The remote interface declares all the utility functions to be used in your EJB component
@javax.ejb.Remote
public interface SRLContainerSplitRemote extends ActionHandlerRemote//, EJBObject
{
	public String confirm()throws RemoteException,ITMException;
	//public String actionHandler(String actionType, String xmlString, String xmlString1, String xmlString2 ,String objContext, String xtraParams)throws RemoteException,ITMException;	
	public String confirm(String tranID,String xtraParams, String forcedFlag)throws RemoteException,ITMException;

}
