package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;

import javax.ejb.Local;

import org.w3c.dom.Document;

@Local
public interface SalesConsolidateICLocal extends ValidatorLocal{

	public String wfValData(String currXmlDataStr, String hdrXmlDataStr, String allXmlDataStr, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String wfValData(Document currDom, Document hdrDom, Document allDom, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String itemChanged(Document dom, Document dom1, Document dom2,String objContext, String currentColumn, String editFlag,String xtraParams) throws RemoteException, ITMException;
}
