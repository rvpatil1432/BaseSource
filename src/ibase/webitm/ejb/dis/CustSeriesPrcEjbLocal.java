package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import org.w3c.dom.*;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;

import javax.ejb.Local;
import javax.ejb.Remote; // added for ejb3

@Local // added for ejb3

public interface CustSeriesPrcEjbLocal extends ProcessLocal
{
	//public String process();
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException;
	
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
	
	//public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
	
	//public String getData(Document dom, Document dom2, String windowNamem, String xtraParams) throws RemoteException,ITMException;
}

