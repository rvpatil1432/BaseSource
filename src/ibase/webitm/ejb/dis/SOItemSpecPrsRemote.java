package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import java.sql.*;
import java.rmi.RemoteException;

// The remote interface declares all the utility functions to be used in your EJB component
@javax.ejb.Remote
public interface SOItemSpecPrsRemote extends ValidatorRemote//, EJBObject
{
	public String preSave()throws RemoteException,ITMException;
	public String preSave(String xmlString, String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}


