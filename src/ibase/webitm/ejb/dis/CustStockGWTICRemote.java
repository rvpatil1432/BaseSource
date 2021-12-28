package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
import java.util.HashMap;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;
import javax.xml.parsers.*;

import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3
@Remote // added for ejb3

public interface CustStockGWTICRemote extends ValidatorRemote//, EJBObject
{
	public String wfValData() throws RemoteException,ITMException;
	public String wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String wfValData(Document dom, Document dom1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String itemChanged() throws RemoteException,ITMException;
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String itemChanged(String xmlString, String xmlString1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException;
	//Method override for external transaction generation process Added by saurabh[27/03/17|Start]
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams,String objName) throws RemoteException,ITMException;
	//Method override for external transaction generation process Added by saurabh[27/03/17|End]
	//Method override for external transaction generation process for AWACS Added by saurabh[24/07/17|Start]
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams,String objName,HashMap dataMap) throws RemoteException,ITMException;
	//Method override for external transaction generation process for AWACS Added by saurabh[24/07/17|End]
	public String itemChanged(Document dom, Document dom1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
}