/***************
 * VALLABH KADAM 
 * StkAllocPostSaveLocal 
 * request id [D14JSUN005]
 * 19/JAN/15
 * ********************/

package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import java.util.ArrayList;
import java.sql.Connection;

import org.w3c.dom.*;

import ibase.webitm.utility.ITMException;

import javax.ejb.Local; //added for ejb3
@Local // added for ejb3

public interface StkAllocPostSaveLocal extends ValidatorLocal//, EJBObject
{
	public String postSave( String domString, String editFlag, String xtraParams,
			Connection conn ) throws RemoteException,ITMException;
	
}