package ibase.webitm.ejb.dis;
 
import org.w3c.dom.*;

import java.sql.*;
import java.util.*;
import java.io.*;

import java.text.SimpleDateFormat;

import javax.naming.InitialContext;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import org.xml.sax.InputSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

import ibase.system.config.AppConnectParm;
//import ibase.webitm.ejb.MasterStateful; // for ejb3
//import ibase.webitm.ejb.MasterStatefulHome; // for ejb3
import ibase.webitm.ejb.MasterStatefulLocal;  // for ejb3
import ibase.webitm.utility.*;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;

public class SaleOrdItmGen
{
    //GenericUtility genericUtility =GenericUtility.getInstance();
	E12GenericUtility genericUtility= new  E12GenericUtility();
    Statement stmt = null;
    ResultSet rs = null;
    
   // MasterStatefulHome masterStatefulHome = null;  //for ejb3 on 3/31/2009
     MasterStatefulLocal masterStatefulLocal = null;  //for ejb3 on 3/31/2009
   //  MasterStateful masterStateful = null; // for jeb3
    
	public String genItemSalesOrd(Document dom,Connection conn) throws ITMException{
        NodeList detailNodes = null,currTranDetailNodes = null;
        int detailNodeListLength = 0;
        Node tranNode = null,currField = null,currDetail = null,currHdr = null,currDet = null;
        String currDetailName = "",itemGenAttrib = "",itemType = "",sql = "";
        String itemCode = "";
        String currTranId = "",currLineNo = "",sorderRetStr = "";
        ArrayList genAttribToken = new ArrayList();
        
	    try{
            stmt = conn.createStatement();
	        detailNodes = dom.getElementsByTagName("Detail");
            detailNodeListLength = detailNodes.getLength();
            System.out.println("detailNodeListLength :"+detailNodeListLength);
            for(int i = 0;i < detailNodeListLength;i++){
                tranNode = detailNodes.item(i);
                currTranDetailNodes = tranNode.getChildNodes();
                currHdr = currTranDetailNodes.item(0);
                System.out.println("currTranDetailNodes.getLength() "+currTranDetailNodes.getLength());
                for(int cnt = 0;cnt < currTranDetailNodes.getLength();cnt++){
                    currDetail = currTranDetailNodes.item(cnt);
                    
                    currDetailName = currDetail.getNodeName();
                    System.out.println("currDetailName :"+currDetailName);
                    if(currDetailName.equalsIgnoreCase("Detail2")){
                        itemType = genericUtility.getColumnValueFromNode("item_type",currDetail);
                        sql = "select gen_attrib from item_type where item_type = '"+itemType+"'";
                        rs = stmt.executeQuery(sql);
                        if(rs.next()){
                            itemGenAttrib = rs.getString(1);
                        }
                        rs.close();
                        
                        genAttribToken = genericUtility.getTokenList(itemGenAttrib,",");
                        
                        
                        StringBuffer sql1 = new StringBuffer();
                        sql1.append("select item_code from item where  ");
                        int count = 0;
                        itemCode = "";
                        for (int j = 0;j < genAttribToken.size() ;j++ ){
                            String attNo = genAttribToken.get(j).toString();
                            System.out.println("attNo...... :: "+attNo);
                            String attValue =genericUtility.getColumnValueFromNode("phy_attrib_"+attNo,currDetail);
                            System.out.println("attValue...... :: "+attValue);
                            if (attValue != null && attValue.trim().length() > 0){
                                if (count == 0){
                                    sql1.append(" phy_attrib_"+attNo+" = '").append(attValue).append("' ");
                                }
                                else{
                                    sql1.append(" and phy_attrib_"+attNo+" = '").append(attValue).append("' ");
                                }
                                count++;
                            }
                            else{
                                sql1.append("and (phy_attrib_"+attNo+" is null or phy_attrib_"+attNo+" = '') ");
                            }
                        }
                        System.out.println("SQL "+sql1.toString());
                        rs = stmt.executeQuery(sql1.toString());
                        if(rs.next()){
                            itemCode = rs.getString(1);
                            System.out.println("Item Code "+itemCode);
                        }
                        rs.close();
                        if(itemCode == null || itemCode.trim().length() == 0){
                            itemCode = generateItemCode(currHdr,currDetail,conn);
                            if(itemCode.indexOf("Errors") != -1){
                                return itemCode;
                            }
                            if(itemCode != null &&  itemCode.trim().length() > 0){
                                currTranId = genericUtility.getColumnValueFromNode("tran_id",currDetail);
                                currLineNo = genericUtility.getColumnValueFromNode("line_no",currDetail);
                                sql = "update sordform_att_det set item_code = '"+itemCode+"' " +
                                        "where tran_id = '"+currTranId+"' and line_no = '"+currLineNo+"'";
                                System.out.println("SQL "+sql);
                                int upd = stmt.executeUpdate(sql);
                                System.out.println("Record Updated "+upd);
                                updateNode(itemCode,currDetail);   
                                conn.commit();
                            }
                        }
                    }
                }//Transaction End
                sorderRetStr = generateSaleOrder(tranNode,conn);
                if(sorderRetStr.indexOf("Success") == -1){
                    break;
                }
            }
        }catch(SQLException se){
            System.out.println("SQLException "+se);
            se.printStackTrace();
            throw new ITMException(se); //Added By Mukesh Chauhan on 05/08/19
        }catch(Exception e){
            System.out.println("Exception in SaleOrdItemGen "+e);
            e.printStackTrace();
            throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
        }
        finally{
            try{
                if(rs != null)
                    rs.close();
                if(stmt != null)
                    stmt.close();
            }catch(Exception e){}
        }
        return sorderRetStr;
	}
    
