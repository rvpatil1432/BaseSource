/**
 * PURPOSE : Local Interface for IndentAmendConf component
 * AUTHOR : Manish Mhatre
 * DATE : 12-04-2021
 */

package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;

import org.w3c.dom.*;
import javax.ejb.*;


@javax.ejb.Local
public interface IndentAmendConfLocal extends ActionHandlerLocal
{
	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException;

	public String confirm( String tranId, String xtraParams, String forcedFlag, Connection conn) throws RemoteException,ITMException;

}
