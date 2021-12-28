package ibase.webitm.ejb.dis;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;

import java.sql.Connection;
import java.rmi.RemoteException;
import javax.ejb.Local;

@Local
public interface PriceListConfLocal extends ActionHandlerLocal
{
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
	public String confirm( String tranId, String xtraParams,String forcedFlag, Connection conn, boolean connStatus ) throws RemoteException,ITMException;
}
