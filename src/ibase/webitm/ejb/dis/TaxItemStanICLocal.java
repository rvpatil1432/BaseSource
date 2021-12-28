package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;

import org.w3c.dom.*;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3
//public interface SOrderForm extends ValidatorLocal,EJBObject
@Local // added for ejb3

public interface TaxItemStanICLocal extends ValidatorLocal
{
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String wfValData(Document dom, Document dom1, Document dom2,String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;

	public String itemChanged(Document dom, Document dom1, Document dom2,String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String preSaveRec()throws RemoteException,ITMException;
	public String preSaveRec(String xmlString,String xmlString1,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}
