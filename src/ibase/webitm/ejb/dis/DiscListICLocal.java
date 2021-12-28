
package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import org.w3c.dom.Document;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local;
@Local //Changed By Nasruddin 23-SEP-16 extends ValidatorLocal
public interface DiscListICLocal extends ValidatorLocal
{
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
	public String itemChanged(Document dom, Document dom1, Document dom2,String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
}
