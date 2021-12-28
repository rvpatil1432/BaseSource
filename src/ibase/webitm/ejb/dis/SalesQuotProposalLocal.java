
/********************************************************
	Title : PlaceOrdWizICLocal [D16BSUP001]
	Date  : 25/05/16
	Author: Poonam Gole

********************************************************/
package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import org.w3c.dom.*;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local; 

@Local // added for ejb3
public interface SalesQuotProposalLocal extends ValidatorLocal
{
	public String wfValData() throws RemoteException,ITMException;
	public String wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams, String formName) throws RemoteException,ITMException;
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams, String formName) throws RemoteException,ITMException;
	public String itemChanged() throws RemoteException,ITMException;
	public String itemChanged(String xmlString, String xmlString1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams, String formName) throws RemoteException,ITMException;
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams, String formName) throws RemoteException,ITMException;
	public String approvePriceRate(String quotNo, String userInfo, String xmlData,String effDate, String validUpto);
	
}