/********************************************************
	Title : StationAmdConfRemote
	Date  : 03/05/12
	Developer: Kunal Mandhre

 ********************************************************/
package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; 
@Remote 
public interface StationAmdConfRemote extends ActionHandlerRemote
{
	public String confirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
