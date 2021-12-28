package ibase.webitm.ejb.dis;

import ibase.webitm.ejb.ActionHandlerRemote;
import ibase.webitm.utility.ITMException;

import java.sql.Connection;

import javax.ejb.Remote;
@Remote // added for ejb3
public interface PostOrdInvoicePostRemote  extends ActionHandlerRemote 
{
	//public String invoicePosting(String invoiceId,Connection conn)throws ITMException;
	public String invoicePosting(String invoiceId,String xtraParams,String forcedFlag,Connection conn)throws ITMException;
}
