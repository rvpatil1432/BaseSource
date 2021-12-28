/********************************************************
	Title : ItemLotOwnCopyPrcRemote[]
	Date  : 06/01/15
	Developer: Priyanka

 ********************************************************/
package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import ibase.webitm.utility.ITMException;
import  ibase.webitm.ejb.ProcessRemote;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3

public interface ItemLotOwnCopyPrcRemote extends ProcessRemote
{
	public String process() throws RemoteException,ITMException;
	public String process(String string1, String string2, String windowName, String xtraParams) throws RemoteException,ITMException;
}