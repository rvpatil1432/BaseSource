package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Date;

import ibase.webitm.ejb.ValidatorLocal;
import ibase.webitm.utility.ITMException;

public interface CreatePoVoucherAdvanceLocal extends ValidatorLocal {
	public String createPoVoucherAdv(String tranId,String xtraParams,Connection conn,String as_flag,int ad_advperc,Date day)throws RemoteException,ITMException;

}
