package ibase.webitm.ejb.dis;
import ibase.webitm.ejb.*;
import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3

@Local // added for ejb3
public interface TaxFormRecoMultiIcLocal extends ValidatorLocal//,EJBObject
{
	public String wfValData() throws RemoteException,ITMException;
	
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String itemChanged() throws RemoteException,ITMException;
	
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
}
