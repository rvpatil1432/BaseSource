/* This file is Cteated it incorporate the Pre Save logic from Window
 * Gulzar 11/09/06
 */

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;
import java.sql.Connection;
import javax.ejb.Local;


@Local 
public interface SRetPostSaveLocal extends ValidatorLocal
{
	public String postSaveRec()throws RemoteException,ITMException;
	public String postSave(String winName,String editFlag,String tranId,  String xtraParams,Connection conn) throws RemoteException,ITMException;
	public String postSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}