package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import javax.ejb.Remote;
import ibase.webitm.utility.ITMException;

@Remote
public interface EwaybillPrcRemote {
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;

}
