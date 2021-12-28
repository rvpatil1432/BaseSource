/********************************************************
	Title : POBWizICRemote[D15ESUN017]
	Date  : 10/09/15
	Developer: Chandrashekar

 ********************************************************/
package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3
import ibase.webitm.ejb.ValidatorRemote;// added for ejb3

@Remote // added for ejb3
public interface PrdApplCountrySerICRemote extends ValidatorRemote
{	
	
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;	
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;		
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
}