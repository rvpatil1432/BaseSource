/********************************************************
	Title : SiteItemUpdatePrcLocal[D14ISUN010]
	Date  : 09/03/15
	Developer: Chandrashekar

 ********************************************************/
package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.*;
import javax.xml.parsers.*;
import  ibase.webitm.ejb.ProcessLocal;
import javax.ejb.Local; // added for ejb3
import ibase.webitm.utility.ITMException;
@Local // added for ejb3`	
public interface SiteItemUpdatePrcLocal extends ProcessLocal
{
	public String process() throws RemoteException,ITMException;
	public String process(String string1, String string2, String windowName, String xtraParams) throws RemoteException,ITMException;
	
}