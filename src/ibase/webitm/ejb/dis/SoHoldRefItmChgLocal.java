package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import org.w3c.dom.*;

//import javax.ejb.EJBObject;
import java.sql.*;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3


//public interface SoHoldRefItmChg extends ValidatorLocal,EJBObject
@Local // added for ejb3
public interface SoHoldRefItmChgLocal extends ValidatorLocal
{
	public String wfValData() throws RemoteException,ITMException;
	
	public String wfValData(String xmlString, String xmlString1,String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String wfValData(Document dom, Document dom1,Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String itemChanged() throws RemoteException,ITMException;
		
}
