package ibase.webitm.ejb.dis.adv;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Local;

import org.w3c.dom.Document;

@Local
public interface InvAmdPostConfSMSCompLocal extends ValidatorLocal
{
	public String sendSMS(String xmlString, String formatCode, String xtraParams) throws RemoteException,ITMException;
}