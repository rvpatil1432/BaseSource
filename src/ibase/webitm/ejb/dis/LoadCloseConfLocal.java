package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;

import java.rmi.RemoteException;
import java.sql.Connection;

import ibase.webitm.utility.ITMException;

import javax.ejb.Local; // added for ejb3

import org.w3c.dom.Document;

@Local // added for ejb3

public interface LoadCloseConfLocal extends ActionHandlerLocal//, EJBObject
{
	public String confirm(String xmlString,String tranId,String xtraParams) throws RemoteException,ITMException;
	
}
