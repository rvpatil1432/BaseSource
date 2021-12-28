/* This file is Cteated it incorporate the Pre Save logic from Window
 * Gulzar 11/09/06
 */

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;
import java.sql.Connection;
import javax.ejb.Remote;


@Remote
public interface SRetPostSaveRemote extends ValidatorRemote
{
	public String postSaveRec()throws RemoteException,ITMException;
	public String postSave(String winName,String editFlag,String tranId,  String xtraParams,Connection conn) throws RemoteException,ITMException;
	public String postSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}