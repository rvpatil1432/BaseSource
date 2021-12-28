/**
 * VALLABH KADAM 
 * ChargeBackLocCancelLocal.java
 * Req Id:-[D15BSUN003]
 * */

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
	
public interface ChargeBackLocCancelLocal extends ActionHandlerLocal
{
	public String cancel(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;

}
