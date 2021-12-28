/*******************************************    
	Title : BillofQuatationActRemote
    Date  : 20/09/12
    Author: Kunal Mandhre

 ********************************************************/
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerRemote;
import javax.ejb.Remote; 
@Remote 
public interface BillofQuantityActRemote extends ActionHandlerRemote 
{
	@Override
	public String actionHandler() throws RemoteException,ITMException;
	@Override
	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException;
	@Override
	public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException;
}