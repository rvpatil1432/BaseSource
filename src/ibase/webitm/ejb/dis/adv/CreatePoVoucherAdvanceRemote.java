package ibase.webitm.ejb.dis.adv;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Date;

public interface CreatePoVoucherAdvanceRemote extends ValidatorLocal {
	public String createPoVoucherAdv(String tranId,String xtraParams,Connection conn,String as_flag,int ad_advperc,Date day)throws RemoteException,ITMException;

}
