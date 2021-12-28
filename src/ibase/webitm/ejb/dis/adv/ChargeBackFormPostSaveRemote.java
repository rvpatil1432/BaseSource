package ibase.webitm.ejb.dis.adv;

import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;



	public interface ChargeBackFormPostSaveRemote extends ValidatorRemote//, EJBObject
	{
		public String postSave(String winName,String editFlag,String tranId,  String xtraParams,Connection conn) throws RemoteException,ITMException;
		public String postSaveRec(String xmlString1,String domID,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;

	}

