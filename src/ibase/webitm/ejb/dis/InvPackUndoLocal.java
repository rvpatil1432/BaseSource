package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ProcessLocal;
import javax.ejb.Local;


@Local // added for ejb3

public interface InvPackUndoLocal extends ProcessLocal
{
	public String process() throws RemoteException,ITMException;
	public String process(String string1, String string2, String windowName, String xtraParams) throws RemoteException,ITMException;
}
