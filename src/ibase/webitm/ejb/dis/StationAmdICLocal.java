/********************************************************
	Title : StationAmdICLocal
	Date  : 30/04/12
	Developer: Kunal Mandhre

 ********************************************************/

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import org.w3c.dom.*;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local;
@Local
public interface StationAmdICLocal extends ValidatorLocal
{
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
}
