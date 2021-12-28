package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import org.w3c.dom.Document;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

public class DistRcpTranDateIC extends ValidatorEJB {
	 E12GenericUtility genericUtility = new E12GenericUtility();
  public String itemChanged(String xmlString, String xmlString1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
	  return "";
		}
  
  public String itemChanged(Document dom, Document dom1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
	  
	  return "";
}
  
}


