package ibase.webitm.ejb.dis.adv;

import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Remote;

import org.w3c.dom.Document;

@Remote
public interface InvAmdPostConfSMSCompRemote extends ValidatorRemote
{
	public String sendSMS(String xmlString, String formatCode, String xtraParams) throws RemoteException,ITMException;
}