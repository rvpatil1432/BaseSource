/**
 DEVELOPED BY CHANDRASHEKAR ON 14/05/14 
 PURPOSE: W14BSUN003 (StarClub Employee details.)
 */
package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import java.rmi.RemoteException;
import java.sql.Connection;

@javax.ejb.Local
public interface EmpTravelInfoCancelLocal extends ActionHandlerLocal
{
	public String confirm( String tranId, String xtraParams, String forcedFlag )throws RemoteException,ITMException;
	public String confirm( String tranId, String xtraParams,String forcedFlag, Connection conn, boolean connStatus) throws RemoteException,ITMException;
}
