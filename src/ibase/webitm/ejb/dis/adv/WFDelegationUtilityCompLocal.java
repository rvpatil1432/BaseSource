package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.Connection;
//import javax.ejb.EJBObject;


import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.ejb.ValidatorLocal;

import javax.ejb.Local; // added for ejb3

@Local // added for ejb3
public interface WFDelegationUtilityCompLocal extends ValidatorLocal
{
	public void generateEmailLink(String xmlString ,String xtraParam, String lineNo);
}