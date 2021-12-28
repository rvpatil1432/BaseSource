/*
Title : DistIssPrcRemote
Date  : 07/03/11
Developer: Chandni Shah
*/


package  ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ProcessLocal;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3

public interface DistIssPrcRemote extends ibase.webitm.ejb.ProcessLocal//, EJBObject
{
	public String process() throws RemoteException,ITMException;
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;

}