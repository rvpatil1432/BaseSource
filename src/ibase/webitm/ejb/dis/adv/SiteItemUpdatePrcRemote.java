/********************************************************
	Title : SiteItemUpdatePrcRemote[D14ISUN010]
	Date  : 09/03/15
	Developer: Chandrashekar

 ********************************************************/
package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import ibase.webitm.utility.ITMException;
import  ibase.webitm.ejb.ProcessRemote;
import javax.ejb.Remote; // added for ejb3
import ibase.webitm.ejb.ValidatorRemote;// added for ejb3

@Remote // added for ejb3

public interface SiteItemUpdatePrcRemote extends ProcessRemote
{
	public String process() throws RemoteException,ITMException;
	public String process(String string1, String string2, String windowName, String xtraParams) throws RemoteException,ITMException;
}