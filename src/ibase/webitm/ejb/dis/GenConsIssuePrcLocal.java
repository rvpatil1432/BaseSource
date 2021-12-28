package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ProcessLocal;
import ibase.webitm.utility.ITMException;
import org.w3c.dom.*;
import java.rmi.RemoteException;

import javax.ejb.Local;
@Local // added for ejb3

public interface GenConsIssuePrcLocal extends ProcessLocal//,EJBObject
{
	public String process() throws RemoteException,ITMException;
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException;
	
}
