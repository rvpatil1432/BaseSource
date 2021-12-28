package ibase.webitm.ejb.dis.adv;

import ibase.system.config.AppConnectParm;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.MasterStatefulLocal;
import ibase.webitm.ejb.XML2DBEJB;
import ibase.webitm.ejb.fin.FinCommon;
import ibase.webitm.utility.GenerateXmlFromDB;
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class POBWizSRConf extends ActionHandlerEJB
  implements POBWizSRConfLocal, POBWizSRConfRemote
{

	public String pobConfirm(String tranId, String xtraParams, String forcedFlag) throws RemoteException, ITMException 
	{
		String userInfoStr = "";
		String errString = "";
		try
		{ 
			System.out.println("userInfoStr of confirm::::: " +userInfoStr);
			errString = pobsrConfirm(tranId, xtraParams, forcedFlag, userInfoStr);
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in [POBWizConf] confirm " + e.getMessage());
			throw new ITMException(e);
		}
		return errString;
		
	}

  public String pobsrConfirm(String tranId, String xtraParams, String forcedFlag, String userInfoStr)
    throws RemoteException, ITMException
  {
    System.out.println(">>>>>>>>>>>>>>>>>>POBWizSRConf confirm called(ABHIJIT)>>>>>>>>>>>>>>>>>>>");
    String confirmed = "";
    String sql = "";
    Connection conn = null;
    PreparedStatement pstmt = null;
    String errString = null;
    ResultSet rs = null;
    String loginEmpCode = ""; String siteCode = "";
    String pobNo = ""; String loginSiteCode = ""; String custCode = "";
    String itemSer = ""; String orderType = ""; String retailerCode = "";
    String crTerm = ""; String custCodeBil = ""; String salespers = ""; String salespers1 = ""; String salespers2 = ""; String frtTerm = "";
    String groupCode = ""; String salesPersTmp = ""; String crTermTmp = ""; String salesPersTmp1 = ""; String salesPersTmp2 = ""; String transMode = "";
    String deliveryTerm = ""; String tranCode = ""; String currCodeFrt = ""; String currCodeIns = ""; String add1 = ""; String add2 = ""; String add3 = ""; String tele1 = ""; String tele2 = ""; String tele3 = "";
    String stanCode = ""; String city = ""; String countCode = ""; String pin = ""; String stateCode = ""; String dlvDescr = ""; String pricelist = ""; String pricelistClg = "";
    String userId = ""; String termId = "";
    String currCode1 = ""; String currCode2 = ""; String discount = "";
    String linenoStr = ""; String itemCode = ""; String rate = ""; String quantityStr = ""; String totQty = ""; String netAmt = "";
    StringBuffer xmlBuff = null;
    String xmlString = null; String retString = null;
    String sysDate = ""; String lineNoStr = "";
    int cnt = 0; int lineNo = 0;
    double exchRateFr = 0.0D; double spRate = 0.0D;
    double quantity = 0.0D; double freeQty = 0.0D;
    Timestamp currDate = null;
    Timestamp sysDate1 = null; Timestamp addDate = null; Timestamp chgDate = null; Timestamp tranDate = null;
    ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
    E12GenericUtility genericUtility = new E12GenericUtility();
    FinCommon finCommon = null;
    try
    {
      ConnDriver connDriver = null;
      connDriver = new ConnDriver();
      String transDB = null;
		//conn = connDriver.getConnectDB("DriverITM");
		if(userInfoStr != null && userInfoStr.trim().length() > 0)
		{
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
			transDB       = userInfo.getTransDB();
		}
		if (transDB != null && transDB.trim().length() > 0)
		{
			conn = connDriver.getConnectDB(transDB);
		}
		
		else
		{
			conn = connDriver.getConnectDB("DriverITM");
		}
      conn.setAutoCommit(false);
      finCommon = new FinCommon();
      userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
      termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
      loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
      SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
      currDate = Timestamp.valueOf(sdf1.format(new Date()).toString() + " 00:00:00.0");
      Calendar currentDate = Calendar.getInstance();
      System.out.println("currDate>>>>>>" + currDate);
      SimpleDateFormat sdf = new SimpleDateFormat(
        genericUtility.getApplDateFormat());
      sysDate = sdf.format(currentDate.getTime());
      System.out.println("Now the date is :=>  " + sysDate);
      if ((tranId != null) && (tranId.trim().length() > 0))
      {
        System.out.println("@@@@@tranId[SR]" + tranId + "]");

        sql = "\tselect tran_id,tran_date,site_code,item_ser,order_type,cust_code,confirmed  from  pob_hdr where tran_id = ? ";

        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, tranId);
        rs = pstmt.executeQuery();
        if (rs.next())
        {
          pobNo = rs.getString("tran_id");
          tranDate = rs.getTimestamp("tran_date");
          siteCode = checkNull(rs.getString("site_code"));
          itemSer = checkNull(rs.getString("item_ser"));
          orderType = checkNull(rs.getString("order_type"));
          custCode = checkNull(rs.getString("cust_code"));
          confirmed = checkNull(rs.getString("confirmed"));
        }
        rs.close();
        rs = null;
        pstmt.close();
        pstmt = null;
        if ("N".equalsIgnoreCase(confirmed))
        {
          sql = "select cr_term,stan_code ,city,count_code,pin,state_code,frt_term,curr_code,cust_name,group_code,trans_mode,cust_code__bil,price_list,price_list__clg,sales_pers,sales_pers__1,sales_pers__2,dlv_term, TRAN_CODE, addr1,addr2,addr3,tele1,tele2,tele3  from customer where cust_code = ?  ";

          pstmt = conn.prepareStatement(sql);
          pstmt.setString(1, custCode);
          rs = pstmt.executeQuery();
          if (rs.next())
          {
            crTerm = rs.getString("cr_term") == null ? " " : rs.getString("cr_term");
            stanCode = rs.getString("stan_code") == null ? " " : rs.getString("stan_code");
            city = rs.getString("city") == null ? " " : rs.getString("city");
            countCode = rs.getString("count_code") == null ? " " : rs.getString("count_code");
            pin = rs.getString("pin") == null ? " " : rs.getString("pin");
            stateCode = rs.getString("state_code") == null ? " " : rs.getString("state_code");
            frtTerm = rs.getString("frt_term") == null ? " " : rs.getString("frt_term");
            dlvDescr = rs.getString("cust_name");
            groupCode = rs.getString("group_code") == null ? " " : rs.getString("group_code");
            transMode = rs.getString("trans_mode") == null ? " " : rs.getString("trans_mode");
            custCodeBil = rs.getString("cust_code__bil") == null ? " " : rs.getString("cust_code__bil");
            pricelist = rs.getString("price_list") == null ? " " : rs.getString("price_list");
            pricelistClg = rs.getString("price_list__clg") == null ? " " : rs.getString("price_list__clg");
            salespers = rs.getString("sales_pers") == null ? " " : rs.getString("sales_pers");
            salespers1 = rs.getString("sales_pers__1") == null ? " " : rs.getString("sales_pers__1");
            salespers2 = rs.getString("sales_pers__2") == null ? " " : rs.getString("sales_pers__2");
            deliveryTerm = rs.getString("dlv_term") == null ? " " : rs.getString("dlv_term");
            tranCode = rs.getString("TRAN_CODE") == null ? " " : rs.getString("TRAN_CODE");
            add1 = rs.getString("addr1") == null ? " " : rs.getString("addr1");
            add2 = rs.getString("addr2") == null ? " " : rs.getString("addr2");
            add3 = rs.getString("addr3") == null ? " " : rs.getString("addr3");
            tele1 = rs.getString("tele1") == null ? " " : rs.getString("tele1");
            tele2 = rs.getString("tele2") == null ? " " : rs.getString("tele2");
            tele3 = rs.getString("tele3") == null ? " " : rs.getString("tele3");
            currCode1 = rs.getString("curr_code") == null ? "" : rs.getString("curr_code");
          }

          rs.close();
          rs = null;
          pstmt.close();
          pstmt = null;

          sql = "select sales_pers,sales_pers__1,sales_pers__2,cr_term from customer_series where cust_code = ? and item_ser = ?";
          pstmt = conn.prepareStatement(sql);
          pstmt.setString(1, custCode);
          pstmt.setString(2, itemSer);
          rs = pstmt.executeQuery();
          if (rs.next())
          {
            salesPersTmp = rs.getString("sales_pers") == null ? " " : rs.getString("sales_pers");
            crTermTmp = rs.getString("cr_term") == null ? " " : rs.getString("cr_term");
            salesPersTmp1 = rs.getString("sales_pers__1") == null ? " " : rs.getString("sales_pers__1");
            salesPersTmp2 = rs.getString("sales_pers__2") == null ? " " : rs.getString("sales_pers__2");
          }

          rs.close();
          rs = null;
          pstmt.close();
          pstmt = null;

          sql = "select curr_code from finent where fin_entity in (select fin_entity from site where site_code = ? )";
          pstmt = conn.prepareStatement(sql);
          pstmt.setString(1, siteCode);
          rs = pstmt.executeQuery();
          if (rs.next())
          {
            currCode2 = rs.getString("curr_code") == null ? "" : rs.getString("curr_code");
          }
          if (rs != null)
            rs.close();
          rs = null;
          if (pstmt != null)
            pstmt.close();
          pstmt = null;

          xmlBuff = new StringBuffer();

          System.out.println("--XML CREATION --Abhi PriceList");
          System.out.println("--XML CREATION --");

          xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n");
          xmlBuff.append("<DocumentRoot>");
          xmlBuff.append("<description>").append("Datawindow Root").append("</description>");
          xmlBuff.append("<group0>");
          xmlBuff.append("<description>").append("Group0 description").append("</description>");
          xmlBuff.append("<Header0>");
          xmlBuff.append("<objName><![CDATA[").append("sorder").append("]]></objName>");
          xmlBuff.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
          xmlBuff.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
          xmlBuff.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
          xmlBuff.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
          xmlBuff.append("<action><![CDATA[").append("SAVE").append("]]></action>");
          xmlBuff.append("<elementName><![CDATA[").append("").append("]]></elementName>");
          xmlBuff.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
          xmlBuff.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
          xmlBuff.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
          xmlBuff.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
          xmlBuff.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
          xmlBuff.append("<description>").append("Header0 members").append("</description>");

          xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objName=\"sorder\" objContext=\"1\">");
          xmlBuff.append("<attribute selected=\"N\" updateFlag=\"A\" status=\"N\" pkNames=\"\"/>");
          xmlBuff.append("<sale_order/>");
          xmlBuff.append("<order_type><![CDATA[" + orderType + "]]></order_type>");
          xmlBuff.append("<order_date><![CDATA[" + sysDate + "]]></order_date>");
          xmlBuff.append("<item_ser><![CDATA[" + itemSer.trim() + "]]></item_ser>");
          xmlBuff.append("<site_code><![CDATA[" + siteCode.trim() + "]]></site_code>");
          xmlBuff.append("<site_code__ship><![CDATA[" + siteCode.trim() + "]]></site_code__ship>");
          xmlBuff.append("<cust_code><![CDATA[" + groupCode.trim() + "]]></cust_code>");
          xmlBuff.append("<dlv_to><![CDATA[" + dlvDescr + "]]></dlv_to>");
          xmlBuff.append("<cust_code__bil><![CDATA[" + custCode + "]]></cust_code__bil>");
          xmlBuff.append("<cust_code__dlv><![CDATA[" + custCode + "]]></cust_code__dlv>");
          xmlBuff.append("<stan_code><![CDATA[" + stanCode.trim() + "]]></stan_code>");
          xmlBuff.append("<STAN_CODE__INIT><![CDATA[" + stanCode.trim() + "]]></STAN_CODE__INIT>");
          xmlBuff.append("<cr_term><![CDATA[" + crTerm + "]]></cr_term>");
          xmlBuff.append("<trans_mode><![CDATA[" + transMode.trim() + "]]></trans_mode>");
          xmlBuff.append("<emp_code__ord><![CDATA[" + loginEmpCode + "]]></emp_code__ord>");
          xmlBuff.append("<curr_code><![CDATA[" + currCode1.trim() + "]]></curr_code>");
          xmlBuff.append("<curr_code__frt><![CDATA[" + currCode1.trim() + "]]></curr_code__frt>");
          xmlBuff.append("<curr_code__ins><![CDATA[" + currCode1.trim() + "]]></curr_code__ins>");
          if (currCode1.equalsIgnoreCase(currCode2))
          {
            exchRateFr = 1.0D;
            xmlBuff.append("<exch_rate><![CDATA[" + exchRateFr + "]]></exch_rate>");
            xmlBuff.append("<exch_rate__comm><![CDATA[" + exchRateFr + "]]></exch_rate__comm>");
            xmlBuff.append("<exch_rate__comm_1><![CDATA[" + exchRateFr + "]]></exch_rate__comm_1>");
            xmlBuff.append("<exch_rate__comm_2><![CDATA[" + exchRateFr + "]]></exch_rate__comm_2>");
          }
          else {
            exchRateFr = finCommon.getDailyExchRateSellBuy(currCode1, "", siteCode, sdf.format(tranDate).toString(), "S", conn);
            System.out.println("((((((((( " + exchRateFr + " ))))))))))))");
            xmlBuff.append("<exch_rate><![CDATA[" + exchRateFr + "]]></exch_rate>");
            xmlBuff.append("<exch_rate__comm><![CDATA[" + exchRateFr + "]]></exch_rate__comm>");
            xmlBuff.append("<exch_rate__comm_1><![CDATA[" + exchRateFr + "]]></exch_rate__comm_1>");
            xmlBuff.append("<exch_rate__comm_2><![CDATA[" + exchRateFr + "]]></exch_rate__comm_2>");
          }
          xmlBuff.append("<dlv_city><![CDATA[" + city + "]]></dlv_city>");
          xmlBuff.append("<dlv_pin><![CDATA[" + pin + "]]></dlv_pin>");
          xmlBuff.append("<dlv_add1><![CDATA[" + add1 + "]]></dlv_add1>");
          xmlBuff.append("<dlv_add2><![CDATA[" + add2 + "]]></dlv_add2>");
          xmlBuff.append("<dlv_add3><![CDATA[" + add3 + "]]></dlv_add3>");
          xmlBuff.append("<tel1__dlv><![CDATA[" + tele1 + "]]></tel1__dlv>");
          xmlBuff.append("<tel2__dlv><![CDATA[" + tele2 + "]]></tel2__dlv>");
          xmlBuff.append("<tel3__dlv><![CDATA[" + tele3 + "]]></tel3__dlv>");
          xmlBuff.append("<state_code__dlv><![CDATA[" + stateCode + "]]></state_code__dlv>");
          xmlBuff.append("<count_code__dlv><![CDATA[" + countCode + "]]></count_code__dlv>");
          xmlBuff.append("<tran_id__porcp><![CDATA[" + tranId + "]]></tran_id__porcp>");
          xmlBuff.append("<status_remarks><![CDATA[generate from POB " + tranId + "]]></status_remarks>");
          xmlBuff.append("<chg_user><![CDATA[" + userId + "]]></chg_user>");
          xmlBuff.append("<chg_term><![CDATA[" + termId + "]]></chg_term>");
          xmlBuff.append("<chg_date><![CDATA[" + sysDate + "]]></chg_date>");
          xmlBuff.append("<tran_code><![CDATA[" + tranCode.trim() + "]]></tran_code>");

          xmlBuff.append("<sales_pers><![CDATA[" + salespers.trim() + "]]></sales_pers>");
          xmlBuff.append("<sales_pers__1><![CDATA[" + salespers1.trim() + "]]></sales_pers__1>");
          xmlBuff.append("<sales_pers__2><![CDATA[" + salespers1.trim() + "]]></sales_pers__2>");
          xmlBuff.append("<frt_term><![CDATA[" + frtTerm.trim() + "]]></frt_term>");
          xmlBuff.append("<dlv_term><![CDATA[" + deliveryTerm + "]]></dlv_term>");
          xmlBuff.append("</Detail1>");

          sql = "select * from pob_det where tran_id=? ";
          pstmt = conn.prepareStatement(sql);
          pstmt.setString(1, tranId);
          rs = pstmt.executeQuery();
          while (rs.next())
          {
            linenoStr = rs.getString("line_no");
            itemCode = rs.getString("item_code") == null ? " " : rs.getString("item_code");
            rate = rs.getString("rate") == null ? " " : rs.getString("rate");
            quantityStr = rs.getString("quantity") == null ? "0" : rs.getString("quantity");

            totQty = rs.getString("tot_qty") == null ? " " : rs.getString("tot_qty");
            discount = rs.getString("discount") == null ? " " : rs.getString("discount");
            netAmt = rs.getString("net_amt") == null ? " " : rs.getString("net_amt");
            if ((quantityStr != null) && (quantityStr.trim().length() > 0))
            {
              quantity = Double.parseDouble(quantityStr);
            }
            if ((rate != null) && (rate.trim().length() > 0))
            {
              spRate = Double.parseDouble(rate);
            }
            System.out.println("SpRate is:[" + spRate + "]");
            if (quantity > 0.0D)
            {
              lineNo++;
              lineNoStr = "";
              lineNoStr = "   " + lineNo;
              lineNoStr = lineNoStr.substring(lineNoStr.length() - 3, lineNoStr.length());
              System.out.println("lineNoStr[" + lineNoStr + "]");

              xmlBuff.append("<Detail2 dbID='' domID='" + lineNo + "' objName=\"sorder\" objContext=\"2\">");

              xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" updateFlag=\"A\" status=\"N\" />");

              xmlBuff.append("<sale_order/>");
              xmlBuff.append("<remarks><![CDATA[generated from POB transaction " + tranId + "]]></remarks>");
              xmlBuff.append("<line_no><![CDATA[" + lineNoStr + "]]></line_no>");
              xmlBuff.append("<item_code__ord><![CDATA[" + itemCode.trim() + "]]></item_code__ord>");
              xmlBuff.append("<item_code><![CDATA[" + itemCode.trim() + "]]></item_code>");
              xmlBuff.append("<quantity><![CDATA[" + quantity + "]]></quantity>");
              xmlBuff.append("<quantity__fc><![CDATA[" + quantity + "]]></quantity__fc>");
              xmlBuff.append("<rate><![CDATA[" + spRate + "]]></rate>");
              xmlBuff.append("<rate__clg><![CDATA[" + spRate + "]]></rate__clg>");
              xmlBuff.append("<net_amt><![CDATA[" + netAmt + "]]></net_amt>");
              xmlBuff.append("<item_ser><![CDATA[" + itemSer.trim() + "]]></item_ser>");
              xmlBuff.append("<discount><![CDATA[0]]></discount>");
              xmlBuff.append("<item_flg><![CDATA[I]]></item_flg>");
              xmlBuff.append("<CHG_USER><![CDATA[" + userId + "]]></CHG_USER>");
              xmlBuff.append("<CHG_TERM><![CDATA[" + termId + "]]></CHG_TERM>");
              xmlBuff.append("<chg_date><![CDATA[" + currDate + "]]></chg_date>");
              xmlBuff.append("<nature><![CDATA[C]]></nature>");
              xmlBuff.append("</Detail2>");
            }

          }

          rs.close();
          rs = null;
          pstmt.close();
          pstmt = null;
          xmlBuff.append("</Header0>");
          xmlBuff.append("</group0>");
          xmlBuff.append("</DocumentRoot>");
          xmlString = xmlBuff.toString();
          System.out.println("@@@@@2: xmlString Checking:" + xmlBuff.toString());
          System.out.println("...............just before savdata()");
          siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
          System.out.println("== site code ==" + siteCode);
          retString = saveData(siteCode, xmlString, conn);
          System.out.println("@@@@@2: retString:" + retString);
          System.out.println("--retString finished--");

          if (retString.indexOf("Success") > -1)
          {
            sql = " update pob_hdr set confirmed = 'Y', conf_date = ?, emp_code__aprv = ?, wf_status='C', status_date= ? where tran_id = ? ";
            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, currDate);
            pstmt.setString(2, loginEmpCode);
            pstmt.setTimestamp(3, currDate);
            pstmt.setString(4, tranId);

            cnt = pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            System.out.println("@@@@@@ cnt...[" + cnt + "]");
            if (cnt > 0)
            {
              conn.commit();
              errString = itmDBAccessLocal.getErrorString("", "VTCNFSUCC", "");
            }
            else {
              errString = itmDBAccessLocal.getErrorString("", "VTNCONFT", "");
            }
          }
        }
        else {
          errString = itmDBAccessLocal.getErrorString("", "VTINVCONF2", "");
        }

      }

    }
    catch (Exception e)
    {
      if (conn != null) {
        try
        {
          conn.rollback();
        }
        catch (SQLException ex) {
          e.printStackTrace();
          throw new ITMException(e);
        }
      }
      e.printStackTrace();
      throw new ITMException(e);
    }
    finally
    {
      try
      {
        if ((conn != null) && (!conn.isClosed()))
        {
          conn.close();
          conn = null;
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

      }
      catch (Exception e)
      {
        System.out.println("Exception : " + e); e.printStackTrace();
        throw new ITMException(e);
      }
    }
    return errString;
  }

  private String checkNull(String input) {
    if (input == null)
    {
      input = "";
    }
    return input;
  }

  private String saveData(String siteCode, String xmlString, Connection conn) throws ITMException {
    System.out.println("saving data...........");
    InitialContext ctx = null;
    String retString = null;
    MasterStatefulLocal masterStateful = null;
    try
    {
      AppConnectParm appConnect = new AppConnectParm();
      ctx = new InitialContext(appConnect.getProperty());
      masterStateful = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local");
      System.out.println("-----------masterStateful------- " + masterStateful);
      String[] authencate = new String[2];
      authencate[0] = "";
      authencate[1] = "";
      System.out.println("xmlString to masterstateful [" + xmlString + "]");
      retString = masterStateful.processRequest(authencate, siteCode, true, xmlString, true, conn);
      System.out.println("--retString - -" + retString);
    }
    catch (ITMException itme)
    {
      System.out.println("ITMException :CreateDistOrder :saveData :==>");
      throw itme;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.out.println("Exception :CreateDistOrder :saveData :==>");
      throw new ITMException(e);
    }
    return retString;
  }

  public String confirm(String tranId, String xtraParams, String forcedFlag)
    throws RemoteException, ITMException
  {
    System.out.println(">>>>>>>>>>>POBWizSRConf called for submit>>>>>>>>>>>>");
    String sql = ""; String wfStatus = ""; String confirmed = "";
    Connection conn = null;
    PreparedStatement pstmt = null;
    String errString = null;
    ResultSet rs = null;
    int updCount = 0;
    boolean isError = false;
    ITMDBAccessEJB itmDBAccessLocal = new ITMDBAccessEJB();
    try
    {
      ConnDriver connDriver = null;
      connDriver = new ConnDriver();
      //conn = connDriver.getConnectDB("DriverITM");
	  conn = getConnection();

      conn.setAutoCommit(false);
      System.out.println(">>>tranId:" + tranId);

      if ((tranId != null) && (tranId.trim().length() > 0))
      {
        System.out.println(">>>tranId:" + tranId);
        sql = "\tselect wf_status, confirmed from pob_hdr where tran_id = ? ";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, tranId);
        rs = pstmt.executeQuery();
        if (rs.next())
        {
          wfStatus = rs.getString("wf_status");
          confirmed = rs.getString("confirmed");
        }
        rs.close();
        rs = null;
        pstmt.close();
        pstmt = null;

        System.out.println(">>>Check wfStatus:" + wfStatus);
        if ("S".equalsIgnoreCase(wfStatus))
        {
          errString = itmDBAccessLocal.getErrorString("", "VTINVSUB2", "");
          isError = true;
        }
        else if ("Y".equalsIgnoreCase(confirmed))
        {
          errString = itmDBAccessLocal.getErrorString("", "VTINVCONF2", "");
          isError = true;
        }
        else
        {
          System.out.println(">>>>Before Calling method invokeWorkflow tranId:" + tranId);
          errString = invokeWorkflow(conn, tranId, xtraParams, forcedFlag);
          System.out.println(">>>>From invokeWorkflow errString:" + errString);

          if ("success".equalsIgnoreCase(errString))
          {
            sql = " update pob_hdr set wf_status = 'S' where tran_id = ? ";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, tranId);
            updCount = pstmt.executeUpdate();
            pstmt.close();
            pstmt = null;
            System.out.println(">>>After Workflow init Success updCount:" + updCount);
            if (updCount > 0)
            {
              errString = itmDBAccessLocal.getErrorString("", "VTPOBSUBM", "");
            }
            else
            {
              errString = itmDBAccessLocal.getErrorString("", "SUBMITFAIL", "");
              isError = true;
            }
          }
          else
          {
            errString = itmDBAccessLocal.getErrorString("", "SUBMITFAIL", "");
            isError = true;
          }
        }
      }

    }
    catch (Exception e)
    {
      if (conn != null)
      {
        isError = true;
        try
        {
          conn.rollback();
        }
        catch (SQLException ex)
        {
          e.printStackTrace();
          throw new ITMException(e);
        }
      }
      e.printStackTrace();
      throw new ITMException(e);
    }
    finally
    {
      try {
        System.out.println(">>>Before Closing Connection in POBWizConf finally");

        System.out.println("In finally Check isError:" + isError);
        if (!isError)
        {
          conn.commit();
          System.out.println(">>>In if commit successfuly");
        }
        else
        {
          conn.rollback();
          System.out.println(">>>In else rollback successfuly");
        }
        if (conn != null)
        {
          conn.close();
          conn = null;
          System.out.println(">>>Close Connection Successfuly in confirm()");
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

  private String invokeWorkflow(Connection conn, String tranId, String xtraParams, String hdrEdiFlag)
    throws ITMException
  {
    String winName = "w_pob_view_sr";
    GenericUtility genericUtility = null;
    Document domAll = null;
    NodeList nodeList = null;
    Node node = null;
    Element nodeElement = null;
    String sql = "";
    PreparedStatement pStmt = null;
    ResultSet rs = null;
    String retString = "";
    String objName = "pob_view_sr";
    String wrkflwInit = "";
    String refSer = "";
    String nodeName = "";
    try
    {
      System.out.println(">>>>invokeWorkflow method");

      XML2DBEJB xml2dbObj = new XML2DBEJB();
      genericUtility = GenericUtility.getInstance();
      GenerateXmlFromDB generateXmlFromDB = new GenerateXmlFromDB();
      String retXml = generateXmlFromDB.getXMLData(winName, tranId, conn, true);
      System.out.println(">>>In invokeWorkflow retXml:" + retXml);
      retXml = retXml.replace("<Root>", "");
      retXml = retXml.replace("</Root>", "");
      if ((retXml != null) && (retXml.trim().length() > 0))
      {
        domAll = genericUtility.parseString(retXml);
      }
      nodeList = domAll.getElementsByTagName("Detail1");
      node = nodeList.item(0);
      if (node != null)
      {
        objName = node.getAttributes().getNamedItem("objName").getNodeValue();
        nodeList = node.getChildNodes();
        int nodeListLength = nodeList.getLength();
        for (int i = 0; i < nodeListLength; i++)
        {
          node = nodeList.item(i);
          if (node != null)
          {
            nodeName = node.getNodeName();
          }

          if ("wf_status".equalsIgnoreCase(nodeName))
          {
            if (node.getFirstChild() != null)
            {
              node.getFirstChild().setNodeValue("S");
            }
            else
            {
              nodeElement = (Element)node;
              nodeElement.appendChild(domAll.createCDATASection("S"));
            }
          }
        }
      }
      nodeList = domAll.getElementsByTagName("DocumentRoot");
      node = nodeList.item(0);

      sql = "SELECT WRKFLW_INIT,REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = 'w_" + objName + "'";
      pStmt = conn.prepareStatement(sql);
      rs = pStmt.executeQuery();
      if (rs.next())
      {
        wrkflwInit = rs.getString("WRKFLW_INIT") == null ? "" : rs.getString("WRKFLW_INIT");
        refSer = rs.getString("REF_SER") == null ? "" : rs.getString("REF_SER");
      }
      if (rs != null)
      {
        rs.close();
        rs = null;
      }
      if (pStmt != null)
      {
        pStmt.close();
        pStmt = null;
      }
      String entityCodeInit = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
      retString = xml2dbObj.invokeWorkflowExternal(domAll, entityCodeInit, wrkflwInit, objName, refSer, tranId, conn);
      System.out.println(">>>retString From xml2dbObj.invokeWorkflowExternal:" + retString);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      try
      {
        if (rs != null)
        {
          rs.close();
          rs = null;
        }
        if (pStmt != null)
        {
          pStmt.close();
          pStmt = null;
        }
      }
      catch (SQLException sqlEx)
      {
        System.out.println("Exception in Finally " + sqlEx.getMessage());
        sqlEx.printStackTrace();
      }
    }
    finally
    {
      try
      {
        if (rs != null)
        {
          rs.close();
          rs = null;
        }
        if (pStmt != null)
        {
          pStmt.close();
          pStmt = null;
        }
      }
      catch (SQLException sqlEx)
      {
        System.out.println("Exception in Finally " + sqlEx.getMessage());
        sqlEx.printStackTrace();
      }
    }
    return "success";
  }
}