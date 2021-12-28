package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;
import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;
import javax.ejb.Local; // added for ejb3
import ibase.webitm.utility.ITMException;
@Local // added for ejb3
//public interface ChargeBackVerify extends ActionHandlerLocal,EJBObject 
public interface ChargeBackVerifyLocal extends ActionHandlerLocal // added for ejb3 
{
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
