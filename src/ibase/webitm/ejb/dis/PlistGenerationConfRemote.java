package ibase.webitm.ejb.dis;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;

import java.sql.Connection;
import java.rmi.RemoteException;
import javax.ejb.Remote; 

@Remote
public interface PlistGenerationConfRemote extends ActionHandlerRemote 
{
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
	//new method by gulzar - 25/11/11 to call from postsave component
	public String confirm( String tranId, String xtraParams,String forcedFlag, Connection conn, boolean connStatus ) throws RemoteException,ITMException;
}
