package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;
import javax.xml.parsers.*;

import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;

import javax.ejb.Remote; // added for ejb3


//public interface ProfAccounts extends ValidatorRemote, EJBObject
@Remote // added for ejb3
public interface ProfAccountsRemote extends ValidatorRemote
{
	public String wfValData() throws RemoteException,ITMException;
	public String wfValData(Document dom, Document dom1,Document dom2 , String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;

}