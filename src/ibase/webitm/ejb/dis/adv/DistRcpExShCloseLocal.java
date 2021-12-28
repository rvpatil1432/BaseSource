/********************************************************
	Title 	 : DistRcpExShConfLocal
	Date  	 : 13/MAY/15
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
import javax.ejb.Local; // added for ejb3
@Local 
	
public interface DistRcpExShCloseLocal extends ActionHandlerLocal//,EJBObject 
{
	public String close(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}