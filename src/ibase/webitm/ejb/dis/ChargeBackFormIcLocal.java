package ibase.webitm.ejb.dis;
import ibase.webitm.ejb.*;
import java.rmi.RemoteException;
import ibase.webitm.utility.ITMException;
import org.w3c.dom.*;
import  ibase.webitm.ejb.ValidatorLocal;
//import javax.ejb.CreateException;
//import javax.ejb.EJBHome;
import javax.ejb.Local; // added for ejb3

@Local // added for ejb3
public interface ChargeBackFormIcLocal extends ValidatorLocal
{	
	//public Validator create() throws RemoteException, CreateException;
	public String wfValData() throws RemoteException,ITMException;		
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException;
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
//	public String itemChanged() throws RemoteException,ITMException;		
//	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;		
//	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
}