/**
Title : BugResolutionPrcRemote
Date  : 05/07/2016
Author: Wasim Ansari

*/

package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;

import org.w3c.dom.*;

@javax.ejb.Remote
public interface BugResolutionPrcRemote  extends ProcessRemote
{
	public String process() throws RemoteException,ITMException;
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException;
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
}