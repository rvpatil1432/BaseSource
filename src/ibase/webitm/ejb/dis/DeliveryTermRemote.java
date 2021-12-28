/********************************************************
	Title : DeliveryTermRemote
	Date  : 07/may/12
	Developer:Rakesh kumar
 ********************************************************/

package ibase.webitm.ejb.dis;


import java.rmi.RemoteException;
import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote;
import org.w3c.dom.Document;

@Remote
public interface DeliveryTermRemote extends ValidatorRemote
{
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;		
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;

}
