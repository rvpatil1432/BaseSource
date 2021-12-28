package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ProcessLocal;
import javax.ejb.Local;

import org.w3c.dom.Document;


@Local // added for ejb3

public interface PorderOpenPrcLocal extends ProcessLocal{
	
public String getData(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException;
public String getdata(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException;
public String process() throws RemoteException,ITMException;
public String process(String string1, String string2, String windowName, String xtraParams) throws RemoteException,ITMException;
public String process(Document headerDom, Document detailDom, String windowName, String xtraParams) throws RemoteException,ITMException;

}