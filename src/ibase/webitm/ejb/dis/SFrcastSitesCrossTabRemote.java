package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import org.w3c.dom.*;
//import javax.ejb.EJBObject;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import javax.ejb.Remote; // added for ejb3
import ibase.webitm.ejb.ValidatorRemote;// added for ejb3

@Remote // added for ejb3
public interface SFrcastSitesCrossTabRemote extends ValidatorRemote
{	
	public String wfValData() throws RemoteException,ITMException;		
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String itemChanged() throws RemoteException,ITMException;		
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;		
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
}