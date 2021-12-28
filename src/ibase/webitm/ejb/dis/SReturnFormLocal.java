package ibase.webitm.ejb.dis;

//import ibase.webitm.ejb.dis.DistCommon;
import java.rmi.RemoteException;
import org.w3c.dom.*;

//import javax.ejb.EJBObject;
import java.sql.Connection;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3


//public interface SOrderForm extends ValidatorLocal,EJBObject
@Local // added for ejb3//Changed BY Nasruddin 26-sep-16 extends ValidatorLocal

public interface SReturnFormLocal extends ValidatorLocal
{
	public String wfValData(String xmlString, String xmlString1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;

	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException ;
	
	public String wfValData(Document dom, Document dom1, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String itemChanged(String xmlString, String xmlString1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;

	public String itemChanged(Document dom, Document dom1, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
		
}
