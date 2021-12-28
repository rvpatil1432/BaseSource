package ibase.webitm.ejb.dis;

import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ValidatorRemote;
import ibase.webitm.utility.ITMException;

import javax.ejb.Remote;

@Remote

public interface SorderStatusEJBRemote extends ValidatorRemote
{
	//Added by sarita to add userInfo on 8JAN2018
	//public String getSorderStatusXML(String saleOrder,String ref_series)throws ITMException;
	public String getSorderStatusXML(String saleOrder,String ref_series,UserInfoBean userInfo)throws ITMException;
}
