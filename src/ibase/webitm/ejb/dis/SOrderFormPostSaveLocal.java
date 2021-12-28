package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import org.w3c.dom.*;
import java.sql.Connection;
import ibase.webitm.ejb.ValidatorLocal;

//import javax.ejb.EJBObject;

import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3


//public interface SOrderFormPostSave extends ValidatorLocal,EJBObject
@Local // added for ejb3
public interface SOrderFormPostSaveLocal extends ValidatorLocal
{
	public String postSave() throws RemoteException,ITMException;
	
	public String postSave(String winName,String editFlag,String xmlString,  String xtraParams,Connection conn) throws RemoteException,ITMException;
	
	public String postSave(String winName,String editFlag,Document dom,  String xtraParams,Connection conn) throws RemoteException,ITMException;
	
}
