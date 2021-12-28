/**
 DEVELOPED BY RITESH TIWARI ON 27/03/14 
 PURPOSE: WS3LSUN003 (StarClub Employee details.)
 */
package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import org.w3c.dom.*;
//import javax.ejb.EJBObject;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import javax.ejb.Local; // added for ejb3

@Local // added for ejb3

public interface EmpTravelInfoICLocal extends  ValidatorLocal	//, EJBObject
{	
	public String wfValData() throws RemoteException,ITMException;		
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String itemChanged() throws RemoteException,ITMException;		
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;		
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
	//added by chandrashekar on 23-05-2014
	public String getFlightCodeList(String flightCode)throws ITMException;
}