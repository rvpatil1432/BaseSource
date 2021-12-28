package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;

import javax.ejb.Local;

import org.w3c.dom.Document;

@Local//Changed BY Nasruddin 26-sep-16 extends ValidatorLocal
public interface SupplierPosFormLocal extends ValidatorLocal
{
	public String postSaveForm() throws RemoteException,ITMException;
	public String postSaveForm(String arg1,String arg2,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
	public String executePostSaveForm(Document headerDom, Document detailDom,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}
