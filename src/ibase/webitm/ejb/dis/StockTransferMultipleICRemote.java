/**
* PURPOSE : Remote Interface 
* AUTHOR  : BALU  
* Date    : 19/09/2011
*/

package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import org.w3c.dom.*;
import javax.xml.parsers.*;

//The remote interface declares the functions to be implemented for performing validation and itemchange
@javax.ejb.Remote
public interface StockTransferMultipleICRemote extends ValidatorRemote
{
	public String wfValData() throws RemoteException,ITMException;
	
	public String wfValData(Document currDom, Document hdrDom, Document allDom, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String wfValData(String currFrmXmlStr, String hdrFrmXmlStr, String allfrmXmlStr, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String itemChanged() throws RemoteException,ITMException;
	
	public String itemChanged(String currFrmXmlStr, String hdrFrmXmlStr,String allFrmXmlString, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
	
	public String itemChanged(Document currDom, Document hdrDom, Document allDom, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException;
}
