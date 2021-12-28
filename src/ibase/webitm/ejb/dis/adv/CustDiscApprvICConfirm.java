package ibase.webitm.ejb.dis.adv;
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import java.rmi.RemoteException;
import java.util.*;
import java.text.*;
import java.sql.*;
import javax.ejb.*;

import java.util.Date;
import javax.naming.InitialContext;
import ibase.system.config.AppConnectParm;
import org.json.JSONArray;
import org.json.JSONObject;



@Stateless // added for ejb3
public class CustDiscApprvICConfirm extends ActionHandlerEJB implements CustDiscApprvICConfirmRemote,CustDiscApprvICConfirmLocal

{
	E12GenericUtility genericUtility = new E12GenericUtility();
	String errorString = null;
	String userId ="";
	public String actionHandler() throws RemoteException,ITMException
	{
		System.out.println("actionHandler() Method Called....");
		return "";
	}

	public String confirm(String xmlString, String xtraParams, String objContext) throws RemoteException,ITMException
	{
		String  retString = null;
		System.out.println("Xtra Params : " + xtraParams);
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		Connection conn = null;
		System.out.println("XmlString>>>>"+xmlString);
		try
		{
			{
				retString = actionConfirm(xmlString, xtraParams);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :actionHandler(String xmlString):" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from  actionHandler"+retString);
		return (retString);
	}
	private String actionConfirm(String tranID, String xtraParams) throws RemoteException,ITMException, Exception
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1=null;
		ResultSet rs = null;
		int cnt = 0,count1 = 0;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		ConnDriver connDriver = new ConnDriver();
		String sql = "";
		String userId ="",loginEmpCode="";
		String applBasis="";
		userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		String siteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
		loginEmpCode = GenericUtility.getInstance().getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
		java.sql.Timestamp currDate = null;
		String returnString = null;
		String custCode="";
		String status=""; //added by kailasg on 23 feb  
		int count=0;
		//added by kailasg for generation of sale contract
		StringBuffer xmlString = new StringBuffer();
		String discType="";
		String sysDateStr = "";
		ResultSet rs1=null;
	    String pricelist ="";
	    String itemser ="";
	    String remarks="";
	    int domId=0;
		int lineNo=0;
		String itemCode="";
		double offerRate=0.0;
		double quantity=0.0;
		String retString="";
		MasterStatefulLocal masterStatefulLocal = null;
		String [] authencate = new String[2];
		authencate[0] = "";
		authencate[1] = "";
		String validuptostr ="";
		Timestamp validupto=null;
		String effectivestr ="";
		Timestamp effectivedate=null;
		 String  custName="",instDescr="",instCode="",crTerm="";
		 String currCode="";
		try
		{
			authencate[0] = userId;
			authencate[1] = "";
			AppConnectParm appConnect = new AppConnectParm();
			Properties p = appConnect.getProperty();
			InitialContext ctx = new InitialContext(p);

			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Date sysDate = new Date();
			sysDateStr = sdf.format(sysDate);
			conn = getConnection();

			conn.setAutoCommit(false);
			connDriver = null;

			sql = "SELECT count(1) from disc_apr_strg "
					+" where tran_id = ? and confirmed='Y' ";

			pstmt = conn.prepareStatement( sql );
			pstmt.setString(1,tranID);
			rs = pstmt.executeQuery();
			if( rs.next() )
			{
				cnt = rs.getInt( 1 );
			} 
			System.out.println(" COUNT =====> [" + cnt + "]");
			System.out.println("applBasis>>>"+applBasis);

			if(cnt== 0)
			{	
				sql="select appl_basis from disc_apr_strg where tran_id=?";
				pstmt = conn.prepareStatement( sql );
				pstmt.setString(1,tranID);
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					applBasis = rs.getString("appl_basis");
					System.out.println("applBasis>>>"+applBasis);
				} 
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;

				}
				if ("I".equalsIgnoreCase(applBasis))
				{
					sql = "SELECT count(1) from disc_apr_strg_det "
							+" where tran_id = ?";

					pstmt = conn.prepareStatement( sql );
					pstmt.setString(1,tranID);
					rs = pstmt.executeQuery();
					if( rs.next() )
					{
						count1 = rs.getInt( 1 );
					} 
					if (rs != null) {
						rs.close();
						rs = null;
					}
					if (pstmt != null) {
						pstmt.close();
						pstmt = null;

					}
					System.out.println(" COUNT =====> [" + count1 + "]");
					if(count1 == 0)
					{
						returnString = itmDBAccessEJB.getErrorString("","VTEMTITM",userId,"",conn);
						return returnString;
					}


				}

				sql = "SELECT status from disc_apr_strg "
						+" where tran_id = ?";
				pstmt = conn.prepareStatement( sql );
				pstmt.setString(1,tranID);
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					status = rs.getString( "status" );
				} 
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

