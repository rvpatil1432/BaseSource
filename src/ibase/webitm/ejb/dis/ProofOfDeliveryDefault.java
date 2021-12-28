package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ActionHandlerEJB;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless
public class ProofOfDeliveryDefault extends ActionHandlerEJB implements ProofOfDeliveryDefaultLocal,ProofOfDeliveryDefaultRemote
{
		static int count=0;
	  public String actionHandler()  throws RemoteException,ITMException
	  {
	    return "";
	  }
	  public static ProofOfDeliveryDefault getInstance(){
		  return new ProofOfDeliveryDefault();
	  }
	  public String actionHandler(String actionType, String xmlString, String objContext, String xtraParams) throws RemoteException, ITMException
	  {
		  System.out.println("--------- ProofOfDeliveryDefault111-----------------actionType"+actionType);
		  Document dom=null;
		  String str=null;
		  System.out.println("actionType--->>"+actionType);
		  System.out.println("xmlString---"+xmlString);
		  System.out.println("Porder Value===="+objContext);
		  System.out.println("xtraParams---"+xtraParams);
		  try
		  {
		      E12GenericUtility genutility= new E12GenericUtility();
			  if((xmlString!=null) && (xmlString.trim().length()!=0))
			  {
			     System.out.println("xmlString from Servlet Side=="+xmlString);
				 dom=genutility.parseString(xmlString);
			  }
			  if(actionType.equalsIgnoreCase("Items"))
			  {
			     str=getItems(dom,objContext,xtraParams);
				 
			  }
	
		  }catch(Exception e)
		  {
		  System.out.println("Exception :Porder :actionHandler(String xmlString):" + e.getMessage() + ":");
		      throw new ITMException(e);
		  }
		  return str;
	  }
	  
