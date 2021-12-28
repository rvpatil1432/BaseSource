package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import org.w3c.dom.*;

import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote;

@Remote 
public interface DespatchWizRemote extends ValidatorRemote
{
	public String wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String wfValData(Document dom, Document dom1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String itemChanged(String xmlString, String xmlString1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;

	public String itemChanged(Document dom, Document dom1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
			

}