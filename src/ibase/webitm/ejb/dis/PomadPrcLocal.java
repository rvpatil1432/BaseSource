package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3


@Local // added for ejb3
public interface PomadPrcLocal extends ibase.webitm.ejb.ProcessLocal
{
	@Override
	public String process() throws RemoteException,ITMException;
	@Override
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException;
	@Override
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
	@Override
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
	@Override
	public String getData(Document dom, Document dom2, String windowNamem, String xtraParams) throws RemoteException,ITMException;
}