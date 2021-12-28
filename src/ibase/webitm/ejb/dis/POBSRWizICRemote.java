package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Remote;
import org.w3c.dom.Document;

@Remote
public abstract interface POBSRWizICRemote extends ValidatorRemote
{
  public abstract String wfValData(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6)
    throws RemoteException, ITMException;

  public abstract String wfValData(Document paramDocument1, Document paramDocument2, Document paramDocument3, String paramString1, String paramString2, String paramString3)
    throws RemoteException, ITMException;

  public abstract String itemChanged(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7)
    throws RemoteException, ITMException;

  public abstract String itemChanged(Document paramDocument1, Document paramDocument2, Document paramDocument3, String paramString1, String paramString2, String paramString3, String paramString4)
    throws RemoteException, ITMException;
}