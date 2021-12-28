package ibase.webitm.ejb.dis.adv;

import java.util.*;
import java.sql.*;
import java.rmi.RemoteException;
import java.text.*;
//import javax.ejb.SessionContext;
//import javax.ejb.CreateException;
//import javax.ejb.SessionBean;
import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.utility.*;

import ibase.utility.*;
import ibase.webitm.ejb.*;
import ibase.system.config.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3

public class SretConfRetn extends ActionHandlerEJB implements SretConfRetnLocal, SretConfRetnRemote //implements SessionBean
{
   /* public void ejbCreate() throws RemoteException,CreateException{
    }
    public void ejbRemove(){

    }
    public void ejbActivate(){

    }
    public void ejbPassivate(){

    }
    public void setSessionContext(SessionContext se){

    }*/
    public String confirm(String tranId,String xtraParams, String forcedFlag) throws RemoteException,ITMException
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "";
        ConnDriver connDriver = null;
		String loginEmpCode = null;
        ibase.utility.E12GenericUtility genericUtility= null;
		Document dom = null;
		int count=0;
		ITMDBAccessEJB itmDBAccessEJB = null;
		String errString = null;
		int upd = 0;
        try
		{
            itmDBAccessEJB = new ITMDBAccessEJB();
			genericUtility = new ibase.utility.E12GenericUtility();
            connDriver = new ConnDriver();
            //Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
            conn.setAutoCommit(false);

			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			int countDet = 0;
			if( errString == null || errString.trim().length() == 0 )
			{
				sql = " select count( 1 ) cnt from sreturn where tran_id = ? AND RETURN_CONFIRM = 'Y' ";
					
				pstmt = conn.prepareStatement( sql );
				pstmt.setString( 1, tranId.trim() );
				rs = pstmt.executeQuery();
				
				if( rs.next() )
				{
					countDet = rs.getInt( "cnt" );
				}
				pstmt.close();
				pstmt = null;				
				rs.close();
				rs = null;
				
				if( countDet > 0 )
				{
					errString = itmDBAccessEJB.getErrorString("","VTCONFMD","","",conn);
				}
			}
			if( errString == null || errString.trim().length() == 0 )
			{
				sql = "update sreturn set RETURN_CONFIRM = 'Y' where tran_id = ? ";
				pstmt = conn.prepareStatement( sql );
				pstmt.setString( 1, tranId.trim() );
				pstmt.executeUpdate();
				pstmt.close();
				pstmt = null;
			}
            if( errString != null && errString.trim().length() > 0 )
			{
                conn.rollback();
                return errString;
            }
			else
			{
				conn.commit();
				errString = itmDBAccessEJB.getErrorString("","VTCNFSUCC","","",conn);
			}
            System.out.println("errString : "+errString);
        }catch(ITMException ie)
		{
            try
			{
				conn.rollback();
		    }
			catch(Exception t){}
            ie.printStackTrace();
            errString = itmDBAccessEJB.getErrorString("","VTDESNCONF","","",conn);
           throw new ITMException( ie );

        }catch(Exception e){
            try{
				conn.rollback();
			}
			catch(Exception t){}
            e.printStackTrace();
            errString = itmDBAccessEJB.getErrorString("","VTDESNCONF","","",conn);
            throw new ITMException( e );
        }
		finally
		{
            try{
				if( pstmt != null )
				{
					pstmt.close();
				}
				pstmt = null;
				if(conn != null)
				{
					conn.close();
				}
				conn = null;
            }catch(Exception e){//System.out.println("Exception : "+e);
				e.printStackTrace();
			}
        }
        return errString;
	}
}

