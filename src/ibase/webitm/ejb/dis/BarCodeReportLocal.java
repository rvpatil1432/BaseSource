package ibase.webitm.ejb.dis;

import java.rmi.RemoteException; 
import org.w3c.dom.*;
//import javax.ejb.EJBObject; //commented for ejb3
import javax.ejb.Local; //added for ejb3
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
@Local // added for ejb3
//public interface BarCodeReport extends ValidatorLocal,EJBObject //commented for ejb3
public interface BarCodeReportLocal extends ValidatorLocal //added for ejb3
{
	public String wfValData() throws RemoteException,ITMException;
	
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String itemChanged() throws RemoteException,ITMException;
	
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
}
