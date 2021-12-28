package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;

import javax.ejb.Remote;

import org.w3c.dom.Document;

@Remote//Changed BY Nasruddin 26-sep-16 extends ValidatorRemote
public interface SupplierPosFormRemote extends ValidatorRemote
{
	public String postSaveForm() throws RemoteException,ITMException;
	public String postSaveForm(String arg1,String arg2,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
	public String executePostSaveForm(Document headerDom, Document detailDom,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}
