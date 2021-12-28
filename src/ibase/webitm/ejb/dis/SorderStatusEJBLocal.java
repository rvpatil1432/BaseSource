package ibase.webitm.ejb.dis;

import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import javax.ejb.Local;

import org.w3c.dom.Document;

@Local
public interface SorderStatusEJBLocal extends ValidatorLocal
{
	//added by sarita to add userInfo on 8JAN2018
	//public String getSorderStatusXML(String saleOrder,String ref_series)throws ITMException;
	public String getSorderStatusXML(String saleOrder,String ref_series,UserInfoBean userInfo)throws ITMException;
}
