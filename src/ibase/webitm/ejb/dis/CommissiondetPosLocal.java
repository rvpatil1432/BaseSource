/* This file is Cteated it incorporate the Pre Save logic from Window
 * Gulzar 08/09/06
 */

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;
import java.sql.Connection;
import javax.ejb.Local; // added for ejb3


//public interface SReturnPrs extends ValidatorLocal, EJBObject
@Local // added for ejb3
public interface CommissiondetPosLocal extends ValidatorLocal
{
	//public String preSaveRec()throws RemoteException,ITMException;
	//public String preSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
	public String postSave()throws RemoteException,ITMException;
	//public String postSave( String domString,String tranId, String objContext, String editFlag, String xtraParams ) throws RemoteException,ITMException;
	public String postSave( String domString,String tranId, String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException;
}
