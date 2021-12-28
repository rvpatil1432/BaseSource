package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
@javax.ejb.Local

public interface SalePersonTransCompleteLocal extends ValidatorLocal
{
	public String transComplete (String xmlString, String xmlString1, String xmlString2, String objContext, String winName, String xtraParams,Connection conn) throws RemoteException ,ITMException;
}
