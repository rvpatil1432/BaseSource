package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;

import javax.ejb.Local;

@Local
public interface PriceListMasterWfEJBLocal extends ValidatorLocal
{
	public String insertPriceListMaster(String objName,String tranId,String xmlDataAll,String xtraParams,String entityCode) throws RemoteException, ITMException;
}
