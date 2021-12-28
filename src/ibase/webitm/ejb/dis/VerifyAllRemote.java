package ibase.webitm.ejb.dis;

import ibase.scheduler.utility.interfaces.Schedule;
import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.ejb.*;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3
public interface VerifyAllRemote	//extends //EJBObject 
{
	public String verifyAll(String tranId);
	public String verify(String tranId) throws RemoteException;
}
