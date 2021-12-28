/**
 * DEVELOP BY RITESH ON 02/JAN/14
 * PURPOSE: ROAD PERMIT RECORDS GENERATION PROCESS (DI3ISUN015)
 */
package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import org.w3c.dom.*;
import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Local; 

@Local 
public interface RpermitGenICLocal extends ValidatorLocal
{
	
	public String itemChanged(String xmlString, String xmlString1, String xmlString2,  String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;

	public String itemChanged(Document dom, Document dom1, Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
			
}