/**
 * PURPOSE : Remote Interface for IndentAmendConf component
 * AUTHOR : Manish Mhatre
 * DATE : 12-04-2021
 */

package ibase.webitm.ejb.dis.adv;

import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;

@javax.ejb.Remote
public interface IndentAmendConfRemote extends ActionHandlerRemote
{
	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException;

	public String confirm( String tranId, String xtraParams, String forcedFlag, Connection conn) throws RemoteException,ITMException;

}
