package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ProcessRemote;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Remote;

import org.w3c.dom.Document;

@Remote
public interface PoReceiptVerifyPasswordPrcRemote extends ProcessRemote
{
	public String process() throws RemoteException,ITMException;
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException, ITMException;
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException, ITMException;
}
