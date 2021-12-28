package ibase.webitm.ejb.dis;
import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import java.sql.Connection;

import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3

import org.w3c.dom.Document;

@Local // added for ejb3

public interface DistIssDelWizPostSaveLocal extends ValidatorLocal//, EJBObject
{
	public String postSave(String xmlString,String tranId,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException;
	public String postSave(Document dom,String tranId,String editflag,String xtraParams,Connection conn);

}