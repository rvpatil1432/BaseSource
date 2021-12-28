/**
 * @author CustStockGWTPostSave written for itemchange issue on closing stock by Saurabh Jarande [16/02/17] 
 *
 */
package ibase.webitm.ejb.dis;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ejb.Stateless;

import org.w3c.dom.Document;

@Stateless
public class CustStockGWTPostSave extends ValidatorEJB implements CustStockGWTPostSaveLocal,CustStockGWTPostSaveRemote {
	
	public String postSave(String xmlString,String tranId,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException
	{
		System.out.println("------------ postSave method called-111111111----------------CustStockGWTPostSave : ");
		System.out.println("tranId111--->>["+tranId+"]");
		System.out.println("xml String--->>["+xmlString+"]");
		Document dom = null;
		String errString="";
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);				
				errString = postSave(dom,tranId,xtraParams,conn);
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception : CustStockGWTPostSave.java : postSave : ==>\n"+e.getMessage());
			throw new ITMException(e);
		}		
		return errString;
	}	
	public String postSave(Document dom,String tranId,String xtraParams,Connection conn) throws ITMException, RemoteException
	{
		System.out.println("in CustStockGWTPostSave tran_id---->>["+tranId+"]");
		ResultSet rs=null;
		PreparedStatement pstmt=null;
		String sql="";
	    String errString = "",isValidCust="";
	    String invoiceId="",invoiceIdList="",selectedInvList="";
		ITMDBAccessEJB itmDBAccessEJB=new ITMDBAccessEJB();
		try
		{
			sql="select invoice_id from cust_stock_inv where tran_id=? and dlv_flg='Y' and ref_ser='S-INV' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery( );
			while( rs.next() )
			{
				invoiceId = rs.getString("invoice_id");
				invoiceIdList = invoiceIdList + "'"+invoiceId.trim() + "',";
			}
			callPstRs(pstmt, rs);
			if(invoiceIdList.trim().length() > 0)
			{
				selectedInvList = invoiceIdList.substring(0,invoiceIdList.length() - 1);
			}
			else
			{
				selectedInvList = "' '";
			}
			System.out.println("selectedInvList>>>>"+selectedInvList);
			//Added by saurabh for duplicate customer validation[10/03/17|Start] 
			isValidCust =checkValidCust(tranId,conn);
			if(isValidCust== null || isValidCust.trim().length()==0)
			{
				errString =updateCustStockDet(dom,tranId,selectedInvList,conn);
			}
			else
			{
				errString=isValidCust;
			}
			System.out.println("errString>>>>"+errString);
			//Added by saurabh for duplicate customer validation[10/03/17|End]
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			errString = itmDBAccessEJB.getErrorString("", "VTICFAIL","", "", conn);
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		finally
		{
			try
			{
				System.out.println(">>>>>In finally errString:"+errString);
				
				if(errString != null && errString.trim().length()>0 )
				{
					//errString = itmDBAccessEJB.getErrorString("", "VTICFAIL","", "", conn);
					return errString;
				}
			}
			catch(Exception e)
			{
				System.out.println("Exception : "+e);
				e.printStackTrace();
			}
		}
		return errString;
	}
	
	//Added by saurabh for duplicate customer validation[10/03/17|Start]
	private String checkValidCust(String tranId, Connection conn) throws ITMException {
		// TODO Auto-generated method stub
		String sql="",custCodeDom="",itemSerHd="",prdCode="",errString="";
		int custCnt=0;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		ITMDBAccessEJB itmDBAccessEJB =new ITMDBAccessEJB();
		try
		{
			sql="select cust_code,item_ser,prd_code from cust_stock where tran_id=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			if(rs.next()) 
			{
				custCodeDom = rs.getString("cust_code");
				itemSerHd = rs.getString("item_ser");
				prdCode = rs.getString("prd_code");
			}
			callPstRs(pstmt, rs);
			
			sql=" select count(*) as count from cust_stock where cust_code=? and item_ser=? and prd_code=? and tran_id!=? and status<>'X' and pos_code is not null ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, custCodeDom);
			pstmt.setString(2, itemSerHd);
			pstmt.setString(3, prdCode);
			pstmt.setString(4, tranId);
			rs = pstmt.executeQuery();
			if (rs.next()) 
			{
				custCnt = rs.getInt("count");
			}
			callPstRs(pstmt, rs);
			
			if(custCnt>0)
			{
				errString = itmDBAccessEJB.getErrorString("", "VMINVPRDCU","", "", conn);
			}
			else
			{
				errString="";
			}
		}
		catch(Exception e)
		{
			/*errString = e.getMessage();*///Commented By Mukesh Chauhan on 02/08/19
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		return errString;
	}
	//Added by saurabh for duplicate customer validation[10/03/17|End]
	
	private String updateCustStockDet(Document dom,String tranId,String selectedInvList, Connection conn) throws RemoteException, ITMException 
	{
		// TODO Auto-generated method stub
		E12GenericUtility genericUtility =new E12GenericUtility();
		String errString="";
		PreparedStatement pstmt,pstmt1 = null;
		ResultSet rs,rs1 =null;
		String clStockInp="",itemCode1="",custCodeDom="",itemSerHd="";
		String invoiceMonths="",sql="",priceList="",sysDate="",tarnIdLast="";
		String calEnablePrice="",calPriceDivision="";
		int invoiceMonthsPrevious=0,UpdCnt=0;
		double rcpQtmDom=0,rcpReplQtmDom=0,rcpFreeQtmDom=0,retQtyDom=0,retQtyFreeDom=0,opStkDom=0,rateOld=0,rcpValue=0,replValue=0,retValue=0,rcpFreeValue=0;
		double clStock=0,formulaValue=0,rateStd=0,quantityStd=0,closingValue=0,
				rateAll=0,closingRate=0,grossSecondaryQty=0,netSecondarySalesValue=0,grossSecondarySalesValue=0,
				grossSecondaryRate=0,salesQtyCal=0,clValOld=0,clStkOld=0,opValDom=0;
		boolean isClStockInt=false,isItemSerLocal=false;
		Timestamp thirdMonthDay=null,prdFromoDateTmstmp=null,prdtoDateTmstmp=null;
		//ArrayList calCriItemSerList=null;
		ibase.webitm.ejb.dis.DistCommon dist = new ibase.webitm.ejb.dis.DistCommon();
		UtilMethods utlmethd = new UtilMethods();
		Date currentDate = new Date();
		ITMDBAccessEJB itmDBAccessEJB =new ITMDBAccessEJB();
		try
		{
			invoiceMonths =  dist.getDisparams("999999","INVOICE_MONTHS",conn);
			System.out.println("invoiceMonths.." + invoiceMonths);
			if (("NULLFOUND".equalsIgnoreCase(invoiceMonths) || invoiceMonths == null || invoiceMonths.trim().length() == 0) )
			{
				 invoiceMonthsPrevious=-3;
			}else
			{
				invoiceMonthsPrevious=Integer.parseInt(invoiceMonths);
			}
			System.out.println("invoiceMonthsPrevious>>>>>"+invoiceMonthsPrevious);
			
			SimpleDateFormat sdf2 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			
			/*calCriItemSerStr =  dist.getDisparams("999999","CAL_CRIT_ITEMSER",conn);
			if (("NULLFOUND".equalsIgnoreCase(calCriItemSerStr) || calCriItemSerStr == null || calCriItemSerStr.trim().length() == 0) )
			{
				isItemSerLocal=false;
			}else
			{
				 calCriItemSerList= new ArrayList(Arrays.asList(calCriItemSerStr.split(",")));
			}
			System.out.println("isItemSer@@@@@@@after>>>>"+isItemSerLocal);*/
			
			sql=" select d.item_code,d.cl_stock,d.purc_rcp,d.purc_rcp__repl,d.purc_rcp__free,d.purc_ret,d.purc_ret__repl,d.op_stock,d.op_value, " +
					" c.cust_code,c.tran_id__last, " +
					" d.rcp_val,d.rcp_repl_val,d.ret_val,d.rcp_free_val,c.item_ser,c.order_type,c.from_date,c.to_date " +
					" from cust_stock_det d,cust_stock c" +
					" where d.tran_id=c.tran_id " +
					//" and d.cl_value=0 and d.rate=0 and d.cl_stock>0 and d.tran_id=? ";
					" and d.tran_id=? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				sysDate = sdf2.format(currentDate.getTime());
				itemCode1=checkNull(rs.getString("item_code"));
				clStockInp=checkNull(rs.getString("cl_stock"));
				rcpQtmDom=rs.getDouble("purc_rcp");
				rcpReplQtmDom=rs.getDouble("purc_rcp__repl");
				rcpFreeQtmDom=rs.getDouble("purc_rcp__free");
				retQtyDom=rs.getDouble("purc_ret");
				retQtyFreeDom=rs.getDouble("purc_ret__repl");
				opStkDom=rs.getDouble("op_stock");
				opValDom=rs.getDouble("op_value");
				custCodeDom=checkNull(rs.getString("cust_code"));
				tarnIdLast=checkNull(rs.getString("tran_id__last"));
				rcpValue=rs.getDouble("rcp_val");
				replValue=rs.getDouble("rcp_repl_val");
				retValue=rs.getDouble("ret_val");
				rcpFreeValue=rs.getDouble("rcp_free_val");
				itemSerHd = checkNull(rs.getString("item_ser"));
				System.out.println("@S@itemSerHd"+itemSerHd+"]");
				//orderType = checkNull(rs.getString("order_type"));
				prdFromoDateTmstmp = rs.getTimestamp("from_date");
				prdtoDateTmstmp = rs.getTimestamp("to_date");

				rateOld=0;//rateOrgOld=0;
				clValOld=0;clStkOld=0;//added 140617
				if(tarnIdLast.length()>0){
					sql = "	select CASE WHEN rate IS NULL THEN 0 ELSE rate END  as rate," +
							" CASE WHEN rate__org IS NULL THEN 0 ELSE rate__org END as rate__org," +
							" NVL(cl_value,0) as cl_value  ," +
							" NVL(cl_stock,0) as cl_stock  from " +
							" cust_stock_det where tran_id=? and item_code=? ";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1, tarnIdLast);
					pstmt1.setString(2, itemCode1);
					rs1 = pstmt1.executeQuery();
					if (rs1.next()) 
					{
						rateOld = Double.parseDouble(rs1.getString("rate"));
						//rateOrgOld = Double.parseDouble(rs1.getString("rate__org"));
						clValOld = Double.parseDouble(rs1.getString("cl_value"));
						clStkOld = Double.parseDouble(rs1.getString("cl_stock"));
					}
					callPstRs(pstmt1, rs1);
				}

			if(clStkOld>0)
			{
				opStkDom=clStkOld;
			}
			System.out.println("prdFromoDateTmstmp :"+prdFromoDateTmstmp);
			System.out.println("prdtoDateTmstmp :"+prdtoDateTmstmp);
			
			/*if(calCriItemSerStr.trim().length()>0)
			{
				System.out.println("calCriItemSerList.contains(itemSerHd.trim())"+calCriItemSerList.contains(itemSerHd.trim()));
				if(calCriItemSerList.contains(itemSerHd.trim()))
				{
					System.out.println("Inside ItemSer true::::["+calCriItemSerList.contains(itemSerHd.trim())+"]");
					isItemSerLocal=true;
				}
				else{
					System.out.println("Inside ItemSer false::::["+calCriItemSerList.contains(itemSerHd.trim())+"]");
					isItemSerLocal=false;
				}
			}
			else
			{
					System.out.println("isItemSer:::::"+isItemSerLocal);
			}*/
			
			//itemSerHd=getItemSerList(itemSerHd,conn);
			System.out.println("@S@ Group item series::"+itemSerHd);
			//itemSerHeaderSplit=itemSerHd;
			
			isClStockInt= isValidDouble(clStockInp);
			System.out.println("isClStockInt>>>>>>"+isClStockInt);
			if(isClStockInt && Double.parseDouble(clStockInp)>=0)
			{
				System.out.println("clStockInp :" + clStockInp);
				if (clStockInp != null)
				{
					clStock = Math.round(Double.parseDouble(clStockInp));
				} else
				{
					clStock = 0.0;
				}
					System.out.println("opStkDom :" + opStkDom);
					System.out.println("rcpQtmDom :" + rcpQtmDom);
					System.out.println("rcpReplQtmDom :" + rcpReplQtmDom);
					System.out.println("retQtyDom :" + retQtyDom);
					System.out.println("retQtyFreeDom :" + retQtyFreeDom);
					System.out.println("rcpFreeQtmDom :" + rcpFreeQtmDom);
					System.out.println("clStock :" + clStock);
					formulaValue=clStock;
					System.out.println("formulaValue@@@@@@>>>"+formulaValue);
					System.out.println("isItemSer>>>>>"+isItemSerLocal);
					
					//if(!isItemSerLocal)
					//{
					/*invoiceMonths =  dist.getDisparams("999999","INVOICE_MONTHS",conn);
					System.out.println("invoiceMonths.." + invoiceMonths);
					if (("NULLFOUND".equalsIgnoreCase(invoiceMonths) || invoiceMonths == null || invoiceMonths.trim().length() == 0) )
					{
						 invoiceMonthsPrevious=-3;
					}else
					{
						invoiceMonthsPrevious=Integer.parseInt(invoiceMonths);
					}
					System.out.println("invoiceMonthsPrevious>>>>>"+invoiceMonthsPrevious);
					*/
					//Modified by santosh to set priceList(14/SEP/2017).[START]
					calEnablePrice =  dist.getDisparams("999999","ENABLE_SPEC_PRICELIST",conn);
					calPriceDivision =  dist.getDisparams("999999","SPEC_PRICELIST",conn);
					System.out.println("calEnablePrice["+calEnablePrice+"]calPriceDivision["+calPriceDivision+"]");
					if (("NULLFOUND".equalsIgnoreCase(calEnablePrice) || calEnablePrice == null || calEnablePrice.trim().length() == 0) )
					{
						 calEnablePrice="N";
					}
					if (("NULLFOUND".equalsIgnoreCase(calPriceDivision) || calPriceDivision == null || calPriceDivision.trim().length() == 0) )
					{
						 calEnablePrice="N";
					}
					System.out.println("calEnablePrice["+calEnablePrice+"]calPriceDivision["+calPriceDivision+"]");
					//Modified by santosh to set priceList(14/SEP/2017).[END]
					thirdMonthDay= utlmethd.AddMonths(prdFromoDateTmstmp, invoiceMonthsPrevious);
					System.out.println("thirdMonthDay>>>>>>"+thirdMonthDay);
					
					closingValue=0;
					
					sql = "SELECT inv.invoice_id,itrc.rate__stduom as rate__stduom,itrc.quantity__stduom as quantity__stduom,inv.tran_date " +
							"FROM invoice_trace itrc,invoice inv WHERE itrc.item_code=?  " +
							"and itrc.invoice_id=inv.invoice_id and inv.tran_date>=? " +
							"and inv.tran_date<=? AND itrc.rate__stduom >0.001  and inv.cust_code=?  " +
							" ORDER BY inv.tran_date DESC";
					pstmt1 = conn.prepareStatement(sql);
					pstmt1.setString(1,itemCode1);
					pstmt1.setTimestamp(2,thirdMonthDay);
					pstmt1.setTimestamp(3,prdtoDateTmstmp);
					pstmt1.setString(4, custCodeDom );
					rs1 = pstmt1.executeQuery( );
					while(rs1.next())
					{
						rateStd = rs1.getDouble("rate__stduom" );
						quantityStd = rs1.getDouble("quantity__stduom" );
						System.out.println("rateStd :"+rateStd);
						System.out.println("quantityStd :"+quantityStd);
						 if(formulaValue>=quantityStd)
				         {
				        	 closingValue=closingValue+ quantityStd*rateStd;
				        	 System.out.println("closing value"+closingValue);
				        	 formulaValue=formulaValue-quantityStd;
				         }
				         else
				         {
				        	 closingValue=closingValue+ formulaValue*rateStd;
				        	 formulaValue=0;
				        	 System.out.println("closing value>>>>"+closingValue);
				         }
						 closingValue=getRequiredDcml(closingValue,3);
						 if(formulaValue == 0)
						 {
							 break;
						 }
					}
					callPstRs(pstmt1, rs1);
					System.out.println("closingValue>>>>>>"+closingValue);
					System.out.println("formulaValue>>>>>>"+formulaValue);
					if(formulaValue>0)
					{
						//Modified by santosh to set priceList(14/SEP/2017).[START]
						if("Y".equalsIgnoreCase(calEnablePrice))
						{
							if("BR".equalsIgnoreCase(itemSerHd))
							{
								priceList = calPriceDivision.substring(calPriceDivision.indexOf(",")+1,calPriceDivision.length());
								System.out.println("@S@priceList for division ::BR:: ["+priceList+"]");
							}
							else
							{
								priceList= calPriceDivision.substring(0,calPriceDivision.indexOf(","));
								System.out.println("@S@priceList for all divisions ["+priceList+"]");
							}
						}
						else
						{
							sql = "select price_list from customer where cust_code =? ";
							pstmt1 = conn.prepareStatement(sql);
							pstmt1.setString(1, custCodeDom );
							rs1 = pstmt1.executeQuery();
							if (rs1.next())
							{
								priceList = checkNull(rs1.getString("price_list"));
								System.out.println("priceList edit :" + priceList);
							}
							callPstRs(pstmt1, rs1);
						}
						//Modified by santosh to set priceList(14/SEP/2017).[END]
						//Added by saurabh to get rate from DDF_PICK_MAX_RATE fuction[18/10/16|Start] 
						sysDate = genericUtility.getValidDateString( sysDate , getApplDateFormat() , getDBDateFormat());
						sql = "SELECT DDF_PICK_MAX_SLAB_RATE( ?, TO_DATE( ? , ? ), ? ) FROM DUAL ";
						pstmt1 = conn.prepareStatement( sql );
						pstmt1.setString( 1, priceList );
						pstmt1.setString( 2, sysDate);
						pstmt1.setString( 3, getDBDateFormat() );
						pstmt1.setString( 4, itemCode1 );
						rs1 = pstmt1.executeQuery();
						if (rs1.next())
						{
							rateAll = rs1.getDouble(1);
							System.out.println("rateAll-----------> [" +rateAll+ "]");				
						}
						callPstRs(pstmt1, rs1);
						rateAll=getRequiredDcml(rateAll,3);
						//Added by saurabh to get rate from DDF_PICK_MAX_RATE fuction[18/10/16|end]
						System.out.println("rateAll>>>>>"+rateAll);
						closingValue=closingValue+formulaValue*rateAll;
						closingValue=getRequiredDcml(closingValue,3);
						
					}
					/*}else
					{
						calRate=getOpeningRate(invoiceMonthsPrevious,orderType,itemSerHeaderSplit,selectedInvList,rcpQtmDom,itemCode1,prdtoDateTmstmp,prdFromoDateTmstmp,custCodeDom,conn);
						calRate=getRequiredDcml(calRate,3);
						closingValue=clStock*calRate;
					}*/
					closingRate=0;
					if(clStock>0)
					{
						closingRate=closingValue/clStock;
					}else
					{
						closingRate=0.0;
					}
					closingRate=getRequiredDcml(closingRate,3);
					
					formulaValue=0;grossSecondaryQty=0;netSecondarySalesValue=0;grossSecondarySalesValue=0;salesQtyCal=0;
					
					grossSecondaryQty=opStkDom+rcpQtmDom+rcpReplQtmDom+rcpFreeQtmDom-retQtyDom-clStock;
					System.out.println("grossSecondaryQty>>>>"+grossSecondaryQty);
					System.out.println("closingRate@@@@@@@@@"+closingRate);
					System.out.println("grossSecondaryQty>>>>>>"+grossSecondaryQty);
					System.out.println("formulaValue>>>>>"+formulaValue);
					System.out.println("opStkDom>>>"+opStkDom+">>rateOld>>>"+rateOld);
					System.out.println("rcpValue>>>"+rcpValue+">>replValue>>>"+replValue);
					System.out.println("retValue>>>"+retValue+">>closingValue>>>"+closingValue);
					System.out.println("rcpFreeValue>>>>"+rcpFreeValue);
					if(clValOld>0)
					{
						netSecondarySalesValue=(clValOld)+rcpValue+replValue-retValue-closingValue;
					}
					else
					{
						netSecondarySalesValue=(opValDom)+rcpValue+replValue-retValue-closingValue;
					}
					netSecondarySalesValue=getRequiredDcml(netSecondarySalesValue,3);
					System.out.println("netSecondarySalesValue>>>>>>>"+netSecondarySalesValue);
					if(clValOld>0)
					{
						grossSecondarySalesValue=(clValOld)+rcpValue+replValue+rcpFreeValue-retValue-closingValue;
					}
					else
					{
						grossSecondarySalesValue=(opValDom)+rcpValue+replValue+rcpFreeValue-retValue-closingValue;
					}
					grossSecondarySalesValue=getRequiredDcml(grossSecondarySalesValue,3);
					System.out.println("grossSecondarySalesValue>>>>"+grossSecondarySalesValue);
					
					if(grossSecondaryQty>0)
					{
						grossSecondaryRate = grossSecondarySalesValue / grossSecondaryQty;
					}else
					{
						grossSecondaryRate=0.0;
					}
					grossSecondaryRate=getRequiredDcml(grossSecondaryRate,3);
					System.out.println("grossSecondaryRate>>>"+grossSecondaryRate);
					salesQtyCal = opStkDom + (rcpQtmDom + rcpReplQtmDom) - (retQtyDom + retQtyFreeDom) - clStock;
					System.out.println("salesQtyCal :" + salesQtyCal);
					if(grossSecondaryRate<0)
					{
						grossSecondaryRate=0;
					}
				
				sql =" update cust_stock_det set sales=? ,rate=? ,rate__org=? ,sales__org=? ,cl_value=? ,sales_value=? ,op_value=? where tran_id=? and item_code=? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setDouble(1, salesQtyCal );//sales
				pstmt1.setDouble(2, closingRate );//rate
				pstmt1.setDouble(3, grossSecondaryRate );//rate__org
				pstmt1.setDouble(4, grossSecondaryQty );//sales__org
				pstmt1.setDouble(5, closingValue );//cl_value
				pstmt1.setDouble(6, netSecondarySalesValue );//sales_value
				if(clValOld>0){
				pstmt1.setDouble(7, getRequiredDcml((clValOld),3) );//op_value
				}
				else
				{
					pstmt1.setDouble(7, getRequiredDcml((opValDom),3) );//op_value
				}
				pstmt1.setString(8, tranId);
				pstmt1.setString(9, itemCode1);
				UpdCnt = pstmt1.executeUpdate();
				if(UpdCnt>0)
				{
					System.out.println("No of record updated:"+UpdCnt+" for tranId>>"+tranId+">>>itemCode1"+itemCode1);
				}
				if (pstmt1 != null) 
				{
					pstmt1.close();
					pstmt1 = null;
				}
			}
			
		}
		callPstRs(pstmt, rs);	

		}
		catch(Exception e)
		{
			e.printStackTrace();
			errString = itmDBAccessEJB.getErrorString("", "VTICFAIL","", "", conn);
			//errString=e.getMessage();
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		finally
		{
			try
			{
				System.out.println(">>>In finally errString:"+errString);
				if( errString != null && errString.trim().length()>0 )
				{
					//errString = itmDBAccessEJB.getErrorString("", "VTICFAIL","", "", conn);
					return errString;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				errString = itmDBAccessEJB.getErrorString("", "VTICFAIL","", "", conn);
			}
		
		}
		return errString;
	}
	
	/*private double getOpeningRate(int invoiceMonthsPrevious,String orderType,String itemSerHeaderSplit,String selectedInvList ,double rcpQtyDom,String itemCode, Timestamp prdtoDateTmstmp,Timestamp prdFromoDateTmstmp, String custCode, Connection conn) throws ITMException
    {
		E12GenericUtility genericUtility =new E12GenericUtility();
		UtilMethods utlmethd = new UtilMethods();
		ibase.webitm.ejb.dis.DistCommon dist = new ibase.webitm.ejb.dis.DistCommon();
	    String invoiceMonths="",sql="";
	    String sysDatetemp="",priceList="";
	    //int invoiceMonthsPrevious=0;
	    Timestamp thirdMonthDay=null;
	    double openingRate=0.0;
	    PreparedStatement pstmt = null,pstmt1 = null;
		ResultSet rs = null,rs1 = null;
		String sysDate="";
	    try
		{
	    	Date currentDate = new Date();
	    	SimpleDateFormat sdf2 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			sysDatetemp = sdf2.format(currentDate.getTime());
			
			if(rcpQtyDom > 0)
			{
				sql =  " SELECT itrace.RATE__STDUOM as RATE__STDUOM FROM" +
						" invoice invoice,invoice_trace itrace ,item item " +
						" 	where invoice.invoice_id=itrace.invoice_id  and itrace.item_code=item.item_code AND invoice.cust_code = ? " +
						" and item.item_ser in ("+itemSerHeaderSplit+") "+
						" AND itrace.RATE__STDUOM>0.001 "+ 
						" and itrace.item_code = ? " +
						"and itrace.invoice_id in("+selectedInvList+") ORDER BY invoice.tran_date DESC " ;
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,custCode  );
				pstmt.setString(2, itemCode );
				rs = pstmt.executeQuery( );
				if( rs.next() )
				{
					openingRate = rs.getDouble("RATE__STDUOM" );
					System.out.println("openingRate>>>>>> :"+openingRate);
				}
				callPstRs(pstmt, rs);
			}
			else
			{
			invoiceMonths = dist.getDisparams("999999", "INVOICE_MONTHS", conn);
			System.out.println("invoiceMonths>>>>.." + invoiceMonths);
			if (("NULLFOUND".equalsIgnoreCase(invoiceMonths) || invoiceMonths == null || invoiceMonths.trim().length() == 0))
			{
				invoiceMonthsPrevious = -3;
			} else
			{
				invoiceMonthsPrevious = Integer.parseInt(invoiceMonths);
			}
			System.out.println("invoiceMonthsPrevious>>>>>" + invoiceMonthsPrevious);
			thirdMonthDay = utlmethd.AddMonths(prdFromoDateTmstmp, invoiceMonthsPrevious);
			System.out.println("thirdMonthDay from method>>>>>>" + thirdMonthDay);

			sql = "SELECT inv.invoice_id,itrc.rate__stduom as rate__stduom,inv.tran_date " + 
			"FROM invoice_trace itrc,invoice inv WHERE itrc.item_code=?  " + 
					"and itrc.invoice_id=inv.invoice_id and inv.tran_date>=? " +
			"and inv.tran_date<=? AND itrc.rate__stduom >0.001  and inv.cust_code=?  " + 
					" ORDER BY inv.tran_date DESC";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode);
			pstmt.setTimestamp(2, thirdMonthDay);
			pstmt.setTimestamp(3, prdtoDateTmstmp);
			pstmt.setString(4, custCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				openingRate = rs.getDouble("rate__stduom");
				System.out.println("openingRate>>>>>> :" + openingRate);

			}
			callPstRs(pstmt, rs);
			if (openingRate == 0)
			{
				sql = "select price_list from customer where cust_code =? ";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, custCode);
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					priceList = checkNull(rs1.getString("price_list"));
					System.out.println("priceList edit :" + priceList);
				}
				callPstRs(pstmt1, rs1);
				sysDate = genericUtility.getValidDateString( sysDatetemp , getApplDateFormat() , getDBDateFormat());
				sql = "SELECT DDF_PICK_MAX_SLAB_RATE( ?, TO_DATE( ? , ? ), ? ) FROM DUAL ";
				pstmt1 = conn.prepareStatement( sql );
				pstmt1.setString( 1, priceList );
				pstmt1.setString( 2, sysDate );
				pstmt1.setString( 3, getDBDateFormat() );
				pstmt1.setString( 4, itemCode );
				rs1 = pstmt1.executeQuery();
				if (rs1.next())
				{
					openingRate = rs1.getDouble(1);
					System.out.println("openingRate-----------> [" +openingRate+ "]");				
				}
				callPstRs(pstmt1, rs1);
			}
		}
	    	
		}catch(Exception exception)
		{
			exception.printStackTrace();
			throw new ITMException( exception );
		}
		System.out.println("return openingRate>>>>"+openingRate);
	    return openingRate;
    }*/
	public String getItemSerList(String itemser, Connection conn) throws ITMException
	{
		String itemSerGrpValue="",itemSerSplit="",resultItemSer="";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		try
		{
			sql= " select distinct item_ser from" +
					"(select item_ser from itemser where grp_code=?  " +
					"union all " +
					"select item_ser from itemser where item_ser=?) ";
					
		pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, itemser);
		pstmt.setString(2, itemser);
		rs = pstmt.executeQuery();
		while(rs.next())
		{
			itemSerGrpValue=checkNull(rs.getString("item_ser")).trim();
			itemSerSplit=itemSerSplit+"'"+itemSerGrpValue+"',";
		}
		callPstRs(pstmt, rs);
		resultItemSer = itemSerSplit.substring(0, itemSerSplit.length() - 1);
		System.out.println("resultItemSer>>>>>"+resultItemSer);
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			try
            {
                throw new ITMException( exception );
            } catch (ITMException e)
            {
                e.printStackTrace();
            }
			throw new ITMException(exception); //Added By Mukesh Chauhan on 02/08/19
		}
		return resultItemSer;
	}
	
	public boolean isValidDouble(String number) throws ITMException, Exception
	{

		Boolean isReult = true;
		double amount=0.0;
		try
		{
			amount = Double.parseDouble(number);
			System.out.println("amount>>>>>>>>"+amount);

		} catch (NumberFormatException e)
		{

			isReult = false;
		}
		return isReult;

	}

	public double getRequiredDcml(double actVal, int prec)
	{
		double value=0.0;
		String fmtStr = "############0";
		if (prec > 0)
		{
			fmtStr = fmtStr + "." + "000000000".substring(0, prec);
		}
		DecimalFormat decFormat = new DecimalFormat(fmtStr);
		if(decFormat.format(actVal) != null && decFormat.format(actVal).trim().length() > 0 )
		{
			value=Double.parseDouble(decFormat.format(actVal));
		}else
		{
			value=0.00;
		}
		return value;
	}
	
	private String checkNull(String input)
	{
		return input == null ? "" : input.trim();
	}
	
	public void callPstRs(PreparedStatement pstmt, ResultSet rs) 
	{
		try 
		{
			if (pstmt != null) 
			{
				pstmt.close();
				pstmt = null;
			}
			if (rs != null) 
			{
				rs.close();
				rs = null;
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
