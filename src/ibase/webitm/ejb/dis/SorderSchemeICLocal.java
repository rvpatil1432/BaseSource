/*
 * Author:Wasim Ansari
 * Date: 12-12-2016
 * Request ID:S16EBAS006 (Zero rate sale order IC)
 */
package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Local;
import org.w3c.dom.Document;

@Local
public interface SorderSchemeICLocal extends ValidatorLocal 
{
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;		
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
}
