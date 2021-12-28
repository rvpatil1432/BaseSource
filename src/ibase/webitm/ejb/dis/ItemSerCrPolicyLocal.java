package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import ibase.webitm.utility.ITMException;
import org.w3c.dom.Document;

import ibase.webitm.ejb.ProcessLocal;
import ibase.webitm.ejb.ValidatorLocal;

import javax.ejb.Local;

@Local
//Changed by Santosh on 18-07-16
//public interface ItemSerCrPolicyLocal  {
public interface ItemSerCrPolicyLocal extends ValidatorLocal {
    /*public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException, ITMException;

    public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException, ITMException;*/
	
	public String wfValData() throws RemoteException, ITMException ;
	public String wfValData(Document dom, Document dom1, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException ;
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag) throws RemoteException, ITMException ;
	public String itemChanged() throws RemoteException, ITMException ;
	public String itemChanged(Document dom, Document dom1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag) throws RemoteException, ITMException ;
}