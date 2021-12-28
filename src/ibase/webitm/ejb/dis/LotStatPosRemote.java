/**
 * Author : Chaitali Parab
 * Date   : 25/08/2011
 * 
 * */
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;

import org.w3c.dom.*;

import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
@javax.ejb.Remote

public interface LotStatPosRemote extends ValidatorRemote
{
	public String postSave()throws RemoteException,ITMException;
	public String postSave( String domString, String tranId,String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException;
}
