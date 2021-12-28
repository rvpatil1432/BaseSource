
package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3

//public interface ChargeBackRemote extends EJBObject // commented for ejb3
@Remote // added for ejb3
public interface ChargeBackRemote //extends ValidatorRemote // added for ejb3
{
	public String replaceVal(String xmlString,String targetField,String formNo,String xtraParams)throws RemoteException,ITMException;
}
