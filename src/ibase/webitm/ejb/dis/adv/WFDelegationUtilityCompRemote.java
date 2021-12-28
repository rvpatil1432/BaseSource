package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.Connection;
//import javax.ejb.EJBObject;


import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.ejb.ValidatorRemote;

import javax.ejb.Remote; // added for ejb3

@Remote // added for ejb3
public interface WFDelegationUtilityCompRemote extends ValidatorRemote
{
	public void generateEmailLink(String xmlString ,String xtraParam, String lineNo);
}