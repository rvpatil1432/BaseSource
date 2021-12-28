package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import ibase.webitm.ejb.ProcessLocal;
import ibase.webitm.utility.ITMException;

import javax.ejb.Local;

import org.w3c.dom.Document;

@Local
public interface SalesConsolidationPrcLocal extends ProcessLocal {
	
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;

}
