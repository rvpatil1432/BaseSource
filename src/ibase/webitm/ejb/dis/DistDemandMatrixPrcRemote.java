package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import  ibase.webitm.ejb.ProcessRemote;
//import ibase.webitm.ejb.Process;
import javax.ejb.Remote; // added for ejb3
import ibase.webitm.ejb.ValidatorRemote;// added for ejb3

@Remote // added for ejb3
public interface DistDemandMatrixPrcRemote extends ProcessRemote
{
	public String process() throws RemoteException,ITMException;
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String getData(Document dom, Document dom2, String windowNamem, String xtraParams) throws RemoteException,ITMException;
}