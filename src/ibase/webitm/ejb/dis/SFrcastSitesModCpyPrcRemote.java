package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import javax.xml.parsers.*;
//import  ibase.webitm.ejb.Process;
import ibase.webitm.utility.ITMException;
import  ibase.webitm.ejb.ProcessRemote;
import javax.ejb.Remote; // added for ejb3
import ibase.webitm.ejb.ValidatorRemote;// added for ejb3

@Remote // added for ejb3
public interface SFrcastSitesModCpyPrcRemote extends ProcessRemote
{
	public String process() throws RemoteException,ITMException;
	public String process(String string1, String string2, String windowName, String xtraParams) throws RemoteException,ITMException;
	
}