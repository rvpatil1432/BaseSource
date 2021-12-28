package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.CreateException;
//import javax.ejb.EJBHome;
//import ibase.webitm.ejb.ProcessHome;
//import  ibase.webitm.ejb.Process;
import org.w3c.dom.*;
import  ibase.webitm.ejb.ProcessLocal;
import javax.ejb.Local; // added for ejb3
import ibase.webitm.utility.ITMException;

@Local // added for ejb3`	
public interface DistDemandMatrixPrcLocal extends ProcessLocal
{
	//public ibase.webitm.ejb.Process create() throws RemoteException, CreateException;
	public String process() throws RemoteException,ITMException;
	public String process(String string1, String string2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String getData(Document dom, Document dom2, String windowNamem, String xtraParams) throws RemoteException,ITMException;
}