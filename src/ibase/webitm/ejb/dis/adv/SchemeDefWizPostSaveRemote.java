/*
 * Author:Varsha V
 * Date:21-02-2019
 * Request ID:D18JMES001 (Free Offer on multiple Products Wizard)
 */
package ibase.webitm.ejb.dis.adv;

import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import javax.ejb.Remote;

@Remote

public interface SchemeDefWizPostSaveRemote extends ValidatorRemote
{
	public String postSave(String xmlStringAll, String tranId, String editFlag, String xtraParams, Connection conn) throws RemoteException,ITMException;
}
