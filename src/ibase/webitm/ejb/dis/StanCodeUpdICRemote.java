package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Remote;

@Remote
public interface StanCodeUpdICRemote extends ValidatorRemote
{
	public String wfValData(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6) throws RemoteException,ITMException;
	public String itemChanged(String currFrmXmlStr, String hdrFrmXmlStr,String allFrmXmlStr, String objContext, String currentColumn,String editFlag, String xtraParams) throws RemoteException,ITMException;
}
