package ibase.webitm.ejb.dis;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;

import java.sql.Connection;
import java.rmi.RemoteException;
import javax.ejb.Remote; 

@Remote
public interface DespatchConfirmRemote extends ActionHandlerRemote 
{
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
	
	public String confirm( String tranId, String xtraParams,String forcedFlag, Connection conn) throws RemoteException,ITMException;
	public String confirm( String tranId, String xtraParams,String forcedFlag, Connection conn,Connection connCP) throws RemoteException,ITMException;
}
