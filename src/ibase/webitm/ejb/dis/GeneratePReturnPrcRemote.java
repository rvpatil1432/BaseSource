package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; 
@Remote 
public interface GeneratePReturnPrcRemote extends ibase.webitm.ejb.ProcessRemote
{
	
	@Override
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException;
	@Override
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
	
}
