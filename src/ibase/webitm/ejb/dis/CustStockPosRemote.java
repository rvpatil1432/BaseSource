package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import java.util.ArrayList;
import java.sql.Connection;
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3
@Remote // added for ejb3

public interface CustStockPosRemote extends ValidatorRemote//, EJBObject
{
	public String postSave()throws RemoteException,ITMException;
	//public String postSave(String tranId,String editFlag, String domString, String xtraParams, Connection conn) throws RemoteException,ITMException;
	public String postSave(String winName,String editFlag,String tranId, String xtraParams, Connection conn) throws RemoteException,ITMException;
	public String postSave(String tranid, String editFlag, String xtraParams, Connection conn) throws RemoteException,ITMException;
}