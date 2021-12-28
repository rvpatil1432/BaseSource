package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import javax.ejb.Remote;

import org.w3c.dom.Document;

@Remote

public interface StockTransferEJBRemote extends ValidatorRemote
{
	public String wfValData() throws RemoteException,ITMException;
	
	public String wfValData(Document currDom, Document hdrDom, Document allDom, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String wfValData(String currFrmXmlStr, String hdrFrmXmlStr, String allFrmXmlStr, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String itemChanged() throws RemoteException,ITMException;
	
	public String itemChanged(String currFrmXmlStr, String hdrFrmXmlStr,String allFrmXmlStr, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String itemChanged(Document currDom, Document hdrDom, Document allDom, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;

}
