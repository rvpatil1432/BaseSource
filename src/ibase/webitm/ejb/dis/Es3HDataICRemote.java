package ibase.webitm.ejb.dis;

import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Remote;

import org.w3c.dom.Document;

@Remote
public interface Es3HDataICRemote {
	public abstract String wfValData(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6) throws RemoteException;
	public String itemChanged(Document currDom, Document hdrDom,Document allDom, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException;
}
