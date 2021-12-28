package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import org.w3c.dom.*;
import java.sql.Connection;
import ibase.webitm.ejb.ValidatorRemote;

//import javax.ejb.EJBObject;

import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3


//public interface SOrderFormPostSave extends ValidatorRemote,EJBObject
@Remote // added for ejb3
public interface SOrderFormPostSaveRemote extends ValidatorRemote
{
	public String postSave() throws RemoteException,ITMException;
	
	public String postSave(String winName,String editFlag,String xmlString,  String xtraParams,Connection conn) throws RemoteException,ITMException;
	
	public String postSave(String winName,String editFlag,Document dom,  String xtraParams,Connection conn) throws RemoteException,ITMException;
	
}
