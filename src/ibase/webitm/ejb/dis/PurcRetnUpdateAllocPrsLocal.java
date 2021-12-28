package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import java.sql.Connection;

import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ValidatorLocal;
import javax.ejb.Local; // added for ejb3

@Local // added for ejb3
public interface PurcRetnUpdateAllocPrsLocal extends ValidatorLocal  //, EJBObject
{
	public String preSaveRec()throws RemoteException,ITMException;
	public String preSaveRec(String xmlString,String xmlString1,String objContext,String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}