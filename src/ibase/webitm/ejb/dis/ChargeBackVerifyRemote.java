package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;
import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;
import javax.ejb.Remote; // added for ejb3
import ibase.webitm.utility.ITMException;

//public interface ChargeBackVerify extends ActionHandlerRemote,EJBObject 
@Remote // added for ejb3
public interface ChargeBackVerifyRemote extends ActionHandlerRemote // added for ejb3 
{
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException;
}
