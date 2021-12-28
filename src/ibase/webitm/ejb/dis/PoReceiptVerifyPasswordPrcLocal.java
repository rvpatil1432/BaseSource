package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ProcessLocal;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Local;

import org.w3c.dom.Document;

@Local
public interface PoReceiptVerifyPasswordPrcLocal extends ProcessLocal
{
	public String process() throws RemoteException,ITMException;
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException, ITMException;
	public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException, ITMException;
}
