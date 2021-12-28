/********************************************************
	Title : CustItemUpdatePrcLocal[D14LSUN003]
	Date  : 11/03/15
	Developer: Priyanka Shinde

 ********************************************************/
package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ProcessLocal;
import javax.ejb.Local;


@Local // added for ejb3

public interface CustItemUpdatePrcLocal extends ProcessLocal
{
	public String process() throws RemoteException,ITMException;
	public String process(String string1, String string2, String windowName, String xtraParams) throws RemoteException,ITMException;
}