/********************************************************
	Title : StarClubHubICLocal[T16ASUN001]
	Date  : 26/04/16
	Developer: Chandrashekar

 ********************************************************/
package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import  ibase.webitm.ejb.ValidatorLocal;
import javax.ejb.Local; // added for ejb3

@Local // added for ejb3
public interface StarClubHubICLocal extends ValidatorLocal
{	
		
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String wfValData(Document dom, Document dom1,Document dom2, String objCotext, String editFlag, String xtraParams) throws RemoteException,ITMException;	
}