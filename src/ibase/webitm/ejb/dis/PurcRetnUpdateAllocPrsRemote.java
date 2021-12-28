package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import java.sql.Connection;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ValidatorRemote;
import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3

public interface PurcRetnUpdateAllocPrsRemote extends ValidatorRemote //, EJBObject
{
	public String preSaveRec()throws RemoteException,ITMException;
	public String preSaveRec(String xmlString,String xmlString1,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}