    private String generateItemCode(Node header,Node detail,Connection conn)throws ITMException
    {

            Statement stmt = null;
            ResultSet rs = null;
            PreparedStatement pstmt = null,pstmt1 =null;
            String itemSer = "",unit = "",locType = "",siteCode = "";
            String retString = "",newItemCode = "",itemType = "";
            String genAttrib = "",sql = "",hold ="";
            String tempItem = "",existItem = "",phyItem2 = "",phyItem3 ="";
            String keyString = "",keyCol = "",tranSer = "";
            StringBuffer XMLString = new StringBuffer();;
			String tranIdG ="",sql2 = "",phyItem21 ="",phyItem22="";
			String attNo = "";
			String attValue = "";
            boolean temp = false;
            int upd = 0, upd1 =0, i=0,up = 0;
            //ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB(); 
            ArrayList genAttribList = new ArrayList();

            itemSer = genericUtility.getColumnValueFromNode("item_ser",header);
            unit = genericUtility.getColumnValueFromNode("unit",detail);
            locType = genericUtility.getColumnValueFromNode("loc_type",detail); 
            siteCode = genericUtility.getColumnValueFromNode("site_code",header);
            itemType = genericUtility.getColumnValueFromNode("item_type",detail);
            hold = genericUtility.getColumnValueFromNode("phy_attrib_21",detail);
            tempItem = genericUtility.getColumnValueFromNode("phy_attrib_22",detail);
            existItem = genericUtility.getColumnValueFromNode("phy_attrib_1",detail);
            phyItem2 = genericUtility.getColumnValueFromNode("phy_attrib_2",detail);
			phyItem3 = genericUtility.getColumnValueFromNode("phy_attrib_3",detail);

			phyItem21 = genericUtility.getColumnValueFromNode("phy_attrib_21",detail);
			phyItem22 = genericUtility.getColumnValueFromNode("phy_attrib_22",detail);
		

            try
            {
                if(existItem == null || existItem.trim().length() == 0){
					existItem = "";
				}
				if(phyItem2 == null || phyItem2.trim().length() == 0){
					phyItem2 = "";
				}
				sql = " SELECT KEY_STRING,TRAN_ID_COL,REF_SER FROM TRANSETUP "
                        +"WHERE TRAN_WINDOW ='w_item' ";
                pstmt = conn.prepareStatement(sql);
                rs = pstmt.executeQuery();  
                System.out.println("sql..... :: "+sql);
                if (rs.next())
                {
                    keyString = rs.getString(1);
                    keyCol = rs.getString(2);
                    tranSer = rs.getString(3);
                }
                rs.close(); 
                pstmt.close();
                XMLString.append( "<?xml version=\"1.0\"?>\r\n<Root>\r\n<header>");
                            XMLString.append("\r\n</header><Detail1><tran_id></tran_id><item_ser>"+itemSer+"</item_ser>");
							//+"</Detail1></Root>";
               
                sql = "select gen_attrib from item_type where item_type ='"+itemType+"'";
                pstmt = conn.prepareStatement(sql);
               // stmt = conn.createStatement();
                rs = pstmt.executeQuery();  
                System.out.println("sql..... :: "+sql);
                //rs = stmt.executeQuery(sql);
                if(rs.next()){
                    genAttrib = rs.getString(1);
                }
                rs.close();
                pstmt.close();
                genAttribList = genericUtility.getTokenList(genAttrib,",");

                StringBuffer sql11 = new StringBuffer();
                StringBuffer sql12 = new StringBuffer();
                sql11.append("INSERT INTO ITEM (ITEM_CODE ,DESCR , ITEM_SER , UNIT ,STK_OPT ,CHG_DATE , CHG_USER ,CHG_TERM,ACTIVE ,item_type");
                sql12.append("VALUES (?,?,?,?,?,?,?,?,?,? ");
                for (i = 0;i < genAttribList.size() ;i++ )
                {
                            attNo = genAttribList.get(i).toString();
                            System.out.println("attNo...... :: "+attNo); 
                            //String attValue =(String) getVal.get((Integer.parseInt(attNo)) -1);
                            attValue =genericUtility.getColumnValueFromNode("phy_attrib_"+attNo,detail)==null ?"":genericUtility.getColumnValueFromNode("phy_attrib_"+attNo,detail);
                            
							XMLString.append("<phy_attrib_"+attNo+">"+attValue+"</phy_attrib_"+attNo+">");
                            //XMLString.append("<phy_attrib_"+attNo+">"+attValue == null ? "" :attValue+"</phy_attrib_"+attNo+">");
							System.out.println("attValue...... :: "+attValue);
                            sql11.append(", PHY_ATTRIB_"+attNo+" ");
                        
                                if (attValue != null && attValue.trim().length() > 0 )
                                {
                                    sql12.append(" ,'"+attValue+"' ");
                                }
                                else
                                {
                                    sql12.append(",''");
                                    
                                }
                }

				XMLString.append("</Detail1></Root>");
				System.out.println("XMLString...... :: "+XMLString.toString());

				TransIDGenerator tg = new TransIDGenerator(XMLString.toString(), "BASE", CommonConstants.DB_NAME);
                tranIdG = tg.generateTranSeqID(tranSer, keyCol, keyString, conn);
                retString = tranIdG ;
                System.out.println("tranIdG :"+tranIdG);
                System.out.println("itemSer :"+itemSer);
                CommonConstants.setIBASEHOME();
				sql11.append(" ,phy_attrib_21,phy_attrib_22");
				sql12.append(" ,?,?");
                sql11.append(")");
                sql12.append(")");
                sql11 =sql11.append(sql12);
                pstmt = conn.prepareStatement(sql11.toString());
                System.out.println("SQL "+sql11.toString());
                pstmt.setString(1,tranIdG);
                pstmt.setString(2,"New Item");
                pstmt.setString(3,itemSer);
                pstmt.setString(4,unit);
                pstmt.setString(5,"2");
                pstmt.setTimestamp(6,new Timestamp(System.currentTimeMillis() ));
                pstmt.setString(7,"SYSTEM");
                pstmt.setString(8,"SYSTEM");
				pstmt.setString(9,"Y");
				pstmt.setString(10,itemType);
				pstmt.setString(11,phyItem21);
				pstmt.setString(12,phyItem22);
                System.out.println("Item Code sql11 :: "+sql11.toString());
                System.out.println("Item Code sql12 :: "+sql12.toString());
                upd = pstmt.executeUpdate();
                if(upd == 1)
                {
                    System.out.println("Record inserted  upd:"+upd);
                }
                pstmt.close();
				StringBuffer sqlBuff = new StringBuffer();
                if(tempItem.equalsIgnoreCase("N"))
                {
                      System.out.println("PHY_ATTRIB_22   is ..N :: ");
                      ArrayList getATT = new ArrayList();
                      StringBuffer query = new StringBuffer();
                      sql = " SELECT DESCR,SH_DESCR,ITEM_PARNT,ITEM_SER,GRP_CODE,SGRP_CODE,TRADE_MARK,UNIT,ITEM_TYPE,LOC_CODE, "
                            + " SITE_CODE,SITE_CODE__OWN,MIN_QTY,MAX_QTY,REO_QTY,REO_LEV,ORDC_PERC,STK_OPT,PRICE_LIST,REGULATED_PRICE, "
							+ " PURC_RATE ,SALE_RATE,COST_RATE,ACTIVE,ITEM_STRU,SUPP_SOUR,MFG_LEAD,QC_LEAD_TIME,LOC_TYPE,TAX_CHAP,TAX_CLASS, "
                            + " APR_CODE ,SHELF_LIFE,MIN_SHELF_LIFE,TRACK_SHELF_LIFE,UNIT__RATE,SPEC_TOL,DLV_PRD_TOL_BEF,DLV_PRD_TOL_AFT, "
							+ " BIN_NO,CYCLE_COUNT,QC_CYCLE_TIME,QC_REQD,SPEC_REQD,POVAL_VAR,APPR_SUPP,MARKET_REG,PACK_CODE,PACK_INSTR, "
							+ " YIELD_PERC,BOM_CODE,GENERIC_DESCR,TECH_DESCR,REG_SCH,POTENCY_PERC,QTY_TOL_PERC,RCP_TOL_PERC,EMP_CODE__PUR ," 
							+ " EMP_CODE__PLN,CAS_NO,HSN_NO,MIN_ORDER_QTY,MAX_ORDER_QTY,PUR_LEAD_TIME,EMP_CODE__IAPR,SUPP_CODE__PREF,LEVEL_CODE__HIER, "
							+ " ADD_DATE,ADD_USER,ADD_TERM,BOM_CODE__BENEFIT,ORDER_OPT,INTEGRAL_QTY,MIN_STK_DAY,QUOT_OPT,QC_REQD_TYPE,CANC_BO_MODE, "
							+ " INDENT_OPT,BENEFIT_CATEGORY,QTY_SAMPLE,NET_WEIGHT,START_SALE,START_DEV,START_MFG,MFG_DATE_ON,ISS_CRITERIA,UNIT__PUR, "
							+ "	STOP_BUSINESS,SCANNED_BARCODE,PO_RATE_OPTION,PO_RATE_VARIENCE,EMP_CODE__QCAPRV,MIN_NO_ENQUIRY,IND_VAL_TO_RAISE_ENQ, "
							+ " TAX_CHAP__REP,MFG_TYPE,SALE_OPTION,CUST_CODE__MKT,STD_DISC,BATCH_QTY_TYPE,ROLE_CODE__QCAPRV,ROLE_CODE__INDAPRV, "
							+ " MAX_STK_DAY,REMARKS,ITEM_USAGE,NO_SALES_MONTH,UDF__STR1,UDF__STR2,QC_TABLE,ITEM_CODE__PLAN,CONTRACT_REQ,VARIENCE_QTYPER, "
							+ " STAB_PATTERN,OHD_TABLE__POST,AUTO_REQC,UNIT__SAL,ITEM_CODE__NDC,PARK_TYPE,PARK_NO,CONTAINER_QTY,UNIT__NETWT, "
							+ " DIMENSION,UNIT__DIMN,BOM_CODE__STD,ITEM_CODE__AL,NO_CONS_UNAPRV,DISAPPRV_ON_REJ,NO_OF_REJ,EXTRA_MFG_PERC,TABLE_NO__DETR, "
							+ " APPL_SEG FROM ITEM WHERE ITEM_CODE ='"+ existItem+"' " ;
					  System.out.println(" sql..... :: "+ sql);
						pstmt = conn.prepareStatement(sql);
                        rs = pstmt.executeQuery(); 
                      
					  if(rs.next())
					  {
						  sqlBuff.append(" UPDATE ITEM SET DESCR = '"+ (rs.getString("DESCR") == null ? "" :rs.getString("DESCR")+""+phyItem2+""+phyItem3)+"' , "
								 + " SH_DESCR = '"+ (rs.getString("SH_DESCR") == null ? "" :rs.getString("SH_DESCR")+""+phyItem2+""+phyItem3) +"' , "
								+ " ITEM_PARNT = '"+ (rs.getString("ITEM_PARNT") == null ? "" :rs.getString("ITEM_PARNT")) +"' , "
								+ " GRP_CODE = '"+ (rs.getString("GRP_CODE") == null ? "" :rs.getString("GRP_CODE")) +"' , "
								+ " TRADE_MARK = '"+ (rs.getString("TRADE_MARK") == null ? "" :rs.getString("TRADE_MARK")) +"' , "
								+ " SITE_CODE__OWN = '"+ (rs.getString("SITE_CODE__OWN") == null ? "" :rs.getString("SITE_CODE__OWN")) +"' , "
								+ " MIN_QTY = "+ (rs.getDouble("MIN_QTY") ) +" , "
								+ " MAX_QTY = "+ (rs.getDouble("MAX_QTY")) +" , "
								+ " REO_QTY = "+ (rs.getDouble("REO_QTY") ) +" , "
								+ " REO_LEV = "+ (rs.getDouble("REO_LEV") ) +" , "
								+ " ORDC_PERC = "+ (rs.getDouble("ORDC_PERC") ) +" , "
								+ " PRICE_LIST = '"+ (rs.getString("PRICE_LIST") == null ? "" :rs.getString("PRICE_LIST")) +"' , "
								+ " REGULATED_PRICE = '"+ (rs.getString("REGULATED_PRICE") == null ? "" :rs.getString("REGULATED_PRICE")) +"' , "
								+ " PURC_RATE = "+ (rs.getDouble("PURC_RATE")) +" , "
								+ " SALE_RATE = "+ (rs.getDouble("SALE_RATE") ) +" , "
								+ " COST_RATE = "+ (rs.getDouble("COST_RATE")) +" , "
								//+ " ACTIVE = '"+ (rs.getString("ACTIVE") == null ? "" :rs.getString("ACTIVE")) +"' , "
								+ " ITEM_STRU = '"+ (rs.getString("ITEM_STRU") == null ? "" :rs.getString("ITEM_STRU")) +"' , "
								+ " SUPP_SOUR = '"+ (rs.getString("SUPP_SOUR") == null ? "" :rs.getString("SUPP_SOUR")) +"'  ,"
								+ " MFG_LEAD = "+ (rs.getDouble("MFG_LEAD") ) +" , "
								+ " QC_LEAD_TIME = "+ (rs.getDouble("QC_LEAD_TIME") ) +" , "
								+ " LOC_TYPE = '"+ (rs.getString("LOC_TYPE") == null ? "" :rs.getString("LOC_TYPE")) +"' , "
								+ " TAX_CHAP = '"+ (rs.getString("TAX_CHAP") == null ? "" :rs.getString("TAX_CHAP")) +"' , "
								+ " MIN_SHELF_LIFE = "+ (rs.getDouble("MIN_SHELF_LIFE") ) +", "
								+ " TRACK_SHELF_LIFE = '"+ (rs.getString("TRACK_SHELF_LIFE") == null ? "" :rs.getString("TRACK_SHELF_LIFE")) +"' , "
								+ " SPEC_TOL = '"+ (rs.getString("SPEC_TOL") == null ? "" :rs.getString("SPEC_TOL")) +"' , "
								+ " DLV_PRD_TOL_BEF = "+ (rs.getDouble("DLV_PRD_TOL_BEF") ) +" , "
								+ " DLV_PRD_TOL_AFT = "+ (rs.getDouble("DLV_PRD_TOL_AFT") ) +" , "
								+ " BIN_NO = '"+ (rs.getString("BIN_NO") == null ? "" :rs.getString("BIN_NO")) +"' , "
								+ " CYCLE_COUNT = "+ (rs.getDouble("CYCLE_COUNT")) +" , "
								+ " QC_CYCLE_TIME = "+ (rs.getString("QC_CYCLE_TIME")) +" , "
								+ " QC_REQD = '"+ (rs.getString("QC_REQD") == null ? "" :rs.getString("QC_REQD")) +"' , "
								+ " SPEC_REQD = '"+ (rs.getString("SPEC_REQD") == null ? "" :rs.getString("SPEC_REQD")) +"' , "
								+ " POVAL_VAR = "+ (rs.getDouble("POVAL_VAR") ) +" , "
								+ " APPR_SUPP = '"+ (rs.getString("APPR_SUPP") == null ? "" :rs.getString("APPR_SUPP")) +"' , "
								+ " MARKET_REG = '"+ (rs.getString("MARKET_REG") == null ? "" :rs.getString("MARKET_REG")) +"' , "
								+ " PACK_CODE = '"+ (rs.getString("PACK_CODE") == null ? "" :rs.getString("PACK_CODE")) +"' , "
								+ " PACK_INSTR = '"+ (rs.getString("PACK_INSTR") == null ? "" :rs.getString("PACK_INSTR")) +"' , "
								+ " YIELD_PERC = "+ (rs.getDouble("YIELD_PERC") ) +" , "
								+ " BOM_CODE = '"+ (rs.getString("BOM_CODE") == null ? "" :rs.getString("BOM_CODE")) +"' , "
								+ " GENERIC_DESCR = '"+ (rs.getString("GENERIC_DESCR") == null ? "" :rs.getString("GENERIC_DESCR")) +"' , "
								+ " TECH_DESCR = '"+ (rs.getString("TECH_DESCR") == null ? "" :rs.getString("TECH_DESCR")) +"' , "
								
								+ " POTENCY_PERC = "+ (rs.getDouble("POTENCY_PERC")) +" , "
								+ " QTY_TOL_PERC = "+ (rs.getDouble("QTY_TOL_PERC")) +" , "
								+ " RCP_TOL_PERC = "+ (rs.getDouble("RCP_TOL_PERC")) +" , "
								+ " CAS_NO = '"+ (rs.getString("CAS_NO") == null ? "" :rs.getString("CAS_NO")) +"' , "
								+ " HSN_NO = '"+ (rs.getString("HSN_NO") == null ? "" :rs.getString("HSN_NO")) +"' , "

								+ " MIN_ORDER_QTY = "+ (rs.getDouble("MIN_ORDER_QTY") ) +" , "
								+ " MAX_ORDER_QTY = "+ (rs.getDouble("MAX_ORDER_QTY") ) +" , "
								+ " PUR_LEAD_TIME = "+ (rs.getDouble("PUR_LEAD_TIME")) +" , "
								+ " LEVEL_CODE__HIER = '"+ (rs.getString("LEVEL_CODE__HIER") == null ? "" :rs.getString("LEVEL_CODE__HIER")) +"' , "
							
								+ " ADD_USER = '"+ (rs.getString("ADD_USER") == null ? "" :rs.getString("ADD_USER")) +"' , "
								+ " ADD_TERM = '"+ (rs.getString("ADD_TERM") == null ? "" :rs.getString("ADD_TERM")) +"' , "
								+ " ORDER_OPT = '"+ (rs.getString("ORDER_OPT") == null ? "" :rs.getString("ORDER_OPT")) +"' , "
								+ " INTEGRAL_QTY = "+ (rs.getDouble("INTEGRAL_QTY") ) +" , "
								+ " MIN_STK_DAY = "+ (rs.getInt("MIN_STK_DAY") ) +" , "
								+ " QUOT_OPT = '"+ (rs.getString("QUOT_OPT") == null ? "" :rs.getString("QUOT_OPT")) +"' , "
								+ " QC_REQD_TYPE = '"+ (rs.getString("QC_REQD_TYPE") == null ? "" :rs.getString("QC_REQD_TYPE")) +"' , "
								+ " CANC_BO_MODE = '"+ (rs.getString("CANC_BO_MODE") == null ? "" :rs.getString("CANC_BO_MODE")) +"' , "
								+ " INDENT_OPT = '"+ (rs.getString("INDENT_OPT") == null ? "" :rs.getString("INDENT_OPT")) +"' , "
								+ " BENEFIT_CATEGORY = '"+ (rs.getString("BENEFIT_CATEGORY") == null ? "" :rs.getString("BENEFIT_CATEGORY")) +"' , "
								+ " QTY_SAMPLE = "+ (rs.getDouble("QTY_SAMPLE") ) +" , "
								+ " NET_WEIGHT = "+ (rs.getDouble("NET_WEIGHT") ) +" , "
								
								
								+ " ISS_CRITERIA = '"+ (rs.getString("ISS_CRITERIA") == null ? "" :rs.getString("ISS_CRITERIA")) +"' , "
								+ " UNIT__PUR = '"+ (rs.getString("UNIT__PUR") == null ? "" :rs.getString("UNIT__PUR")) +"' , "
								+ " STOP_BUSINESS = '"+ (rs.getString("STOP_BUSINESS") == null ? "" :rs.getString("STOP_BUSINESS")) +"' , "
								+ " SCANNED_BARCODE = '"+ (rs.getString("SCANNED_BARCODE") == null ? "" :rs.getString("SCANNED_BARCODE")) +"' , "
								+ " PO_RATE_OPTION = '"+ (rs.getString("PO_RATE_OPTION") == null ? "" :rs.getString("PO_RATE_OPTION")) +"' , "
								+ " PO_RATE_VARIENCE = "+ (rs.getDouble("PO_RATE_VARIENCE") ) +" , "
								+ " EMP_CODE__QCAPRV = '"+ (rs.getString("EMP_CODE__QCAPRV") == null ? "" :rs.getString("EMP_CODE__QCAPRV")) +"' , "
								+ " MIN_NO_ENQUIRY = "+ (rs.getInt("MIN_NO_ENQUIRY") ) +" , "
								+ " IND_VAL_TO_RAISE_ENQ = "+ (rs.getDouble("IND_VAL_TO_RAISE_ENQ") ) +" , "
								+ " TAX_CHAP__REP = '"+ (rs.getString("TAX_CHAP__REP") == null ? "" :rs.getString("TAX_CHAP__REP")) +"' , "
								+ " MFG_TYPE = '"+ (rs.getString("MFG_TYPE") == null ? "" :rs.getString("MFG_TYPE")) +"' , "
								+ " SALE_OPTION = '"+ (rs.getString("SALE_OPTION") == null ? "" :rs.getString("SALE_OPTION")) +"' , "
								+ " CUST_CODE__MKT = '"+ (rs.getString("CUST_CODE__MKT") == null ? "" :rs.getString("CUST_CODE__MKT")) +"' , "
								+ " STD_DISC = "+ (rs.getDouble("STD_DISC") ) +" , "
								+ " BATCH_QTY_TYPE = '"+ (rs.getString("BATCH_QTY_TYPE") == null ? "" :rs.getString("BATCH_QTY_TYPE")) +"' , "
								+ " ROLE_CODE__QCAPRV = '"+ (rs.getString("ROLE_CODE__QCAPRV") == null ? "" :rs.getString("ROLE_CODE__QCAPRV")) +"' , "
								+ " ROLE_CODE__INDAPRV = '"+ (rs.getString("ROLE_CODE__INDAPRV") == null ? "" :rs.getString("ROLE_CODE__INDAPRV")) +"' , "
								+ "MAX_STK_DAY = "+ (rs.getInt("MAX_STK_DAY") ) +" , "
								
								+ " REMARKS = '"+ (rs.getString("REMARKS") == null ? "" :rs.getString("REMARKS")) +"' , "
								+ " ITEM_USAGE = '"+ (rs.getString("ITEM_USAGE") == null ? "" :rs.getString("ITEM_USAGE")) +"' , "
								+ "NO_SALES_MONTH = "+ (rs.getInt("NO_SALES_MONTH") ) +" , "
								+ " UDF__STR1 = '"+ (rs.getString("UDF__STR1") == null ? "" :rs.getString("UDF__STR1")) +"' , "
								+ " UDF__STR2 = '"+ (rs.getString("UDF__STR2") == null ? "" :rs.getString("UDF__STR2")) +"' , "
								+ " QC_TABLE = '"+ (rs.getString("QC_TABLE") == null ? "" :rs.getString("QC_TABLE")) +"' , "
								+ " ITEM_CODE__PLAN = '"+ (rs.getString("ITEM_CODE__PLAN") == null ? "" :rs.getString("ITEM_CODE__PLAN")) +"' , "
								+ " CONTRACT_REQ = '"+ (rs.getString("CONTRACT_REQ") == null ? "" :rs.getString("CONTRACT_REQ")) +"' , "

							
								+ " STAB_PATTERN= '"+ (rs.getString("STAB_PATTERN") == null ? "" :rs.getString("STAB_PATTERN")) +"' , "
								+ " OHD_TABLE__POST = '"+ (rs.getString("OHD_TABLE__POST") == null ? "" :rs.getString("OHD_TABLE__POST")) +"' , "
								+ " AUTO_REQC = '"+ (rs.getString("AUTO_REQC") == null ? "" :rs.getString("AUTO_REQC")) +"' , "
								+ " ITEM_CODE__NDC = '"+ (rs.getString("ITEM_CODE__NDC") == null ? "" :rs.getString("ITEM_CODE__NDC")) +"' , "
								+ " PARK_TYPE = '"+ (rs.getString("PARK_TYPE") == null ? "" :rs.getString("PARK_TYPE")) +"' , "
								+ " PARK_NO = '"+ (rs.getString("PARK_NO") == null ? "" :rs.getString("PARK_NO")) +"' , "
								+ " CONTAINER_QTY = "+ (rs.getDouble("CONTAINER_QTY") ) +" , "
								+ " UNIT__DIMN = '"+ (rs.getString("UNIT__DIMN") == null ? "" :rs.getString("UNIT__DIMN")) +"' , "
								+ " UNIT__NETWT = '"+ (rs.getString("UNIT__NETWT") == null ? "" :rs.getString("UNIT__NETWT")) +"' , "
								+ " DIMENSION = '"+ (rs.getString("DIMENSION") == null ? "" :rs.getString("DIMENSION")) +"' , "
								//ADDED  FIELD BY RAJENDRA 
		            			+ " UNIT__SAL = '"+ (rs.getString("UNIT__SAL") == null ? "" :rs.getString("UNIT__SAL")) +"' , "	
								+ " UNIT__RATE = '"+ (rs.getString("UNIT__RATE") == null ? "" :rs.getString("UNIT__RATE")) +"' , "	
								+ " BOM_CODE__BENEFIT = '"+ (rs.getString("BOM_CODE__BENEFIT") == null ? "" :rs.getString("BOM_CODE__BENEFIT")) +"' , "
								+ " REG_SCH = '"+ (rs.getString("REG_SCH") == null ? "" :rs.getString("REG_SCH")) +"' , "	);
								if(rs.getString("APR_CODE") != null)
								{
									sqlBuff.append( " APR_CODE = '"+ (rs.getString("APR_CODE")) +"' , ");
								}
								if(rs.getString("SITE_CODE") != null)
								{
									sqlBuff.append( " SITE_CODE = '"+ (rs.getString("SITE_CODE")) +"' , ");
								}
								if(rs.getString("LOC_CODE") != null)
								{
									sqlBuff.append( " LOC_CODE = '"+ (rs.getString("LOC_CODE")) +"' , ");
								}

								if(rs.getString("EMP_CODE__IAPR") != null)
								{
									sqlBuff.append( "EMP_CODE__IAPR = '"+ (rs.getString("EMP_CODE__IAPR")) +"' , ");
								}
                               
                                if(rs.getString("EMP_CODE__PUR") != null)
                                {
                                    sqlBuff.append( "EMP_CODE__PUR = '"+ (rs.getString("EMP_CODE__PUR")) +"' , ");
                                }
                                
                                if(rs.getString("EMP_CODE__PLN") != null)
                                {
                                    sqlBuff.append( "EMP_CODE__PLN = '"+ (rs.getString("EMP_CODE__PLN")) +"' , ");
                                }
                                if(rs.getString("SUPP_CODE__PREF") != null)
                                {
                                    sqlBuff.append( "SUPP_CODE__PREF = '"+ (rs.getString("SUPP_CODE__PREF")) +"' , ");
                                }
                               
                                //sqlBuff.append("?,?,?,?" );
                                sqlBuff.append(" START_DEV = ?,START_MFG = ?,START_SALE =?,MFG_DATE_ON = ? ," );
                                sqlBuff.append( " VARIENCE_QTYPER  = "+ (rs.getDouble("VARIENCE_QTYPER")) +" , ");
								sqlBuff.append( " SHELF_LIFE = "+ (rs.getDouble("SHELF_LIFE")) +"  ");
							
								sqlBuff.append( " WHERE ITEM_CODE ='"+tranIdG +"' " ) ;
						
						  System.out.println(" sqlBuff..... :: "+ sqlBuff.toString());	
						  pstmt1 = conn.prepareStatement(sqlBuff.toString());		
                          pstmt1.setTimestamp(1, rs.getTimestamp("START_DEV"));
                          pstmt1.setTimestamp(2, rs.getTimestamp("START_MFG"));
                          pstmt1.setTimestamp(3, rs.getTimestamp("START_SALE"));
                          //pstmt1.setTimestamp(4, rs.getTimestamp("MFG_DATE_ON"));
						  pstmt1.setString(4, rs.getString("MFG_DATE_ON"));
                          up = pstmt1.executeUpdate();
																								   
					  }						  
					  rs.close();
					  pstmt.close();
					  pstmt1.close();
					  if( up == 1	)
					  {
						   System.out.println("Record updated  :"+up);
					  }
            
                 } 
            }
            catch(Exception e)
            {
				retString = (new ITMException(e)).toString();
                System.out.println("Error In Generating Key ::==> "+e);
                e.printStackTrace();
                throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
            }
        finally
            {
                try
                {  //System.out.println("Connection Commited");
                    //conn.commit();`
                  if (pstmt != null)
                    {
                        rs.close();
                        pstmt.close();
                    }
                }
                catch(Exception e){}
            }
	 //retString = tranIdG ;
     System.out.println("generateItemCode :: "+retString);
     return retString ;
    }
    //private String generateItemCode(Node header,Node detail,Connection conn)throws ITMException // through dom
    /*private String generateItemCode(Node header,Node detail,Connection conn)throws ITMException
    {
        Statement stmt = null;
        ResultSet rs = null;
        String itemSer = "",unit = "",locType = "",siteCode = "";
        String retString = "",newItemCode = "",itemType = "";
        String genAttrib = "",sql = "";
        String tempItem = "",existItem = "",phyItem2 = "";
        boolean temp = false;
        ArrayList genAttribList = new ArrayList();
        
        try{
            String [] authencate = new String[2];
            authencate[0] = "";
            authencate[1] = "";
            SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
            itemSer = genericUtility.getColumnValueFromNode("item_ser",header);
            unit = genericUtility.getColumnValueFromNode("unit",detail);
            locType = genericUtility.getColumnValueFromNode("item_ser",detail); 
            siteCode = genericUtility.getColumnValueFromNode("site_code",header);
            itemType = genericUtility.getColumnValueFromNode("item_type",detail);
            tempItem = genericUtility.getColumnValueFromNode("phy_attrib_22",detail);
            existItem = genericUtility.getColumnValueFromNode("phy_attrib_1",detail);
            phyItem2 = genericUtility.getColumnValueFromNode("phy_attrib_2",detail);
            
            
            
            sql = "select gen_attrib from item_type where item_type ='"+itemType+"'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                genAttrib = rs.getString(1); 
            }
            rs.close();
            genAttribList = genericUtility.getTokenList(genAttrib,","); 
                
            sql = "select descr,sh_descr,item_parnt,item_ser,grp_code,sgrp_code,trade_mark,unit, " +
                    "item_type,loc_code,site_code,site_code__own,min_qty,max_qty,reo_qty," +
                    "reo_lev,ordc_perc,stk_opt,price_list,regulated_price ,purc_rate," +
                    "sale_rate,cost_rate,active,item_stru,supp_sour,mfg_lead," +
                    "qc_lead_time,loc_type,tax_chap,tax_class,apr_code,shelf_life," +
                    "min_shelf_life,track_shelf_life,unit__rate,spec_tol,dlv_prd_tol_bef ," +
                    "dlv_prd_tol_aft,bin_no,cycle_count,qc_cycle_time,qc_reqd,spec_reqd,poval_var,appr_supp,market_reg," +
                    "pack_code,pack_instr,chg_date,chg_user,chg_term,yield_perc,bom_code,generic_descr,tech_descr," +
                    "reg_sch,potency_perc,qty_tol_perc,rcp_tol_perc,emp_code__pur,emp_code__pln,cas_no,hsn_no,min_order_qty," +
                    "max_order_qty,pur_lead_time,emp_code__iapr,supp_code__pref,level_code__hier,add_date,add_user," +
                    "add_term,bom_code__benefit,order_opt,integral_qty,min_stk_day,quot_opt,qc_reqd_type,canc_bo_mode," +
                    "indent_opt,benefit_category,qty_sample,net_weight,start_sale,start_dev,start_mfg,mfg_date_on," +
                    "iss_criteria,unit__pur,stop_business,scanned_barcode,po_rate_option,po_rate_varience,emp_code__qcaprv," +
                    "min_no_enquiry,ind_val_to_raise_enq,tax_chap__rep,phy_attrib_1,phy_attrib_2,phy_attrib_3,phy_attrib_4," +
                    "phy_attrib_5,phy_attrib_6,phy_attrib_7,phy_attrib_8,phy_attrib_9,phy_attrib_10,phy_attrib_11," +
                    "phy_attrib_12,mfg_type,sale_option,cust_code__mkt,std_disc,batch_qty_type,role_code__qcaprv," +
                    "role_code__indaprv,max_stk_day,remarks,item_usage,no_sales_month,udf__str1,udf__str2,qc_table," +
                    "item_code__plan,contract_req,varience_qtyper,stab_pattern,ohd_table__post,auto_reqc," +
                    "unit__sal,item_code__ndc,phy_attrib_13,phy_attrib_14,phy_attrib_15,phy_attrib_16,park_type,park_no," +
                    "container_qty,unit__netwt,dimension,unit__dimn,bom_code__std,item_code__al,no_cons_unaprv," +
                    "disapprv_on_rej,no_of_rej,extra_mfg_perc,table_no__detr,appl_seg,phy_attrib_17,phy_attrib_18," +
                    "phy_attrib_19,phy_attrib_20,phy_attrib_21,phy_attrib_22 from item   where item_code = '"+existItem+"'";
            rs = stmt.executeQuery(sql);
            
            System.out.println("temp : "+temp);
            
            if(tempItem.equalsIgnoreCase("N") && rs.next()){
                temp = true;
            }
            
            StringBuffer xmlString= new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            xmlString.append("<DocumentRoot><description>Datawindow Root</description><group0><description>Group0 escription</description>");
            xmlString.append("<Header0>");
            xmlString.append("<description>Header0 members</description>");
            
            xmlString.append("<objName><![CDATA[").append("item").append("]]></objName>");
            xmlString.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
            xmlString.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
            xmlString.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
            xmlString.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
            xmlString.append("<action><![CDATA[").append("SAVE").append("]]></action>");
            xmlString.append("<elementName><![CDATA[").append("").append("]]></elementName>");
            xmlString.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
            xmlString.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
            xmlString.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
            xmlString.append("<forcedSave><![CDATA[").append("true").append("]]></forcedSave>");
            xmlString.append("<taxInFocus><![CDATA[").append("false").append("]]></taxInFocus>");
            
            xmlString.append("<Detail1 dbID='' domID=\"1\" objName=\"item\" objContext=\"1\">");
            xmlString.append("<attribute pkNames=\"item_code:\" status=\"N\" updateFlag=\"A\" selected=\"N\" />");
            xmlString.append("<item_code><![CDATA[").append("").append("]]></item_code>");
            xmlString.append("<active><![CDATA[").append("Y").append("]]></active>");
            xmlString.append("<sh_descr><![CDATA[").append(temp ? rs.getString("sh_descr")+phyItem2:"New Item").append("]]></sh_descr>");
            
            xmlString.append("<item_ser><![CDATA[").append(temp ? (rs.getString("item_ser") == null ? "":rs.getString("item_ser")):itemSer).append("]]></item_ser>");
            xmlString.append("<grp_code><![CDATA[").append(temp ? (rs.getString("grp_code") == null ? "":rs.getString("grp_code")):"").append("]]></grp_code>");
            xmlString.append("<sgrp_code><![CDATA[").append(temp ? (rs.getString("sgrp_code") == null ? "":rs.getString("sgrp_code")):"").append("]]></sgrp_code>");
            xmlString.append("<unit><![CDATA[").append(temp ? (rs.getString("unit") == null ? "":rs.getString("unit")):unit).append("]]></unit>");
            xmlString.append("<item_type><![CDATA[").append(temp ? (rs.getString("item_type") == null ? "":rs.getString("item_type")):genericUtility.getColumnValueFromNode("item_type",detail)).append("]]></item_type>");
            xmlString.append("<loc_code><![CDATA[").append(temp ? (rs.getString("loc_code") == null ? "":rs.getString("loc_code")):"").append("]]></loc_code>");
            xmlString.append("<site_code><![CDATA[").append(temp ? (rs.getString("site_code") == null ? "":rs.getString("site_code")):genericUtility.getColumnValueFromNode("site_code",header)).append("]]></site_code>");
            xmlString.append("<min_qty><![CDATA[").append(temp ? rs.getDouble("min_qty"):0).append("]]></min_qty>");
            xmlString.append("<reo_qty><![CDATA[").append(temp ? rs.getDouble("reo_qty"):0).append("]]></reo_qty>");
            xmlString.append("<reo_lev><![CDATA[").append(temp ? rs.getDouble("reo_lev"):0).append("]]></reo_lev>");
            xmlString.append("<ordc_perc><![CDATA[").append(temp ? rs.getDouble("ordc_perc"):0).append("]]></ordc_perc>");
            xmlString.append("<stk_opt><![CDATA[").append(temp ? rs.getString("stk_opt"):"Y").append("]]></stk_opt>");
            xmlString.append("<price_list><![CDATA[").append(temp ? rs.getString("price_list"):genericUtility.getColumnValueFromNode("price_list",header) == null ? "":genericUtility.getColumnValueFromNode("price_list",header)).append("]]></price_list>");
            xmlString.append("<purc_rate><![CDATA[").append(temp ? rs.getDouble("purc_rate"):0).append("]]></purc_rate>");
            xmlString.append("<sale_rate><![CDATA[").append(temp ? rs.getDouble("sale_rate"):0).append("]]></sale_rate>");
            xmlString.append("<cost_rate><![CDATA[").append(temp ? rs.getDouble("cost_rate"):0).append("]]></cost_rate>");
            xmlString.append("<chg_date/>");
            xmlString.append("<chg_user/>");
            xmlString.append("<chg_term/>");
            xmlString.append("<item_stru><![CDATA[").append(temp ? (rs.getString("item_stru") == null ?"" :rs.getString("item_stru")):"").append("]]></item_stru>");
            xmlString.append("<supp_sour><![CDATA[").append(temp ? (rs.getString("supp_sour") == null ? "" :rs.getString("supp_sour")):"").append("]]></supp_sour>");
            xmlString.append("<mfg_lead><![CDATA[").append(temp ? rs.getInt("mfg_lead"):0).append("]]></mfg_lead>");
            xmlString.append("<loc_type><![CDATA[").append(temp ? (rs.getString("loc_type") == null ? "":rs.getString("loc_type")):(genericUtility.getColumnValueFromNode("loc_type",detail) == null ? "":genericUtility.getColumnValueFromNode("loc_type",detail))).append("]]></loc_type>");
            xmlString.append("<tax_chap><![CDATA[").append(temp ? (rs.getString("tax_chap") == null ? "":rs.getString("tax_chap")):"").append("]]></tax_chap>");
            xmlString.append("<apr_code><![CDATA[").append(temp ? rs.getString("apr_code"):"").append("]]></apr_code>");
            xmlString.append("<itemser_descr><![CDATA[").append("").append("]]></itemser_descr>");
            xmlString.append("<aprlev_descr><![CDATA[").append("").append("]]></aprlev_descr>");
            xmlString.append("<location_descr><![CDATA[").append("").append("]]></location_descr>");
            xmlString.append("<taxchap_descr><![CDATA[").append("").append("]]></taxchap_descr>");
            xmlString.append("<shelf_life><![CDATA[").append(temp ? rs.getInt("shelf_life"):0).append("]]></shelf_life>");
            xmlString.append("<trade_mark><![CDATA[").append(temp ? (rs.getString("trade_mark") == null ? "" :rs.getString("trade_mark")):"").append("]]></trade_mark>");
            xmlString.append("<tax_class><![CDATA[").append(temp ? (rs.getString("tax_class") == null ? "" :rs.getString("tax_class")):"").append("]]></tax_class>");
            xmlString.append("<pack_instr><![CDATA[").append(temp ? (rs.getString("pack_instr") == null ? "" :rs.getString("pack_instr")):"").append("]]></pack_instr>");
            xmlString.append("<unit__rate><![CDATA[").append(temp ? (rs.getString("unit__rate") == null ? "" :rs.getString("unit__rate")):genericUtility.getColumnValueFromNode("unit__rate",detail)).append("]]></unit__rate>");
            xmlString.append("<descr><![CDATA[").append(temp ? rs.getString("descr")+phyItem2:"New Item").append("]]></descr>");
            xmlString.append("<spec_tol><![CDATA[").append(temp ? (rs.getString("spec_tol") == null ? "" :rs.getString("spec_tol")):"").append("]]></spec_tol>");
            xmlString.append("<dlv_prd_tol_bef><![CDATA[").append(temp ? rs.getInt("dlv_prd_tol_bef"):0).append("]]></dlv_prd_tol_bef>");
            
            xmlString.append("<dlv_prd_tol_aft><![CDATA[").append(temp ? rs.getInt("dlv_prd_tol_aft"):0).append("]]></dlv_prd_tol_aft>");
            xmlString.append("<bin_no><![CDATA[").append(temp ? (rs.getString("bin_no") == null ? "" :rs.getString("bin_no")):"").append("]]></bin_no>");
            xmlString.append("<cycle_count><![CDATA[").append(temp ? rs.getInt("cycle_count"):0).append("]]></cycle_count>");
            xmlString.append("<site_code__own><![CDATA[").append(temp ? (rs.getString("site_code__own") == null ? "" :rs.getString("site_code__own")):"").append("]]></site_code__own>");
            xmlString.append("<min_shelf_life><![CDATA[").append(temp ? rs.getDouble("min_shelf_life"):0).append("]]></min_shelf_life>");
            
            
            xmlString.append("<site_descr><![CDATA[").append("").append("]]></site_descr>");
            xmlString.append("<qc_cycle_time><![CDATA[").append(temp ? rs.getInt("qc_cycle_time"):0).append("]]></qc_cycle_time>");
            xmlString.append("<qc_reqd><![CDATA[").append(temp ? (rs.getString("qc_reqd") == null ? "" :rs.getString("qc_reqd")):"").append("]]></qc_reqd>");                              
            
            xmlString.append("<spec_reqd><![CDATA[").append(temp ? (rs.getString("spec_reqd") == null ? "" :rs.getString("spec_reqd")):"").append("]]></spec_reqd>");
            xmlString.append("<poval_var><![CDATA[").append(temp ? rs.getDouble("poval_var"):0).append("]]></poval_var>");
            xmlString.append("<appr_supp><![CDATA[").append(temp ? (rs.getString("appr_supp") == null ? "":rs.getString("appr_supp")):"").append("]]></appr_supp>");
            xmlString.append("<qc_lead_time><![CDATA[").append(temp ? rs.getDouble("qc_lead_time"):0).append("]]></qc_lead_time>");
            xmlString.append("<market_reg><![CDATA[").append(temp ? (rs.getString("market_reg") == null ? "":rs.getString("market_reg")):"").append("]]></market_reg>");
            xmlString.append("<pack_code><![CDATA[").append(temp ? (rs.getString("pack_code") == null ? "" :rs.getString("pack_code")):"").append("]]></pack_code>");
            xmlString.append("<track_shelf_life><![CDATA[").append(temp ? (rs.getString("track_shelf_life") == null ? "" :rs.getString("track_shelf_life")):"").append("]]></track_shelf_life>");
 
//	(temp ? (rs.getString("sgrp_code") == null ? "":rs.getString("sgrp_code")):"")
            xmlString.append("<regulated_price><![CDATA[").append(temp ? (rs.getString("regulated_price") == null ? "":rs.getString("regulated_price")):"").append("]]></regulated_price>");
            xmlString.append("<max_qty><![CDATA[").append(temp ? rs.getDouble("max_qty"):0).append("]]></max_qty>");
            xmlString.append("<bom_code><![CDATA[").append(temp ? (rs.getString("bom_code") == null ? "":rs.getString("bom_code")):"").append("]]></bom_code>");
            xmlString.append("<yield_perc><![CDATA[").append(temp ? rs.getDouble("yield_perc"):0).append("]]></yield_perc>");
            xmlString.append("<generic_descr><![CDATA[").append(temp ? (rs.getString("generic_descr") == null ?"" :rs.getString("generic_descr")):"").append("]]></generic_descr>");
            xmlString.append("<tech_descr><![CDATA[").append(temp ? (rs.getString("tech_descr") == null ? "" :rs.getString("tech_descr")):"").append("]]></tech_descr>");
            xmlString.append("<reg_sch><![CDATA[").append(temp ? (rs.getString("reg_sch") == null ? "" :rs.getString("reg_sch")):"").append("]]></reg_sch>");
            xmlString.append("<potency_perc><![CDATA[").append(temp ? rs.getDouble("potency_perc"):0).append("]]></potency_perc>");
            xmlString.append("<qty_tol_perc><![CDATA[").append(temp ? rs.getDouble("qty_tol_perc"):0).append("]]></qty_tol_perc>");
            xmlString.append("<rcp_tol_perc><![CDATA[").append(temp ? rs.getDouble("rcp_tol_perc"):0).append("]]></rcp_tol_perc>");
            xmlString.append("<emp_code__pur><![CDATA[").append(temp ? (rs.getString("emp_code__pur") == null ?"" : rs.getString("emp_code__pur")):"").append("]]></emp_code__pur>");
            xmlString.append("<emp_code__pln><![CDATA[").append(temp ? (rs.getString("emp_code__pln") == null ? "" :rs.getString("emp_code__pln")):"").append("]]></emp_code__pln>");
            xmlString.append("<cas_no><![CDATA[").append(temp ? (rs.getString("cas_no") == null ? "" :rs.getString("cas_no")) :"").append("]]></cas_no>");
            xmlString.append("<hsn_no><![CDATA[").append(temp ? (rs.getString("hsn_no") == null ? "":rs.getString("hsn_no")):"").append("]]></hsn_no>");
            xmlString.append("<min_order_qty><![CDATA[").append(temp ? rs.getDouble("min_order_qty"):0).append("]]></min_order_qty>");
            xmlString.append("<max_order_qty><![CDATA[").append(temp ? rs.getDouble("max_order_qty"):0).append("]]></max_order_qty>");
            xmlString.append("<pur_lead_time><![CDATA[").append(temp ? rs.getDouble("pur_lead_time"):0).append("]]></pur_lead_time>");
            xmlString.append("<emp_code__iapr><![CDATA[").append(temp ? (rs.getString("emp_code__iapr") == null ? "":rs.getString("emp_code__iapr")):"").append("]]></emp_code__iapr>");
            xmlString.append("<supp_code__pref><![CDATA[").append(temp ? (rs.getString("supp_code__pref") == null ?"":rs.getString("supp_code__pref")) :"").append("]]></supp_code__pref>");
            xmlString.append("<order_opt><![CDATA[").append(temp ? (rs.getString("order_opt") == null ? "":rs.getString("order_opt") ):"").append("]]></order_opt>");
            xmlString.append("<min_stk_day><![CDATA[").append(temp ? rs.getInt("min_stk_day"):0).append("]]></min_stk_day>");
            xmlString.append("<quot_opt><![CDATA[").append(temp ? (rs.getString("quot_opt") == null ? "":rs.getString("quot_opt")):"").append("]]></quot_opt>");
            xmlString.append("<qc_reqd_type><![CDATA[").append(temp ? (rs.getString("qc_reqd_type") == null ? "":rs.getString("qc_reqd_type")):"").append("]]></qc_reqd_type>");
            xmlString.append("<canc_bo_mode><![CDATA[").append(temp ? (rs.getString("canc_bo_mode") == null ? "" :rs.getString("canc_bo_mode")):"").append("]]></canc_bo_mode>");
            xmlString.append("<indent_opt><![CDATA[").append(temp ? (rs.getString("indent_opt") == null ? "" :rs.getString("indent_opt")):"").append("]]></indent_opt>");
            xmlString.append("<qty_sample><![CDATA[").append(temp ? rs.getDouble("qty_sample"):0).append("]]></qty_sample>");
            xmlString.append("<benefit_category><![CDATA[").append(temp ? (rs.getString("benefit_category") == null ? "" :rs.getString("benefit_category")):"").append("]]></benefit_category>");
            xmlString.append("<net_weight><![CDATA[").append(temp ? rs.getDouble("net_weight"):0).append("]]></net_weight>");
            xmlString.append("<mfg_date_on><![CDATA[").append(temp ? (rs.getString("mfg_date_on") == null ? "":rs.getString("mfg_date_on")) :"").append("]]></mfg_date_on>");
            xmlString.append("<start_sale><![CDATA[").append(temp ? (rs.getDate("start_sale") != null ? sdf.format(rs.getDate("start_sale")):""):"").append("]]></start_sale>");
            
            xmlString.append("<start_dev><![CDATA[").append(temp ? (rs.getDate("start_dev") != null ? sdf.format(rs.getDate("start_dev")):""):"").append("]]></start_dev>");
            xmlString.append("<start_mfg><![CDATA[").append(temp ? (rs.getDate("start_mfg") != null ? sdf.format(rs.getDate("start_mfg")):""):"").append("]]></start_mfg>");
            xmlString.append("<integral_qty><![CDATA[").append(temp ? rs.getDouble("integral_qty"):0).append("]]></integral_qty>");
            xmlString.append("<iss_criteria><![CDATA[").append(temp ? (rs.getString("iss_criteria") ==null ? "":rs.getString("iss_criteria")) :"").append("]]></iss_criteria>");
            xmlString.append("<unit__pur><![CDATA[").append(temp ? (rs.getString("unit__pur") == null ? "" :rs.getString("unit__pur")):"").append("]]></unit__pur>");
            xmlString.append("<stop_business><![CDATA[").append(temp ? (rs.getString("stop_business") == null ? "" :rs.getString("stop_business") ):"").append("]]></stop_business>");
            xmlString.append("<po_rate_option><![CDATA[").append(temp ? (rs.getString("po_rate_option") == null ? "" :rs.getString("po_rate_option")):"").append("]]></po_rate_option>");
            xmlString.append("<po_rate_varience><![CDATA[").append(temp ? rs.getDouble("po_rate_varience"):0).append("]]></po_rate_varience>");
            xmlString.append("<emp_code__qcaprv><![CDATA[").append("").append("]]></emp_code__qcaprv>");
            xmlString.append("<min_no_enquiry><![CDATA[").append(temp ? rs.getInt("min_no_enquiry"):0).append("]]></min_no_enquiry>");
            xmlString.append("<ind_val_to_raise_enq><![CDATA[").append(temp ? rs.getDouble("ind_val_to_raise_enq"):0).append("]]></ind_val_to_raise_enq>");
            xmlString.append("<tax_chap__rep><![CDATA[").append(temp ? (rs.getString("tax_chap__rep") == null ? "":rs.getString("tax_chap__rep")):"").append("]]></tax_chap__rep>");
            xmlString.append("<phy_attrib_1><![CDATA[").append(genAttribList.contains("1") ? genericUtility.getColumnValueFromNode("phy_attrib_1",detail):"").append("]]></phy_attrib_1>");
            xmlString.append("<phy_attrib_2><![CDATA[").append(genAttribList.contains("2") ? genericUtility.getColumnValueFromNode("phy_attrib_2",detail):"").append("]]></phy_attrib_2>");
            xmlString.append("<phy_attrib_3><![CDATA[").append(genAttribList.contains("3") ? genericUtility.getColumnValueFromNode("phy_attrib_3",detail):"").append("]]></phy_attrib_3>");
            xmlString.append("<phy_attrib_4><![CDATA[").append(genAttribList.contains("4") ? genericUtility.getColumnValueFromNode("phy_attrib_4",detail):"").append("]]></phy_attrib_4>");
            xmlString.append("<phy_attrib_5><![CDATA[").append(genAttribList.contains("5") ? genericUtility.getColumnValueFromNode("phy_attrib_5",detail):"").append("]]></phy_attrib_5>");
            xmlString.append("<phy_attrib_6><![CDATA[").append(genAttribList.contains("6") ? genericUtility.getColumnValueFromNode("phy_attrib_6",detail):"").append("]]></phy_attrib_6>");
            xmlString.append("<phy_attrib_7><![CDATA[").append(genAttribList.contains("7") ? genericUtility.getColumnValueFromNode("phy_attrib_7",detail):"").append("]]></phy_attrib_7>");
            xmlString.append("<phy_attrib_8><![CDATA[").append(genAttribList.contains("8") ? genericUtility.getColumnValueFromNode("phy_attrib_8",detail):"").append("]]></phy_attrib_8>");
            xmlString.append("<phy_attrib_9><![CDATA[").append(genAttribList.contains("9") ? genericUtility.getColumnValueFromNode("phy_attrib_9",detail):"").append("]]></phy_attrib_9>");
            xmlString.append("<phy_attrib_10><![CDATA[").append(genAttribList.contains("10") ? genericUtility.getColumnValueFromNode("phy_attrib_10",detail):"").append("]]></phy_attrib_10>");
            xmlString.append("<phy_attrib_11><![CDATA[").append(genAttribList.contains("11") ? genericUtility.getColumnValueFromNode("phy_attrib_11",detail):"").append("]]></phy_attrib_11>");
            xmlString.append("<phy_attrib_12><![CDATA[").append(genAttribList.contains("12") ? genericUtility.getColumnValueFromNode("phy_attrib_12",detail):"").append("]]></phy_attrib_12>");
            
            xmlString.append("<mfg_type><![CDATA[").append(temp ? (rs.getString("mfg_type") == null ? "" :rs.getString("mfg_type")):"").append("]]></mfg_type>");
            xmlString.append("<sale_option><![CDATA[").append(temp ? (rs.getString("sale_option") == null ?"" :rs.getString("sale_option")):"").append("]]></sale_option>");
            xmlString.append("<item_std_disc><![CDATA[").append("").append("]]></item_std_disc>");
            xmlString.append("<role_code__qcaprv><![CDATA[").append(temp ? (rs.getString("role_code__qcaprv") == null ? "" :rs.getString("role_code__qcaprv")):"").append("]]></role_code__qcaprv>");
            xmlString.append("<role_code__indaprv><![CDATA[").append(temp ? (rs.getString("role_code__indaprv") == null ? "" :rs.getString("role_code__indaprv")):"").append("]]></role_code__indaprv>");
            xmlString.append("<batch_qty_type><![CDATA[").append(temp ? (rs.getString("batch_qty_type") == null ? "" :rs.getString("batch_qty_type")):"").append("]]></batch_qty_type>");
            xmlString.append("<max_stk_day><![CDATA[").append(temp ? rs.getInt("max_stk_day"):0).append("]]></max_stk_day>");
            xmlString.append("<item_usage><![CDATA[").append(temp ? (rs.getString("item_usage") == null ? "" :rs.getString("item_usage")):"").append("]]></item_usage>");
            xmlString.append("<remarks><![CDATA[").append(temp ? (rs.getString("remarks") == null ? "" :rs.getString("remarks")):"").append("]]></remarks>");
            xmlString.append("<no_sales_month><![CDATA[").append(temp ? rs.getInt(121):0).append("]]></no_sales_month>");
            xmlString.append("<qc_table><![CDATA[").append(temp ? (rs.getString("qc_table") == null ? "" :rs.getString("qc_table")):"").append("]]></qc_table>");
            xmlString.append("<item_code__plan><![CDATA[").append(temp ? (rs.getString("item_code__plan") == null ? "" :rs.getString("item_code__plan")):"").append("]]></item_code__plan>");
            xmlString.append("<contract_req><![CDATA[").append(temp ? (rs.getString("contract_req") == null ? "" :rs.getString("contract_req")):"").append("]]></contract_req>");
            xmlString.append("<varience_qtyper><![CDATA[").append(temp ? rs.getDouble("varience_qtyper"):0).append("]]></varience_qtyper>");
            xmlString.append("<site_b_site_descr><![CDATA[").append("").append("]]></site_b_site_descr>");
            xmlString.append("<stab_pattern><![CDATA[").append(temp ? (rs.getString("stab_pattern") == null ? "" :rs.getString("stab_pattern")):"").append("]]></stab_pattern>");
            xmlString.append("<auto_reqc><![CDATA[").append(temp ? (rs.getString("auto_reqc") == null ? "" :rs.getString("auto_reqc")):"").append("]]></auto_reqc>");
            xmlString.append("<ohd_table__post><![CDATA[").append(temp ? (rs.getString("ohd_table__post") == null ? "" :rs.getString("ohd_table__post")):"").append("]]></ohd_table__post>");
            xmlString.append("<unit__sal><![CDATA[").append(temp ? (rs.getString("unit__sal") == null ? "":rs.getString("unit__sal")):"").append("]]></unit__sal>");
            xmlString.append("<unit__netwt><![CDATA[").append(temp ? (rs.getString("unit__netwt") == null ? "" :rs.getString("unit__netwt") ):"").append("]]></unit__netwt>");
            xmlString.append("<dimension><![CDATA[").append(temp ? (rs.getString("dimension") == null ? "" :rs.getString("dimension") ):"").append("]]></dimension>");
            xmlString.append("<unit__dimn><![CDATA[").append(temp ? (rs.getString("unit__dimn") == null ? "" :rs.getString("unit__dimn")):"").append("]]></unit__dimn>");
            xmlString.append("<item_code__al><![CDATA[").append(temp ? (rs.getString("item_code__al")== null ? "" :rs.getString("item_code__al")):"").append("]]></item_code__al>");
            xmlString.append("<scanned_barcode><![CDATA[").append(temp ? (rs.getString("scanned_barcode") == null ? "" :rs.getString("scanned_barcode")):"").append("]]></scanned_barcode>");
            xmlString.append("<bom_code__std><![CDATA[").append(temp ? (rs.getString("bom_code__std") == null ? "" :rs.getString("bom_code__std")):"").append("]]></bom_code__std>");
            xmlString.append("<cust_code__mkt><![CDATA[").append(temp ? (rs.getString("cust_code__mkt") == null ? "":rs.getString("cust_code__mkt")):"").append("]]></cust_code__mkt>");
            //xmlString.append("<phy_attrib__lab1><![CDATA[").append("").append("]]></phy_attrib__lab1>");
            //xmlString.append("<phy_attrib__lab2><![CDATA[").append("").append("]]></phy_attrib__lab2>");
            //xmlString.append("<phy_attrib__lab3><![CDATA[").append("").append("]]></phy_attrib__lab3>");
            //xmlString.append("<phy_attrib__lab4><![CDATA[").append("").append("]]></phy_attrib__lab4>");
            //xmlString.append("<phy_attrib__lab5><![CDATA[").append("").append("]]></phy_attrib__lab5>");
            //xmlString.append("<phy_attrib__lab6><![CDATA[").append("").append("]]></phy_attrib__lab6>");
            //xmlString.append("<phy_attrib__lab7><![CDATA[").append("").append("]]></phy_attrib__lab7>");
            //xmlString.append("<phy_attrib__lab8><![CDATA[").append("").append("]]></phy_attrib__lab8>");
            //xmlString.append("<phy_attrib__lab9><![CDATA[").append("").append("]]></phy_attrib__lab9>");
            //xmlString.append("<phy_attrib__lab10><![CDATA[").append("").append("]]></phy_attrib__lab10>");
            //xmlString.append("<phy_attrib__lab11><![CDATA[").append("").append("]]></phy_attrib__lab11>");
            //xmlString.append("<phy_attrib__lab12><![CDATA[").append("").append("]]></phy_attrib__lab12>");            
  
            xmlString.append("<no_cons_unaprv><![CDATA[").append(temp ? rs.getDouble("no_cons_unaprv"):0).append("]]></no_cons_unaprv>");
            xmlString.append("<disapprv_on_rej><![CDATA[").append(temp ? rs.getDouble("disapprv_on_rej"):0).append("]]></disapprv_on_rej>");
            xmlString.append("<no_of_rej><![CDATA[").append(temp ? rs.getDouble("no_of_rej"):0).append("]]></no_of_rej>");
            xmlString.append("<extra_mfg_perc><![CDATA[").append(temp ? rs.getDouble("extra_mfg_perc"):0).append("]]></extra_mfg_perc>");
            xmlString.append("<table_no__detr><![CDATA[").append(temp ? (rs.getString("table_no__detr") == null ? "":rs.getString("table_no__detr")):"").append("]]></table_no__detr>");
            xmlString.append("<appl_seg><![CDATA[").append(temp ? (rs.getString("appl_seg") == null ?"" :rs.getString("appl_seg")):"").append("]]></appl_seg>");
            
            xmlString.append("<phy_attrib_17><![CDATA[").append(genAttribList.contains("17") ? genericUtility.getColumnValueFromNode("phy_attrib_17",detail):"").append("]]></phy_attrib_17>");
            xmlString.append("<phy_attrib_18><![CDATA[").append(genAttribList.contains("18") ? genericUtility.getColumnValueFromNode("phy_attrib_18",detail):"").append("]]></phy_attrib_18>");
            xmlString.append("<phy_attrib_19><![CDATA[").append(genAttribList.contains("19") ? genericUtility.getColumnValueFromNode("phy_attrib_19",detail):"").append("]]></phy_attrib_19>");
            xmlString.append("<phy_attrib_20><![CDATA[").append(genAttribList.contains("20") ? genericUtility.getColumnValueFromNode("phy_attrib_20",detail):"").append("]]></phy_attrib_20>");
            xmlString.append("<phy_attrib_21><![CDATA[").append(genAttribList.contains("21") ? genericUtility.getColumnValueFromNode("phy_attrib_21",detail):"").append("]]></phy_attrib_21>");
            xmlString.append("<phy_attrib_22><![CDATA[").append(genAttribList.contains("22") ? genericUtility.getColumnValueFromNode("phy_attrib_22",detail):"").append("]]></phy_attrib_22>");
             //xmlString.append("<phy_attrib_1><![CDATA[").append(genAttribList.contains("1") ? genericUtility.getColumnValueFromNode("phy_attrib_1",detail):"").append("]]></phy_attrib_1>");
			//xmlString.append("<phy_attrib__lab13><![CDATA[").append("").append("]]></phy_attrib__lab13>");
            //xmlString.append("<phy_attrib__lab14><![CDATA[").append("").append("]]></phy_attrib__lab14>");
            //xmlString.append("<phy_attrib__lab15><![CDATA[").append("").append("]]></phy_attrib__lab15>");
            //xmlString.append("<phy_attrib__lab16><![CDATA[").append("").append("]]></phy_attrib__lab16>");
            //xmlString.append("<phy_attrib__lab17><![CDATA[").append("").append("]]></phy_attrib__lab17>");
            //xmlString.append("<phy_attrib__lab18><![CDATA[").append("").append("]]></phy_attrib__lab18>");
            //xmlString.append("<phy_attrib__lab19><![CDATA[").append("").append("]]></phy_attrib__lab19>");
            //xmlString.append("<phy_attrib__lab20><![CDATA[").append("").append("]]></phy_attrib__lab20>");
            //xmlString.append("<phy_attrib__lab21><![CDATA[").append("").append("]]></phy_attrib__lab21>");
            //xmlString.append("<phy_attrib__lab22><![CDATA[").append("").append("]]></phy_attrib__lab22>");
            xmlString.append("<phy_attrib_13><![CDATA[").append(genAttribList.contains("13") ? genericUtility.getColumnValueFromNode("phy_attrib_13",detail):"").append("]]></phy_attrib_13>");
            xmlString.append("<phy_attrib_14><![CDATA[").append(genAttribList.contains("14") ? genericUtility.getColumnValueFromNode("phy_attrib_14",detail):"").append("]]></phy_attrib_14>");
            xmlString.append("<phy_attrib_15><![CDATA[").append(genAttribList.contains("15") ? genericUtility.getColumnValueFromNode("phy_attrib_15",detail):"").append("]]></phy_attrib_15>");
            xmlString.append("<phy_attrib_16><![CDATA[").append(genAttribList.contains("16") ? genericUtility.getColumnValueFromNode("phy_attrib_16",detail):"").append("]]></phy_attrib_16>");
            xmlString.append("</Detail1>");
            xmlString.append("</Header0></group0></DocumentRoot>");
            
            
            InitialContext ctx = null;
            AppConnectParm appConnect = new AppConnectParm();
            ctx = new InitialContext(appConnect.getProperty());
            masterStatefulHome = (MasterStatefulHome)ctx.lookup("MasterStateful");
            masterStateful = masterStatefulHome.create(); 
            
            retString = masterStateful.processRequest(authencate, siteCode, true, xmlString.toString());
            
            if(retString.indexOf("Success") == -1){
                return retString;
            }
            if(retString.indexOf("Success") != -1){
                Document dom = genericUtility.parseString(retString);
                Node nodeTranid = dom.getElementsByTagName("Root").item(0);
                newItemCode = genericUtility.getColumnValueFromNode("TranID",nodeTranid);
            }
        }catch(ITMException ie){
            System.out.println("ITMException "+ie);
            ie.printStackTrace();
            throw(ie);
        }catch(Exception e){
            System.out.println("Exception "+e);
            e.printStackTrace();
            throw new ITMException(e);
        }
        finally{
            try{
                masterStateful.remove();
                masterStateful = null;
            }catch(Exception e){System.out.print("Exception in Finally :"+e);}
        }
        return newItemCode;
    }*/
    
