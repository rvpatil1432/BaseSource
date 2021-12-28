package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.Local;//added for ejb3
//import ibase.webitm.ejb.ActionHandler;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerLocal;
import org.w3c.dom.*;

@Local
//public interface GenStkHelpActHome extends ActionHandlerHome,EJBHome

public interface GenStkHelpActLocal extends ActionHandlerLocal
{
	public String actionHandler() throws RemoteException,ITMException;	
	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException;
	public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException;
	
	
}