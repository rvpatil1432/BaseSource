package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import javax.xml.parsers.*;
import  ibase.webitm.ejb.ProcessLocal;
import ibase.webitm.utility.ITMException;
import  ibase.webitm.ejb.ProcessLocal;

import ibase.webitm.utility.ITMException;
import javax.ejb.Local; // added for ejb3

@Local // added for ejb3
public interface TaxInputCrtXmlPrcLocal extends ProcessLocal //,EJBObject
{
	public String process() throws RemoteException,ITMException;
	public String process(String string1, String string2, String windowName, String xtraParams) throws RemoteException,ITMException;
	
}