				if("C".equalsIgnoreCase(status)) 
				{
					System.out.println("The Selected transaction is already confirmed");
					returnString = itmDBAccessEJB.getErrorString("", "VTYX ", "", "", conn);
					return returnString;
				}     
             
				// adding code by kailasg on 22- march generate sale contract for special rate case...start
				sql = "SELECT discount_type from disc_apr_strg  where tran_id = ?";
				pstmt = conn.prepareStatement( sql );
				pstmt.setString(1,tranID);
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
					discType = rs.getString( "discount_type" );
				} 
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;

			if("R".equalsIgnoreCase(discType)) 
				{
				
				sql = "SELECT cust_code,item_ser,remarks,valid_upto,eff_from,inst_code from disc_apr_strg  where tran_id = ?";
				pstmt = conn.prepareStatement( sql );
				pstmt.setString(1,tranID);
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
				    System.out.println("validupto 1 "+validupto);
					domId++;
					custCode=rs.getString("cust_code");
					itemser=rs.getString("item_ser");
					remarks=rs.getString("remarks");
					validupto=rs.getTimestamp("valid_upto");
					effectivedate=rs.getTimestamp("eff_from");
					instCode=rs.getString("inst_code");
		
				}
				rs.close();
			    rs = null;
			    pstmt.close();
			    pstmt = null;
			
			    sql = "SELECT CR_TERM ,curr_code FROM Customer where cust_code= ?";
				pstmt = conn.prepareStatement( sql );
				pstmt.setString(1,custCode);
				rs = pstmt.executeQuery();
				if( rs.next() )
				{
				    
				    crTerm=rs.getString("CR_TERM");
				    currCode=rs.getString("curr_code");
				}
				rs.close();
			    rs = null;
			    pstmt.close();
			    pstmt = null;
			    
			    System.out.println("crTerm 1 "+crTerm + "currCode "+currCode);
			    
			    System.out.println("validupto  "+validupto);
			    System.out.println("effectivedate  "+effectivedate);
			    
			    sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			   
			    System.out.println("after sdf validupto  "+effectivestr);
			    System.out.println("after sdf effectivedate  "+validuptostr);
			    
			    
			        xmlString.append("<DocumentRoot><description>Datawindow Root</description><group0><description>Group0 escription</description>");
					xmlString.append("<Header0>");
					xmlString.append("<description>Header0 members</description>");
					xmlString.append("<objName><![CDATA[").append("scontract").append("]]></objName>");
					xmlString.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
					xmlString.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
					xmlString.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
					xmlString.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
					xmlString.append("<action><![CDATA[").append("save").append("]]></action>");
					xmlString.append("<elementName><![CDATA[").append("").append("]]></elementName>");
					xmlString.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
					xmlString.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
					xmlString.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
					xmlString.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
					xmlString.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");

					xmlString.append("<Detail1 dbID='' domID=\"1\" objName=\"scontract\" objContext=\"1\">");
					xmlString.append("<attribute pkNames=\"contract_no:\" status=\"N\" updateFlag=\"A\" selected=\"N\" />");
					xmlString.append("<contract_no/>");
					xmlString.append("<contract_date><![CDATA[").append(sysDateStr).append("]]></contract_date>");
					xmlString.append("<cust_code><![CDATA[").append(instCode).append("]]></cust_code>");//end
					xmlString.append("<cust_code__dlv><![CDATA[").append(custCode).append("]]></cust_code__dlv>");
					xmlString.append("<cust_code__bil><![CDATA[").append(custCode).append("]]></cust_code__bil>");
					xmlString.append("<remarks><![CDATA[").append(remarks).append("]]></remarks>");
					xmlString.append("<item_ser><![CDATA[").append(itemser).append("]]></item_ser>");
				
					if(effectivedate != null)
					{
						xmlString.append("<eff_from>").append("<![CDATA[" + sdf.format(effectivedate) + "]]>").append("</eff_from>");
					}
					 System.out.println("after sdf validupto  "+effectivedate);
					if(validupto != null)
					{
						xmlString.append("<valid_upto>").append("<![CDATA[" + sdf.format(validupto) + "]]>").append("</valid_upto>");
					}
					 System.out.println("after sdf validupto  "+validupto);
					xmlString.append("<cr_term><![CDATA[").append(crTerm).append("]]></cr_term>");
					xmlString.append("<curr_code><![CDATA[").append(currCode).append("]]></curr_code>");
					xmlString.append("</Detail1>");

					//detail 1----------
					System.out.println("xmlString For generation after append xml for detail1  "+xmlString.toString());
					
					List<JSONObject> arList = new ArrayList<JSONObject>();
					 sql = "SELECT line_no,item_code,offer_rate,max_quantity from disc_apr_strg_det  where tran_id = ?";
						pstmt = conn.prepareStatement( sql );
						pstmt.setString(1,tranID);
						rs = pstmt.executeQuery();
			        while( rs.next() )
			        {

							JSONObject jObj=new JSONObject();
							jObj.put("line_no",rs.getInt("line_no"));
							jObj.put("item_code",rs.getString("item_code"));
							jObj.put("offer_rate",rs.getDouble("offer_rate"));
							jObj.put("max_quantity",rs.getDouble("max_quantity"));
							arList.add(jObj);
						}
							
						System.out.println("arList......"+arList);
						rs.close();
					    rs = null;
					    pstmt.close();
					    pstmt = null;

					for(JSONObject js : arList)
					{
						
						xmlString.append("<Detail2 dbID=':' domID='" + domId + "' objName='scontract' objContext='2'>");
						xmlString.append("<attribute pkNames='contract_no:line_no:' status='N' updateFlag='A' selected='N'/>");
						xmlString.append("<contract_no/>");
						
						xmlString.append("<item_code><![CDATA[").append(js.getString("item_code").trim()).append("]]></item_code>");
						xmlString.append("<rate><![CDATA[").append(js.getDouble("offer_rate")).append("]]></rate>");
						xmlString.append("<quantity><![CDATA[").append(js.getDouble("max_quantity")).append("]]></quantity>");
						xmlString.append("<rate__stduom><![CDATA[").append(js.getDouble("offer_rate")).append("]]></rate__stduom>");
						xmlString.append("<quantity__stduom><![CDATA[").append(js.getDouble("max_quantity")).append("]]></quantity__stduom>");
						xmlString.append("</Detail2>");
						
						
					}
					System.out.println("xmlString For generation after append xml for detail 2:: "+xmlString.toString());
		
		
				String tranIdscontract="";

				xmlString.append("</Header0></group0></DocumentRoot>");
				System.out.println("xmlString For generation sale contract:: "+xmlString.toString()+"scontract"+tranID);
				System.out.println("estimation--testing");
				
					masterStatefulLocal = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local"); 
					System.out.println("masterstatement process request....qqq:: "+masterStatefulLocal);
					retString = masterStatefulLocal.processRequest( authencate, siteCode, true, xmlString.toString(),false,conn);
					System.out.println("retString....qqq:: "+retString);
					
					if ( retString.indexOf("Success") > -1 && retString.indexOf("<TranID>") > 0 )
					{

						tranIdscontract = retString.substring( retString.indexOf("<TranID>")+8, retString.indexOf("</TranID>"));
						System.out.println("tranId is :"+tranIdscontract);
						//return retString;
					}
				}
			
			System.out.println("before update confirm transaction  :");
			// adding code by kailasg on 22- march generate sale contract for special rate case...	//end
				sql = " Update disc_apr_strg set "
						+ " confirmed = 'Y',"
						+ " wf_status = 'C' ,"
						+ " conf_date = ? ,"
						+ " emp_code__aprv = ? "
						+ " where tran_id = ? " ;
				System.out.println("sql....."+sql);

				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setTimestamp(1,getCurrdateAppFormat() );
				pstmt1.setString(2,loginEmpCode);
				pstmt1.setString(3,tranID);
				count=pstmt1.executeUpdate();
				if (pstmt1 != null) {
					pstmt1.close();
					pstmt1 = null;

				}
				System.out.println("confirm updated -------->>>>>>>>>updCount :"+count);
				//conn.commit();
				returnString = itmDBAccessEJB.getErrorString("","CONFSUCC",userId,"",conn);
				//System.out.println("Commit successful");
				return returnString;

			}
			else
			{
				returnString = itmDBAccessEJB.getErrorString("","VTMCONF1",userId,"",conn);
				
				
			}

			/*if(returnString.indexOf("CONFSUCC") != -1)
			{
				conn.commit();
			}*/
			if ( rs != null )
			{
				rs.close();
				rs = null;
			}	
		}
		catch(Exception e)
		{
			System.out.println("DiscountConfirmEJB..."+e.getMessage());
			e.printStackTrace();
			try
			{
				conn.rollback();
			}
			catch(Exception e1)
			{
				System.out.println("DiscountConfirmEJB..."+e1.getMessage());
				e1.printStackTrace();
			}
		}
		finally
		{
			try
			{
				conn.commit();
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
				}
				if(conn != null)
				{
					conn.close();
					conn = null;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception DiscountConfirmEJB....... :\n"+e.getMessage());
			}
		}
		System.out.println("retString ::"+returnString);
		return returnString;
	}
	private Timestamp getCurrdateAppFormat()
	{
		Timestamp timestamp = null;		
		try
		{
			java.util.Date date = null;
			timestamp = new Timestamp(System.currentTimeMillis());

			SimpleDateFormat simpledateformat = new SimpleDateFormat(genericUtility.getDBDateFormat());
			date = simpledateformat.parse(timestamp.toString());
			timestamp = Timestamp.valueOf(simpledateformat.format(date).toString() + " 00:00:00.0");
		}
		catch(Exception exception)
		{
			System.out.println("Exception in  getCurrdateAppFormat " + exception.getMessage());
		}
		return timestamp;
	}
}