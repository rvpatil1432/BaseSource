package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import org.w3c.dom.Document;

import ibase.webitm.utility.ITMException;

public interface DistRcpTranDatePrcLocal extends ibase.webitm.ejb.ProcessLocal{

	//public String process(Document dom, String windowName, String xtraParams) throws RemoteException,ITMException;
	//public String process(String xmlString,String windowName, String xtraParams) throws RemoteException,ITMException;
	public String process() throws RemoteException,ITMException;
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;

	
}
