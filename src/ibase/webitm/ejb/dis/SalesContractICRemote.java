/********************************************************
	Title : SalesContractICRemote
	Date  : 03/05/2012
	Developer: Mahesh Patidar
 ********************************************************/

package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Remote;
import org.w3c.dom.Document;

@Remote// Changed By Nasruddin 26-SEP-16 extends ValidatorRemote interface
public interface SalesContractICRemote extends ValidatorRemote
{
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;

	public String itemChanged(String xmlString, String xmlString1, String xmlString2,  String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
}
