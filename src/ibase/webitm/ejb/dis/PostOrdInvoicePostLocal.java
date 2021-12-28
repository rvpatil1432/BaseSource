package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ActionHandlerLocal;
import ibase.webitm.utility.ITMException;

import java.sql.Connection;

import javax.ejb.Local;
@Local // added for ejb3
public interface PostOrdInvoicePostLocal extends ActionHandlerLocal
{
	//public String invoicePosting(String invoiceId,Connection conn)throws ITMException;
	public String invoicePosting(String invoiceId,String xtraParams,String forcedFlag,Connection conn)throws ITMException;
}
