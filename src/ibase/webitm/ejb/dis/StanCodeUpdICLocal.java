package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Local;

@Local
public interface StanCodeUpdICLocal extends ValidatorLocal
{
	public String wfValData(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6) throws RemoteException,ITMException;
	public String itemChanged(String currFrmXmlStr, String hdrFrmXmlStr,String allFrmXmlStr, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException;
}
