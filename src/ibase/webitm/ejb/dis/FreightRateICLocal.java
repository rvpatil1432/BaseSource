/********************************************************
	Title 	 : FreightRateICRemote [DI3GSUN047]
	Date  	 : 26/MAR/14
	Developer: Priyanka Shinde
 ********************************************************/
package ibase.webitm.ejb.dis;

import javax.ejb.Local;
import java.rmi.RemoteException;
import org.w3c.dom.Document;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;

@Local
public interface FreightRateICLocal extends ValidatorLocal
{
	
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;

	public String itemChanged(String xmlString, String xmlString1, String xmlString2,  String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
}
	

