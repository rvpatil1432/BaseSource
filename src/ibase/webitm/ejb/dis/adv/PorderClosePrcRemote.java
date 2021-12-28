package ibase.webitm.ejb.dis.adv;


import java.rmi.RemoteException;
import java.sql.Connection;

import ibase.webitm.utility.ITMException;

import ibase.webitm.ejb.ProcessRemote;
import javax.ejb.Remote;

import org.w3c.dom.Document;

@Remote // added for ejb3`	
public interface PorderClosePrcRemote extends ProcessRemote
{
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
public String getdata(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException;
public String process() throws RemoteException,ITMException;
public String process(String string1, String string2, String windowName, String xtraParams) throws RemoteException,ITMException;
public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException;
public String saveData(String siteCode, String xmlString,String userId, Connection conn)  throws RemoteException,ITMException;	
}
