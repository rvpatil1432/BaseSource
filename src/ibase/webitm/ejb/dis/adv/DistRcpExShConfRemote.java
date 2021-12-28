/********************************************************
	Title 	 : DistRcpExShConfRemote
	Date  	 : 13/MAR/15
	Developer: Pankaj R.
 ********************************************************/


package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
import java.sql.*;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;


import javax.xml.parsers.*;
import javax.ejb.Remote; // added for ejb3
@Remote // added for ejb3
				   
public interface DistRcpExShConfRemote extends ActionHandlerRemote//, EJBObject
{
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}