    private void updateNode(String itemCode,Node detail) throws ITMException
    {
        NodeList fieldNodes = null;
        Node field = null;
        String fieldName = "";
        try{
            fieldNodes = detail.getChildNodes();
            for(int i = 0;i < fieldNodes.getLength();i++){
                field = fieldNodes.item(i);
                fieldName = field.getNodeName();
                if(fieldName.equalsIgnoreCase("item_code")){
                    field.getFirstChild().setNodeValue(itemCode);                    
                }
            }
        }catch(Exception e){
            System.out.println("Exception "+e);
            e.printStackTrace();
            throw new ITMException(e);
        }
    }
    
    private String generateSaleOrder(Node tranNode,Connection conn) throws ITMException
    {
        PreparedStatement pstmt = null;
		String retString = "",tranNodeString = "",siteCode = "",sordatt_no_tranid ="",sql ="",itemCodeDescr ="";
		int upd=0;
        NodeList detailListNodes = null;
        try{
            SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
            String currDate = sdf.format(new java.sql.Date(System.currentTimeMillis()));
            String [] authencate = new String[2];
            authencate[0] = "";
            authencate[1] = "";
            detailListNodes = tranNode.getChildNodes();
            tranNodeString = serializeDom(tranNode);
			
            StringBuffer xmlString= new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            xmlString.append("<DocumentRoot><description>Datawindow Root</description><group0><description>Group0 escription</description>");
            xmlString.append("<Header0>");
            xmlString.append("<description>Header0 members</description>");
            
            xmlString.append("<objName><![CDATA[").append("sorder").append("]]></objName>");
            xmlString.append("<pageContext><![CDATA[").append("1").append("]]></pageContext>");
            xmlString.append("<objContext><![CDATA[").append("1").append("]]></objContext>");
            xmlString.append("<editFlag><![CDATA[").append("A").append("]]></editFlag>");
            xmlString.append("<focusedColumn><![CDATA[").append("").append("]]></focusedColumn>");
            xmlString.append("<action><![CDATA[").append("SAVE").append("]]></action>");
            xmlString.append("<elementName><![CDATA[").append("").append("]]></elementName>");
            xmlString.append("<keyValue><![CDATA[").append("1").append("]]></keyValue>");
            xmlString.append("<taxKeyValue><![CDATA[").append("").append("]]></taxKeyValue>");
            xmlString.append("<saveLevel><![CDATA[").append("1").append("]]></saveLevel>");
            xmlString.append("<forcedSave><![CDATA[").append(true).append("]]></forcedSave>");
            xmlString.append("<taxInFocus><![CDATA[").append(false).append("]]></taxInFocus>");
            
            xmlString.append("<Detail1 dbID='' domID=\"1\" objName=\"sorder\" objContext=\"1\">");
            xmlString.append("<attribute pkNames=\"sale_order:\" status=\"N\" updateFlag=\"A\" selected=\"N\" />");
            
            xmlString.append(tranNodeString.substring(tranNodeString.indexOf("</tran_id>")+10,tranNodeString.indexOf("</Detail1>")+10));
            
            for(int detCnt = 0;detCnt < detailListNodes.getLength(); detCnt++){
                Node currNode = detailListNodes.item(detCnt);
                Node currHdr = detailListNodes.item(0);
                siteCode = genericUtility.getColumnValueFromNode("site_code",currHdr);
				sordatt_no_tranid = genericUtility.getColumnValueFromNode("tran_id",currNode);
                if(currNode.getNodeName().equalsIgnoreCase("Detail2")){
                    tranNodeString = serializeDom(currNode);
                    xmlString.append("<Detail2 dbID=':' domID='1' objName='sorder' objContext='2'>");
                    xmlString.append("<attribute pkNames='sale_order:line_no:' status='N' updateFlag='A' selected='N' />");
                    xmlString.append("<sale_order><![CDATA[").append("").append("]]></sale_order>");
                    xmlString.append("<line_no><![CDATA[").append(detCnt).append("]]></line_no>");	 //+1
                    xmlString.append("<site_code><![CDATA[").append(genericUtility.getColumnValueFromNode("site_code",currHdr) ==null ? "":genericUtility.getColumnValueFromNode("site_code",currHdr)).append("]]></site_code>");
                    xmlString.append("<item_code><![CDATA[").append(genericUtility.getColumnValueFromNode("item_code",currNode) ==null ? "":genericUtility.getColumnValueFromNode("item_code",currNode) ).append("]]></item_code>");
                    xmlString.append("<item_flg><![CDATA[").append(genericUtility.getColumnValueFromNode("item_flg",currNode) == null ? "I":genericUtility.getColumnValueFromNode("item_flg",currNode)).append("]]></item_flg>");
                    xmlString.append("<quantity><![CDATA[").append(genericUtility.getColumnValueFromNode("quantity",currNode) ==null ? "0":genericUtility.getColumnValueFromNode("quantity",currNode)).append("]]></quantity>");
                    xmlString.append("<unit><![CDATA[").append(genericUtility.getColumnValueFromNode("unit",currNode) ==null ?"":genericUtility.getColumnValueFromNode("unit",currNode)).append("]]></unit>");
                    xmlString.append("<dsp_date><![CDATA[").append(genericUtility.getColumnValueFromNode("due_date",currNode) == null ? currDate:genericUtility.getColumnValueFromNode("due_date",currNode)).append("]]></dsp_date>");
                    xmlString.append("<rate><![CDATA[").append(genericUtility.getColumnValueFromNode("rate",currNode)== null ? "0":genericUtility.getColumnValueFromNode("rate",currNode)).append("]]></rate>");
                    xmlString.append("<discount><![CDATA[").append(genericUtility.getColumnValueFromNode("discount",currNode) ==null ? "":genericUtility.getColumnValueFromNode("discount",currNode)).append("]]></discount>");

					xmlString.append("<tax_amt><![CDATA[").append("").append("]]></tax_amt>");
                    xmlString.append("<tax_class><![CDATA[").append(" ").append("]]></tax_class>");
                    xmlString.append("<tax_chap><![CDATA[").append("").append("]]></tax_chap>");
                    xmlString.append("<tax_env><![CDATA[").append("").append("]]></tax_env>");
                    xmlString.append("<net_amt><![CDATA[").append("").append("]]></net_amt>");
                    xmlString.append("<remarks><![CDATA[").append("").append("]]></remarks>");
                    xmlString.append("<status><![CDATA[").append("").append("]]></status>");
                    xmlString.append("<status_date><![CDATA[").append(genericUtility.getColumnValueFromNode("status_date",currNode)).append("]]></status_date>");
                    xmlString.append("<chg_date/>");
                    xmlString.append("<chg_user/>");
                    xmlString.append("<chg_term/>");

					//added by rajendra
					String itemCode =genericUtility.getColumnValueFromNode("item_code",currNode);
					if(itemCode !=null && itemCode.trim().length() >0)
					{
						sql ="select DESCR from item where item_code ='"+itemCode  +"' ";
						System.out.println("sql :: "+sql);
						pstmt = conn.prepareStatement(sql);
						rs = pstmt.executeQuery(); 
						if(rs.next())
						{
							itemCodeDescr =rs.getString(1);
						}
						System.out.println("itemCodeDescr..... :: "+itemCodeDescr);
						rs.close();
						pstmt.close();
					}
					String itemDescr =genericUtility.getColumnValueFromNode("item_descr",currNode);
					System.out.println("itemDescr..... :: "+itemDescr);
					System.out.println("itemDescr length..... :: "+itemDescr.length());
                    //xmlString.append("<item_descr><![CDATA[").append(genericUtility.getColumnValueFromNode("item_descr",currNode) == null ? itemCodeDescr :genericUtility.getColumnValueFromNode("item_descr",currNode)).append("]]></item_descr>");     
					if(itemDescr != null &&  itemDescr.trim().length() > 0)
					{	 xmlString.append("<item_descr><![CDATA[").append(itemDescr).append("]]></item_descr>");
						
					}
					else
					{
						xmlString.append("<item_descr><![CDATA[").append(itemCodeDescr).append("]]></item_descr>");
						 System.out.println("itemDescr.inside.... :: "+itemDescr);
					}
					//ended by rajendra 29/05/07
					xmlString.append("<unit__rate><![CDATA[").append(genericUtility.getColumnValueFromNode("unit__rate",currNode) ==null ? "":genericUtility.getColumnValueFromNode("unit__rate",currNode)).append("]]></unit__rate>");
                    xmlString.append("<conv__qty_stduom><![CDATA[").append(genericUtility.getColumnValueFromNode("conv__qty_stduom",currNode) ==null ? "0" :genericUtility.getColumnValueFromNode("conv__qty_stduom",currNode)).append("]]></conv__qty_stduom>");
                    xmlString.append("<conv__rtuom_stduom><![CDATA[").append(genericUtility.getColumnValueFromNode("conv__rtuom_stduom",currNode) ==null ? "0" :genericUtility.getColumnValueFromNode("conv__rtuom_stduom",currNode)).append("]]></conv__rtuom_stduom>");
                    xmlString.append("<unit__std><![CDATA[").append(genericUtility.getColumnValueFromNode("unit__std",currNode) ==null ? "":genericUtility.getColumnValueFromNode("unit__std",currNode)).append("]]></unit__std>");
                    xmlString.append("<quantity__stduom><![CDATA[").append(genericUtility.getColumnValueFromNode("quantity__stduom",currNode) ==null ? "0":genericUtility.getColumnValueFromNode("quantity__stduom",currNode)).append("]]></quantity__stduom>");
                    xmlString.append("<rate__stduom><![CDATA[").append(genericUtility.getColumnValueFromNode("rate__stduom",currNode) ==null ? "0":genericUtility.getColumnValueFromNode("rate__stduom",currNode)).append("]]></rate__stduom>");
                    xmlString.append("<no_art><![CDATA[").append(genericUtility.getColumnValueFromNode("no_art",currNode) == null ? "0":genericUtility.getColumnValueFromNode("no_art",currNode)).append("]]></no_art>");
                    xmlString.append("<pack_code><![CDATA[").append("").append("]]></pack_code>");
                    xmlString.append("<line_no__contr><![CDATA[").append("").append("]]></line_no__contr>");
                    xmlString.append("<pack_instr><![CDATA[").append(genericUtility.getColumnValueFromNode("pack_instr",currNode) == null ? "":genericUtility.getColumnValueFromNode("pack_instr",currNode)).append("]]></pack_instr>");
                    xmlString.append("<spec_ref><![CDATA[").append("").append("]]></spec_ref>");
                    xmlString.append("<pack_qty><![CDATA[").append("").append("]]></pack_qty>");
                    xmlString.append("<item_ser><![CDATA[").append(genericUtility.getColumnValueFromNode("item_ser",currHdr) ==null ? "":genericUtility.getColumnValueFromNode("item_ser",currHdr)).append("]]></item_ser>");
                    xmlString.append("<rate__clg><![CDATA[").append(genericUtility.getColumnValueFromNode("rate__clg",currNode) == null ? "0":genericUtility.getColumnValueFromNode("rate__clg",currNode)).append("]]></rate__clg>");
                    xmlString.append("<mfg_code><![CDATA[").append("").append("]]></mfg_code>");
                    xmlString.append("<contract_no><![CDATA[").append("").append("]]></contract_no>");
                    xmlString.append("<packing_descr><![CDATA[").append("").append("]]></packing_descr>");
                    xmlString.append("<spec_id><![CDATA[").append("").append("]]></spec_id>");
                    xmlString.append("<ord_value><![CDATA[").append("").append("]]></ord_value>");
                    
                    xmlString.append("<item_ser__prom><![CDATA[").append("").append("]]></item_ser__prom>");
                    xmlString.append("<specific_instr><![CDATA[").append("").append("]]></specific_instr>");
                    xmlString.append("<item_code__ord><![CDATA[").append(genericUtility.getColumnValueFromNode("item_code",currNode) ==null ? "":genericUtility.getColumnValueFromNode("item_code",currNode)).append("]]></item_code__ord>");
                    xmlString.append("<pallet_code><![CDATA[").append("").append("]]></pallet_code>");
                    xmlString.append("<no_pallet><![CDATA[").append("").append("]]></no_pallet>");
                    xmlString.append("<over_ship_perc><![CDATA[").append("").append("]]></over_ship_perc>");
                    xmlString.append("<comm_perc_1><![CDATA[").append("0").append("]]></comm_perc_1>");
                    xmlString.append("<comm_perc_2><![CDATA[").append("0").append("]]></comm_perc_2>");
                    xmlString.append("<comm_perc_3><![CDATA[").append("0").append("]]></comm_perc_3>");
                    xmlString.append("<comm_perc_on_1><![CDATA[").append("").append("]]></comm_perc_on_1>");
                    xmlString.append("<comm_perc_on_2><![CDATA[").append("").append("]]></comm_perc_on_2>");
                    xmlString.append("<comm_perc_on_3><![CDATA[").append("").append("]]></comm_perc_on_3>");
                    xmlString.append("<sales_pers_comm_1><![CDATA[").append("0").append("]]></sales_pers_comm_1>");
                    xmlString.append("<sales_pers_comm_2><![CDATA[").append("0").append("]]></sales_pers_comm_2>");
                    xmlString.append("<sales_pers_comm_3><![CDATA[").append("0").append("]]></sales_pers_comm_3>");
                    xmlString.append("<price_list__disc><![CDATA[").append("").append("]]></price_list__disc>");
                    xmlString.append("<rate__std><![CDATA[").append(genericUtility.getColumnValueFromNode("rate__std",currNode) == null ? "0":genericUtility.getColumnValueFromNode("rate__std",currNode)).append("]]></rate__std>");
                    xmlString.append("<fin_scheme><![CDATA[").append("").append("]]></fin_scheme>");
                    xmlString.append("<max_shelf_life><![CDATA[").append("0").append("]]></max_shelf_life>");
                    xmlString.append("<min_shelf_life><![CDATA[").append("").append("]]></min_shelf_life>");
                    xmlString.append("<item_specs><![CDATA[").append("").append("]]></item_specs>");
                    xmlString.append("<st_shrink><![CDATA[").append("").append("]]></st_shrink>");
                    xmlString.append("<loc_type><![CDATA[").append("").append("]]></loc_type>");
                    xmlString.append("<st_scheme><![CDATA[").append("").append("]]></st_scheme>");
					//modify by rajendra 9/05/07
					
                    xmlString.append("<sordform_no><![CDATA[").append(genericUtility.getColumnValueFromNode("tran_id",currNode)).append("]]></sordform_no>");
                    xmlString.append("<line_no__sform><![CDATA[").append(genericUtility.getColumnValueFromNode("line_no",currNode)).append("]]></line_no__sform>");
                    //modify end  by rajendra 9/05/07
					xmlString.append("<stk_qty><![CDATA[").append("").append("]]></stk_qty>");
                    xmlString.append("<appl_seg><![CDATA[").append("").append("]]></appl_seg>");
                    xmlString.append("<uom_descr><![CDATA[").append("").append("]]></uom_descr>");
                    xmlString.append("</Detail2>");
                }
            }
            xmlString.append("</Header0></group0></DocumentRoot>");
            System.out.println("xmlString For generation Sale Order:: "+xmlString.toString());
            
            InitialContext ctx = null;
            AppConnectParm appConnect = new AppConnectParm();
            ctx = new InitialContext(appConnect.getProperty());
			
            masterStatefulLocal = (MasterStatefulLocal)ctx.lookup("ibase/MasterStatefulEJB/local"); // for ejb3 on 3/31/2009
            //masterStateful = masterStatefulHome.create(); //for ejb3
          //Changes By Ajay on 29-12-2017:START passes false and conn to processRequest
            retString = masterStatefulLocal.processRequest(authencate , siteCode, true, xmlString.toString(),false,conn); // for ejb3 on 3/31/2009
          //Changes By Ajay on 29-12-2017:END 
            //added by rajendra 10/05/07
            System.out.println("retString....qqq:: "+retString);
			if(retString.indexOf("Success") != -1)
			{
				//<Root><Detail>Success</Detail><TranID>0028</TranID></Root>:
				int  d =retString.indexOf("<TranID>");
				int f=	 retString.indexOf("</TranID>");
				String saleOrder1 = retString.substring(d+8,f);
				System.out.println("saleOrder1......... "+saleOrder1);
				sql ="update sorder set sordatt_no ='"+sordatt_no_tranid +"',confirmed ='N'  where sale_order ='"+saleOrder1+"' ";
				System.out.println("sql :: "+sql);
				pstmt = conn.prepareStatement(sql);
				upd = pstmt.executeUpdate();  
				System.out.println("updated.....sorder :: "+upd);
				pstmt.close();
			}
			//ended  by rajendra 10/05/07
        }catch(ITMException ie){
            System.out.println("ITMException "+ie);
            ie.printStackTrace();
            throw(ie);
        }catch(Exception e){
            System.out.println("Exception "+e);
            e.printStackTrace();
            throw new ITMException(e);
        }
        finally{
            try{
				masterStatefulLocal.remove();  // for ejb3 inplace of masterStateful.remove();
                masterStatefulLocal = null;     // for ejb3 masterStateful= null ;           
            }catch(Exception e){System.out.println("Exception e"+e);}
        }
        return retString;        
    }
    
    private String serializeDom(Node dom)throws ITMException
    {
        String retString = null;
        try
        {
            
		ByteArrayOutputStream out = new ByteArrayOutputStream();
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            serializer.transform(new DOMSource(dom), new javax.xml.transform.stream.StreamResult(out));
            retString = out.toString();
            out.flush();
            out.close();
            out = null;
        }
        catch (Exception e)
        {
            System.out.println("Exception : MasterStatefulEJB : serializeDom :"+e);
            throw new ITMException(e);
        }
        return retString;
    }
}
