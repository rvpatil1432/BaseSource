/**
* PURPOSE : Local Interface for IndentConf component
* AUTHOR : Sneha Mestry
* DATE : 04-01-2017
*/

package ibase.webitm.ejb.dis;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;

import org.w3c.dom.*;
import javax.ejb.*;


@javax.ejb.Local
public interface IndentConfLocal extends ActionHandlerLocal
{
	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException;
	
	public String confirm( String tranId, String xtraParams, String forcedFlag, Connection conn) throws RemoteException,ITMException;
	
	//public String postSave()throws RemoteException,ITMException;
	
	//public String postSave( String domString, String tranId, String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException;	

}
