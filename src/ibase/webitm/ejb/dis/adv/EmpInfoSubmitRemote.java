/**
 DEVELOPED BY RITESH TIWARI ON 10/04/14 
 PURPOSE: WS3LSUN003 (StarClub Employee details.)
 */
package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import java.rmi.RemoteException;
import java.sql.Connection;

@javax.ejb.Remote
public interface EmpInfoSubmitRemote extends ActionHandlerRemote
{
	public String confirm( String tranId, String xtraParams, String forcedFlag ) throws RemoteException,ITMException;
	//added by chitranjan for call confirm method
	public String confirm( String tranId, String xtraParams,String forcedFlag, Connection conn, boolean connStatus) throws RemoteException,ITMException;
}
