package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import java.util.HashMap;
import java.sql.Connection;

import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3


//public interface DistStkUpd extends EJBObject
@Remote // added for ejb3
public interface DistStkUpdRemote 
{
    public int updAllocTrace(HashMap hmp) throws RemoteException,ITMException;
    public int updAllocTrace(HashMap hmp, Connection conn) throws RemoteException,ITMException;
}