/* This file is Cteated it incorporate the Post Save logic from Window (w_lc_det)
 * Gulzar 08/02/07
 */

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;
import java.sql.Connection;
import javax.ejb.Local; // added for ejb3


//public interface LCDetPos extends ValidatorLocal, EJBObject
@Local // added for ejb3
public interface LCDetPosLocal extends ValidatorLocal

{
	public String postSaveRec()throws RemoteException,ITMException;
	public String postSave(String xmlString1, String editFlag, String xtraParams, Connection con) throws RemoteException, ITMException;
	public String postSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}