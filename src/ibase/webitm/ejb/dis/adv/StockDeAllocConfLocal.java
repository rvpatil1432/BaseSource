/********************************************************
	Title : StockDeAllocConfLocal[W14CSUN004]
	Date  : 10/06/2014
	Developer: Chandrashekar

********************************************************/

package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
import java.sql.Connection;

import javax.ejb.Local; // added for ejb3
@Local 
	
public interface StockDeAllocConfLocal extends ActionHandlerLocal//,EJBObject 
{
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
	public String confirm( String tranId, String xtraParams,String forcedFlag, Connection conn) throws RemoteException,ITMException;
}
