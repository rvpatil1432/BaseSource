/* This file is Cteated it incorporate the Post Save logic from Window (w_lc_det)
 * Gulzar 08/02/07
 */

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;
import java.sql.Connection;
import javax.ejb.Remote; // added for ejb3


//public interface LCDetPos extends ValidatorRemote, EJBObject
@Remote // added for ejb3
public interface LCDetPosRemote extends ValidatorRemote

{
	public String postSaveRec()throws RemoteException,ITMException;
	public String postSave(String xmlString1, String editFlag, String xtraParams, Connection con) throws RemoteException, ITMException;
	public String postSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}