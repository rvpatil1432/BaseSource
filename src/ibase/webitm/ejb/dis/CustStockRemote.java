package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;
import javax.xml.parsers.*;

import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3
@Remote // added for ejb3
public interface CustStockRemote extends ValidatorRemote//, EJBObject
{
	public String wfValData() throws RemoteException,ITMException;
	public String wfValData(Document dom, String formNo, String editFlag) throws RemoteException,ITMException;
	public String wfValData(String xmlString, String formNo, String editFlag) throws RemoteException,ITMException;
	public String itemChanged() throws RemoteException,ITMException;
	/*//Changed by Danish on 06/02/2007
	public String itemChanged(String xmlString , String currentColumn, String formNo, String domID) throws RemoteException,ITMException;
	public String itemChanged(Document dom , String currentColumn, String formNo, String domID) throws RemoteException,ITMException;*/
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
}