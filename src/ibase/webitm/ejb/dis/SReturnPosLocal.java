/* This file is Cteated it incorporate the Pre Save logic from Window
 * Gulzar 11/09/06
 */

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;
import java.sql.Connection;
import javax.ejb.Local; // added for ejb3


//public interface SReturnPos extends ValidatorLocal, EJBObject
@Local // added for ejb3
public interface SReturnPosLocal extends ValidatorLocal
{
	public String postSaveRec()throws RemoteException,ITMException;
	public String postSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}