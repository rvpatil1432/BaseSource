package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;

import javax.ejb.Local;

@Local
public interface AwacsToES3ICLocal 
{
	public String wfValData(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6) throws RemoteException;
}
