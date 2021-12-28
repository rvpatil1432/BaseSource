package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local; 
@Local 
public interface GeneratePReturnPrcLocal extends ibase.webitm.ejb.ProcessLocal
{
	@Override
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException;
	@Override
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
}
