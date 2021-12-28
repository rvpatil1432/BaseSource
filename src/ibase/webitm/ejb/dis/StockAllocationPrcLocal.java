package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
//import javax.ejb.EJBHome;

//import ibase.webitm.ejb.ProcessHome;
import ibase.webitm.ejb.ProcessLocal;
import javax.ejb.Local; // added for ejb3
import ibase.webitm.utility.ITMException;
import org.w3c.dom.*;

@Local // added for ejb3

//public interface StockAllocationPrcHome extends ProcessHome, EJBHome
public interface StockAllocationPrcLocal extends ibase.webitm.ejb.ProcessLocal//, EJBObject
{
	public String process() throws RemoteException,ITMException;
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String getData(Document dom, Document dom2, String windowNamem, String xtraParams) throws RemoteException,ITMException;
}