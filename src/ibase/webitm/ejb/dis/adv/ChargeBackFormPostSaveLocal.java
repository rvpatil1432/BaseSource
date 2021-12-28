package ibase.webitm.ejb.dis.adv;


	import ibase.webitm.ejb.*;

	import java.rmi.RemoteException;
	//import javax.ejb.EJBObject;
	import java.sql.Connection;

	import ibase.webitm.utility.ITMException;
	import javax.ejb.Local; // added for ejb3

	

	@Local // added for ejb3

	public interface ChargeBackFormPostSaveLocal extends ValidatorLocal//, EJBObject
	{
		public String postSave(String winName,String editFlag,String tranId,  String xtraParams,Connection conn) throws RemoteException,ITMException;
		public String postSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
	}

