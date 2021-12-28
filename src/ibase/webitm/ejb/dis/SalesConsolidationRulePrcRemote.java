package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import ibase.webitm.ejb.ProcessRemote;
import ibase.webitm.utility.ITMException;

import javax.ejb.Remote;

import org.w3c.dom.Document;

@Remote
public interface SalesConsolidationRulePrcRemote extends ProcessRemote{

	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;

}
