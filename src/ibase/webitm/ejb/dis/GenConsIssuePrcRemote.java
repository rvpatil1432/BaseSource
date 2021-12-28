package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ProcessRemote;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Remote;

import org.w3c.dom.Document;
@Remote // added for ejb3

public interface GenConsIssuePrcRemote extends ProcessRemote//,EJBObject
{
	public String process() throws RemoteException,ITMException;
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException;
}
