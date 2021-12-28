package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import javax.ejb.Local;

import org.w3c.dom.Document;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;

@Local
public interface CustomerDefaultLocal extends ValidatorLocal 
{
	public String wfValData(String xmlString, String xmlStringHdr, String xmlStringAll, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String wfValData(Document dom, Document domHdr, Document domAll, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String itemChanged(String xmlString, String xmlStringHdr, String xmlStringAll, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException;
}
