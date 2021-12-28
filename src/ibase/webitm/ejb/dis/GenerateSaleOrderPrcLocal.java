
package ibase.webitm.ejb.dis;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
import javax.ejb.Local;

import org.w3c.dom.Document;

@Local
public interface GenerateSaleOrderPrcLocal extends ProcessLocal
{
	public String process() throws RemoteException,ITMException;
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
//	public String confirm(String arg0, String arg1, String arg2);


}
