package ibase.webitm.ejb.dis.adv;


import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ejb.Stateless;

import org.w3c.dom.Document;

import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.utility.ITMException;

@Stateless
public class DistOpenPrc extends ActionHandlerEJB
  implements DistOpenPrcLocal, DistOpenPrcRemote
{
  public String actionHandler(String tranId, String xtraParams, String forcedFlag)
    throws RemoteException, ITMException
  {
    String returnString = null;

    System.out.println(".......tranId......." + tranId);
    System.out.println(".......xtraParams..." + xtraParams);
    System.out.println(".......forcedFlag..." + forcedFlag);
    if ((tranId != null) && (tranId.trim().length() > 0))
    {
      returnString = opendistOrder(tranId, xtraParams, forcedFlag);
    }
    return returnString;
  }

  public String opendistOrder(String distOrder, String xtraParams, String forcedFlag) throws RemoteException, ITMException
  {
    System.out.println("openSaleOrder called........");
    String sql = "",sql1="";
    String errString = "";
    Connection conn = null;
    ConnDriver connDriver = null;
    PreparedStatement pstmt = null,pstmt1=null;
    ResultSet rs = null;
    ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
    E12GenericUtility genericUtility = null;
    Timestamp sysdate = null;
    String chgTerm = ""; String chgUser = "";
    int upCount = 0;
    String status = ""; String status1 = ""; String confirm = ""; String dorder = "";
    String disConfirm = ""; String disOrder = "",qtyOrder="",qtyShipped="",disStatus="",dOrder="";
    double qty = 0.0D; double qtydesp = 0.0D;
    boolean flag = false;
    try
    {
      connDriver = new ConnDriver();
      //Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
      connDriver = null;
      conn.setAutoCommit(false);
      genericUtility = new E12GenericUtility();
      Date dt = new Date();
      SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
      sysdate = Timestamp.valueOf(sdf1.format(dt) + " 00:00:00.0");
      chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "CHG_TERM");
      chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "CHG_USER");
      System.out.println("distOrder @@@@@@@@@@@ORDER===========" + distOrder);
      sql = "select confirmed,dist_order,status from distorder where dist_order= ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, distOrder);
      rs = pstmt.executeQuery();
      if (rs.next())
      {
        confirm = rs.getString("confirmed");

        dorder = rs.getString("dist_order");
        status = rs.getString("status");
      }

      System.out.println("Confirmed is : " + confirm);
      String str1;
      if (confirm.equals("N"))
      {
        errString = itmDBAccessLocal.getErrorString("", "VTDLONTCFM", "","",conn);
        str1 = errString;
        return str1;
      }
      

	  if (status.equals("P"))
  {
    System.out.println("already pending......@@@@@@@@@@@@@@@");

    errString = itmDBAccessLocal.getErrorString("", "VTDOPEN", "","",conn);
    str1 = errString;
    return str1;
  }
      

      System.out.println("Status is : " + status);
      rs.close();
      rs = null;
      pstmt.close();
      pstmt = null;

    
      sql = "select qty_order,qty_shipped,distorder.status,distorder.dist_order from distorder_det ,distorder  where distorder.dist_order=distorder_det.dist_order and  distorder.dist_order=? ";
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, dorder);
      rs = pstmt.executeQuery();
      while(rs.next())
      {
    	  qtyOrder = rs.getString(1);
    	  qtyShipped = rs.getString(2);
    	  status = rs.getString(3);
    	  dOrder = rs.getString(4);
    	 	  
    	  if(qtyOrder.equals(qtyShipped) || qtyOrder==qtyShipped)
          {
    		  
    		 
        	  System.out.println("---------------------->FULL ISSUED, CAN NOT BE OPEN");
        	  errString = itmDBAccessLocal.getErrorString("", "VTDOFC", "","",conn);
              str1 = errString;
           	  return str1;
        	 
          }
    	  else
    	  {
    		  
    		   if (status.equals("X"))
    		      {
    		        sql1 = " update distorder set status='P' where dist_order=?";
    		        pstmt1 = conn.prepareStatement(sql1);
    		        pstmt1.setString(1, dOrder);
    		        upCount = pstmt1.executeUpdate();
    		        pstmt1.close();
    		        pstmt1 = null;
    		        System.out.println("Update Count====" + upCount);

    		       
    		        if (upCount > 0)
    		        {
    		          errString = itmDBAccessLocal.getErrorString("", "VTDORDOP", "","",conn);
    		          str1 = errString;
    		          return str1;
    		        }
    		      }
    		   else
    		      {
    		          
    		          errString = itmDBAccessLocal.getErrorString("", "VTDONO", "","",conn);
    		          str1 = errString;
    		          return str1;
    		        }
    		 
    	
    	  }
    	  
    	  
      }
      System.out.println("qtyOrder=========" + qtyOrder);
      System.out.println("qtyShipped=====" + qtyShipped);
        
      rs.close();
      rs = null;
      pstmt.close();
      pstmt = null;
     

    }
    catch (Exception e)
    {
      System.out.println("distorder..." + e.getMessage());
      e.printStackTrace();
      try
      {
        conn.rollback();
      }
      catch (Exception e1)
      {
        System.out.println("distorder..." + e1.getMessage());
        e1.printStackTrace();
      }

    }
    finally
    {
      try
      {
        if ((errString != null) && (errString.trim().length() > 0))
        {
          System.out.println("--going to commit tranaction--");
          if (errString.indexOf("VTDORDOP") > -1)
          {
            conn.commit();
            System.out.println("--transaction commited--");
          }
          else
          {
            conn.rollback();
            System.out.println("--transaction rollback--");
          }
        }

        if (rs != null)
        {
          rs.close();
          rs = null;
        }
        if (pstmt != null)
        {
          pstmt.close();
          pstmt = null;
        }
        if (conn != null)
        {
          conn.close();
          conn = null;
        }
      }
      catch (Exception e)
      {
        System.out.println("Exception : " + e);
        e.printStackTrace();
        throw new ITMException(e);
      }
    }   

    return errString;
  }
}