package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ProcessLocal;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Local;

import org.w3c.dom.Document;

@Local
public interface SecSalesSubPrcLocal extends ProcessLocal{

	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String getData(Document dom, Document dom2, String windowNamem, String xtraParams) throws RemoteException,ITMException;
	
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
}
