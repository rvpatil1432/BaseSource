/*	
		Developed by	: Hatim Laxmidhar
		Started On		: 23/12/2005
*/


package ibase.webitm.ejb.dis;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject; // commented for ejb3
import org.w3c.dom.*;
import javax.ejb.Local; // added for ejb3

//public interface ConsumeIssue extends ValidatorLocal , EJBObject // commented for ejb3
@Local // added for ejb3
public interface ConsumeIssueLocal extends ValidatorLocal // added for ejb3
{
	public String wfValData() throws RemoteException,ITMException;
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
}