/**
	* @author : Akhilesh Sikarwar 
	* @Version : 1.0
	* Date : 03/10/12
*/
package ibase.webitm.ejb.dis;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
import java.sql.*;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;


import javax.xml.parsers.*;
import javax.ejb.Remote; // added for ejb3
@Remote // added for ejb3
				   
public interface BillofQuantityConfRemote extends ActionHandlerRemote//, EJBObject
{
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
