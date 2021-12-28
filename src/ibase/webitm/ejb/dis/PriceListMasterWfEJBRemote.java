package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Remote;

@Remote
public interface PriceListMasterWfEJBRemote extends ValidatorRemote
{
	public String insertPriceListMaster(String objName,String tranId,String xmlDataAll,String xtraParams,String entityCode) throws RemoteException, ITMException;
}
