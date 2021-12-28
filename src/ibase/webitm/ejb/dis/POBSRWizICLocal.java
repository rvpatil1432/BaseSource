package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import javax.ejb.Local;
import org.w3c.dom.Document;

@Local
public abstract interface POBSRWizICLocal extends ValidatorLocal
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