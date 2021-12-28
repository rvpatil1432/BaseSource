/* This file is Cteated it incorporate the Pre Save logic from Window
 * Gulzar 11/09/06
 */

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;
import java.sql.Connection;
import javax.ejb.Remote; // added for ejb3


//public interface SReturnPos extends ValidatorRemote, EJBObject
@Remote // added for ejb3
public interface SReturnPosRemote extends ValidatorRemote
{
	public String postSaveRec()throws RemoteException,ITMException;
	public String postSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}