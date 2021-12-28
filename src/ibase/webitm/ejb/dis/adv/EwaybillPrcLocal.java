package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import javax.ejb.Local;
import ibase.webitm.utility.ITMException;

@Local
public interface EwaybillPrcLocal {
	public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;

}
