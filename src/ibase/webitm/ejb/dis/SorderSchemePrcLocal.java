/**
Title : SorderSchemePrcLocal
Date  : 09-12-16
Author: Wasim Ansari
*/

package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;


import org.w3c.dom.*;

@javax.ejb.Local
public interface SorderSchemePrcLocal extends ProcessLocal
{
	public String process() throws RemoteException,ITMException;
	public String process(Document dom, Document dom2, String objContext, String xtraParams) throws RemoteException,ITMException;
	public String process(String xmlString, String xmlString2, String objContext, String xtraParams) throws RemoteException,ITMException;
}