	  private String getItems(Document dom,String objContext,String xtraParams) throws ITMException
	  {
		  ResultSet rs=null,rs1=null;
			Connection conn=null;
			ConnDriver ConnDriver = new ConnDriver();
			PreparedStatement pstmt=null,pstmt1=null;
			NodeList parentNodeList = null, childNodeList = null;
			Set lineNoSet=new HashSet();
			Node parentNode = null, childNode = null;
			String retString="",sql="",invoiceID="",lotNo="",lotSl="",itemSerPorm="",itemCode="",itemCodeDesc="",locCode="";
			int invLineNo=0;
			double rate=0.0,quantity=0.0,discount=0.0,pendQty=0;
			String errString="",childNodeName="",lineNo1="",despachID="",approveRate="";
			int updCnt=0;
			HashMap<String,String> domDetail=new HashMap<String,String>();
		  	StringBuffer valueXmlBuff = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root>\r\n");
		    E12GenericUtility genutility = new E12GenericUtility();	    
		 
		    try
		    {
		    	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
				ConnDriver connDriver = null;
				connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 13-06-2016 :START
				  //conn = connDriver.getConnectDB("DriverValidator");
				  conn = getConnection();
				  //Changes and Commented By Bhushan on 13-06-2016 :END
				conn.setAutoCommit(false);
				invoiceID = genutility.getColumnValue("invoice_id", dom);
				System.out.println("------invoiceID--------- :"+invoiceID);
				
				parentNodeList = dom.getElementsByTagName("Detail2");
				System.out.println("parent node length 111---->:"+parentNodeList.getLength());
				for (int i = 0; i < parentNodeList.getLength(); i++)
				{
				    parentNode = parentNodeList.item(i);
				    childNodeList = parentNode.getChildNodes();
				System.out.println("childNodeList139---->:"+childNodeList.getLength());
				   for (int j = 0; j < childNodeList.getLength(); j++)
				   {
					childNode = childNodeList.item(j);
					childNodeName = childNode.getNodeName();
					System.out.println("ChildNodeName ------>> : "+childNodeName);
					 if ("line_no".equalsIgnoreCase(childNodeName))
					 {
						 System.out.println("---------record exist---------------");
						 lineNo1 = childNode.getFirstChild() == null ? "" : childNode.getFirstChild().getNodeValue();
						 System.out.println("LineNoSet adding value itemCode--->>"+lineNo1);
						 int line=Integer.parseInt(lineNo1);
						 System.out.println("Line number  int :"+line);
						 if(line  > 1)
						 {
							 System.out.println("returning value before appending data---------");
							 lineNoSet.add(lineNo1);
							 //return retString;
						 }
							 
						 
					 }
					
				   }
				}
				System.out.println("Size of line number -->>: "+lineNoSet.size());
				
				if(objContext !=null)
				{
//					sql="select rate,quantity,lot_no,lot_sl,item_ser__prom,item_code,inv_line_no,desp_id,discount " +
//							"from invoice_trace where invoice_id = ? order by line_no";								VALLABH KADAM 23/DEC/14
																													// SELECT [line_no] on place of [inv_line_no]
					
					sql="select rate,quantity,lot_no,lot_sl,item_ser__prom,item_code,line_no,desp_id,discount" +
							" from invoice_trace where invoice_id = ? order by line_no";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, invoiceID);
					rs=pstmt.executeQuery();
					int lineNo=0;
					while(rs.next())
					{
						lineNo++;
						rate=rs.getDouble(1);
						quantity=rs.getDouble(2);
						lotNo=rs.getString(3);
						lotSl=rs.getString(4);
						itemSerPorm=rs.getString(5);
						itemCode=rs.getString(6);
						invLineNo=rs.getInt(7);													//VALLABH KADAM 22/DES/14 select invLineNo = [line_no]
						despachID=rs.getString(8);
						discount=rs.getDouble(9);
						lotNo=lotNo ==null ? "" : lotNo.trim();
						lotSl=lotSl ==null ? "" : lotSl.trim();
						itemSerPorm=itemSerPorm ==null ? "" : itemSerPorm.trim();
						itemCode=itemCode ==null ? "" : itemCode.trim();
						despachID=despachID ==null ? "" : despachID.trim();
						System.out.println("Line No in while ----->> "+lineNo);
						System.out.println("despachID ------>> "+despachID);
						System.out.println("discount ------>> "+discount);
						if(!(lineNoSet.contains(lineNo1)))
						{
							System.out.println("lineNo Set condition true----------");
							valueXmlBuff.append("<Detail>\r\n");					
							valueXmlBuff.append("<line_no>").append("<![CDATA["+lineNo+"]]>").append("</line_no>");
							valueXmlBuff.append("<line_no__trace>").append("<![CDATA["+invLineNo+"]]>").append("</line_no__trace>");
							if(discount > 0){
								rate=rate - (rate * discount/100);
								System.out.println("New rate after discount------>> "+rate+"]");
							}
							valueXmlBuff.append("<rate__inv>").append("<![CDATA["+rate+"]]>").append("</rate__inv>");
							approveRate=getApprovedRate(conn,itemCode,rate);
							System.out.println("approveRate final ------>> "+approveRate);
							
							valueXmlBuff.append("<aprv_rate>").append("<![CDATA["+approveRate+"]]>").append("</aprv_rate>");
							valueXmlBuff.append("<quantity__inv>").append("<![CDATA["+quantity+"]]>").append("</quantity__inv>");
							
							valueXmlBuff.append("<lot_no>").append("<![CDATA["+lotNo+"]]>").append("</lot_no>");
							valueXmlBuff.append("<lot_sl>").append("<![CDATA["+lotSl+"]]>").append("</lot_sl>");
							valueXmlBuff.append("<item_ser__prom>").append("<![CDATA["+itemSerPorm+"]]>").append("</item_ser__prom>");
							valueXmlBuff.append("<item_code>").append("<![CDATA["+itemCode+"]]>").append("</item_code>");
							itemCodeDesc=getNameOrDescrForCode(conn, "item", "descr", "item_code", itemCode);
							System.out.println("---------EXECUTE query--------");
							sql="select loc_code from despatchdet where desp_id = ? and item_code = ?";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1,despachID);
							pstmt1.setString(2,itemCode);
							rs1 = pstmt1.executeQuery();
							System.out.println("-----query executed-----");
							if(rs1.next())
							{
								locCode = rs1.getString(1)==null ? "" :rs1.getString(1); 
							}
							locCode=locCode ==null ? "" : locCode.trim();
							System.out.println("Location code -------->> : "+locCode);							
							
							valueXmlBuff.append("<descr>").append("<![CDATA["+itemCodeDesc+"]]>").append("</descr>");
							valueXmlBuff.append("<loc_code>").append("<![CDATA["+locCode+"]]>").append("</loc_code>");
							
							
							domDetail.put("quantity__inv", String.valueOf(quantity));
							domDetail.put("lot_no", lotNo==null ? null :lotNo.trim());
							domDetail.put("lot_sl", lotSl==null ? null :lotSl.trim());
							domDetail.put("invoice_id", invoiceID==null ? null :invoiceID.trim());						
							domDetail.put("line_no__trace", String.valueOf(invLineNo));
							domDetail.put("item_code", itemCode==null ? "" :itemCode.trim());
							domDetail.put("loc_code", locCode);
							
							pendQty=getPendingQty(conn, domDetail); 
							System.out.println("Pending quantity----->>["+pendQty+"]");
							valueXmlBuff.append("<pend_qty>").append("<![CDATA["+pendQty+"]]>").append("</pend_qty>");
							valueXmlBuff.append("<pend_temp>").append("<![CDATA["+pendQty+"]]>").append("</pend_temp>");
							//
							valueXmlBuff.append("</Detail>\r\n");
							pstmt1.close();pstmt1=null;
							rs1.close();rs1=null;
							count++;
						}
						//select descr from item where item_code = ?
						System.out.println("------->>>>Line no :"+lineNo+" rate inv :"+rate+"quantity : "+quantity+" Lot No : "+lotNo+" lotSl : "+lotSl);
						System.out.println("------->>>>item_ser__prom : "+itemSerPorm+" itemCodeDesc:"+itemCodeDesc+" loc code "+locCode);
						itemCodeDesc="";locCode="";
					}
					
					pstmt.close();
					pstmt = null;
					rs.close();
					rs = null;
					valueXmlBuff.append("</Root>\r\n");
					retString = valueXmlBuff.toString();	
					
				}//end objContext				 
				
		    }
		    catch(SQLException se)
			{
				System.out.println("SQLException : class ProofOfDeliveryDefault : ");
				retString = genutility.createErrorString(se);
				se.printStackTrace();
				try
				{
				  conn.rollback();
				}
				catch(Exception e){
					System.out.println("Exception : Occure during rollback........");e.printStackTrace();
					}
				throw new ITMException(se); //Added By Mukesh Chauhan on 05/08/19
			}
			catch(Exception e)
			{
				System.out.println("Exception : class ProofOfDeliveryDefault : ");
				retString = genutility.createErrorString(e);
				e.printStackTrace();
				try
				{
					conn.rollback();
				}
				catch(Exception se){
					System.out.println("Exception : Occure during rollback........");
					se.printStackTrace();
					}
				throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
			}			
			finally
			{
			   try{
				    if (pstmt != null)
				    {
				    	pstmt.close();
						pstmt=null;
				    }
					if (rs !=null)
					{
						rs.close();
						rs=null;
					}
					if(conn!=null)
					{		
						conn.close();
						conn = null;
					}
					
			    }catch(Exception e)
				{
					System.out.println("inside Finally Exception ProofOfDeliveryDefault class:submit() ");
					e.printStackTrace();
				}

			}
		    System.out.println("Return String ..in deafault EJB ----------->>>>>>>>>>> \n" + retString);
			  return retString;
		
	  }
	  protected String getApprovedRate(Connection conn,String itemCode,double invRate)
	  {
		  String regPrice="",varValue="",varName="",apprRateS="";
		  double varValueD=0.0,apprRate=0.0;
		  regPrice=getNameOrDescrForCode(conn,"item","regulated_price","item_code",itemCode);
		  regPrice=regPrice==null ? "" : regPrice.trim();
		  System.out.println("Regulated Price---->>["+regPrice+"]");
		  if(regPrice.equalsIgnoreCase("Y")){
			  varName="AML_REG_PER";
			  varValue=getNameOrDescrForCode(conn,"disparm","var_value","var_name",varName);
			  varValue=varValue==null ? "" : varValue.trim();
			  varValueD=Double.parseDouble(varValue);
			  System.out.println("var Value---->>["+varValueD+"]");		  
			  apprRate=invRate * 100/varValueD;				  
		  }
		  else if(regPrice.equalsIgnoreCase("N")){
			  varName="AML_NONREG_PER";
			  varValue=getNameOrDescrForCode(conn,"disparm","var_value","var_name",varName);
			  varValue=varValue==null ? "0" : varValue.trim();			  
			  varValueD=Double.parseDouble(varValue);			 		
			  apprRate=invRate * 100/varValueD;	
			  
		  }
		  else if(regPrice.equalsIgnoreCase("D")){
			 varName="AML_DPCO_PER";
			  varValue=getNameOrDescrForCode(conn,"disparm","var_value","var_name",varName);		
			  varValue=varValue==null ? "0" : varValue.trim();	
			  varValueD=Double.parseDouble(varValue);
			  System.out.println("var value 'D1111'---->>["+apprRate+"]");
			  apprRate=invRate * 100/varValueD;		  
			  
		  }
		  else if(regPrice.equalsIgnoreCase("O")){
			 varName="AML_OTC_PER";
			  varValue=getNameOrDescrForCode(conn,"disparm","var_value","var_name",varName);		
			  varValue=varValue==null ? "0" : varValue.trim();	
			  varValueD=Double.parseDouble(varValue);
			  System.out.println("var value 'D1111'---->>["+apprRate+"]");
			  apprRate=invRate * 100/varValueD;		  
			  
		  }
		  
		  if(apprRate > 0){
			  DecimalFormat df=new DecimalFormat("#.###");
			  apprRateS=df.format(apprRate);
			  System.out.println("Formated Approved Rate---->>["+apprRateS+"]");			 
		  }
		  return apprRateS;
		 
	  }
	  
	  private String getNameOrDescrForCode(Connection conn, String table_name, String descr_col_name,String whrCondCol, String whrCondVal)
		{
				String descr = null;
				
				if(conn!=null){
					
					ResultSet rs=null;
					PreparedStatement pstmt = null;
					
					String sql="SELECT "+descr_col_name+" FROM "+table_name+" WHERE "+whrCondCol+" = ?";				
					System.out.println("SQL in getNameOrDescrForCode method : "+sql);
					try
					{
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,whrCondVal);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString(descr_col_name);
						}
					}
					catch(SQLException e)
					{
						System.out.println("SQL Exception In getNameOrDescrForCode method of ProofOfDelivery Class : "+e.getMessage());
						e.printStackTrace();
					}
					catch(Exception ex)
					{
						System.out.println("Exception In getNameOrDescrForCode method of ProofOfDelivery Class : "+ex.getMessage());
						ex.printStackTrace();
					}finally{
						
						try{
							
							if(pstmt!=null){
								pstmt.close();
								pstmt = null;
							}
							if(rs!=null){
								rs.close();
								rs = null;
							}
						}catch (SQLException se) {
							se.printStackTrace();
						}
					}
				}else{
					try {
						throw new SQLException("==========Connection is null in getNameOrDescrForCode method of ProofOfDeliveryDefault class===========");
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
					
			return descr;
		}
// Return Pending Quantity for Proof of Delivery	  
  protected double getPendingQty(Connection conn,HashMap<String,String> detailDom)
  {
			ResultSet rs=null;
			PreparedStatement pstmt=null;
			String sql="";
			double pendingQty=0,doneQty=0,invoiceQty=0;;
			try{
				sql="select sum(d.quantity__resale) from spl_sales_por_hdr h,spl_sales_por_det d "
						+ "where h.tran_id = d.tran_id and h.invoice_id = ? and "
						+ "d.lot_no=? and d.lot_sl = ? and d.item_code = ? and loc_code = ? "
						+ "and d.line_no__trace= ?";
				
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, detailDom.get("invoice_id"));
				pstmt.setString(2, detailDom.get("lot_no"));
				pstmt.setString(3, detailDom.get("lot_sl"));
				pstmt.setString(4, detailDom.get("item_code"));
				pstmt.setString(5, detailDom.get("loc_code"));
				pstmt.setInt(6, Integer.parseInt(detailDom.get("line_no__trace")));
				
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					doneQty=rs.getDouble(1);
					
				}
				invoiceQty=Double.parseDouble(detailDom.get("quantity__inv"));
				System.out.println("invoiceQty111--->>["+invoiceQty+"]");
				System.out.println("doneQty--->>["+doneQty+"]");
				pendingQty= invoiceQty - doneQty;
				
			}
			catch(Exception e)
			{
				System.out.println("Exception in getpendingQty : "+e.getMessage());
				e.printStackTrace();
			}
			
			return pendingQty;
		
		}
	  
}
