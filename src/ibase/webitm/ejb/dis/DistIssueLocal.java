package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;

import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3


//public interface DistIssue extends ValidatorLocal,EJBObject
@Local // added for ejb3
public interface DistIssueLocal extends ValidatorLocal
{
	public String itemChanged() throws RemoteException,ITMException;
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
}