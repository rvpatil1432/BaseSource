package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import java.sql.*;

import java.rmi.RemoteException;

// The local interface declares all the utility functions to be used in your EJB component
@javax.ejb.Local
public interface SOItemSpecPrsLocal extends ValidatorLocal
{
	public String preSave()throws RemoteException,ITMException;
	public String preSave(String xmlString, String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException;
}
