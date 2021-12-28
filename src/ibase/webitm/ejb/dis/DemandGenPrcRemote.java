package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; 

@Remote
public interface DemandGenPrcRemote extends ibase.webitm.ejb.ProcessRemote
{
	public String process() throws RemoteException,ITMException;
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;

}
