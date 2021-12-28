package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.util.HashMap;

import javax.ejb.Local;

import org.w3c.dom.Document;

@Local
public interface StockAllocVerifyWizLocal extends ValidatorLocal {

	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext,String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String wfValData(Document currFormDataDom, Document hdrDataDom, Document allFormDataDom, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String handleRequest(HashMap<String, String> reqParamMap);
	
}
