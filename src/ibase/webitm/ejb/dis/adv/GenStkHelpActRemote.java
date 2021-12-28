package ibase.webitm.ejb.dis.adv;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
//import javax.ejb.EJBObject;
import javax.ejb.Remote;//added for ejb3
import org.w3c.dom.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerRemote;

@Remote


//public interface GenStkHelpAct extends ActionHandler,EJBObject

public interface GenStkHelpActRemote extends ActionHandlerRemote
{
	public String actionHandler() throws RemoteException,ITMException;	
	public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException,ITMException;
	public String actionHandler(String actionType, String xmlString, String xmlString1, String objContext, String xtraParams) throws RemoteException,ITMException;
	
	
}