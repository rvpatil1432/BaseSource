package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.utility.CommonConstants;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.ejb.ITMDBAccessEJB;

import java.rmi.RemoteException;

import javax.naming.InitialContext;
import javax.ejb.Stateless;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

import javax.xml.rpc.ParameterMode;

import java.text.SimpleDateFormat;

//import ibase.utility.GenericUtility;
import ibase.webitm.ejb.MasterStatefulLocal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

//import javax.ejb.Stateless;
//import com.ibm.db2.jcc.b.SQLException;

@Stateless
public class PlistGenerationConf extends ActionHandlerEJB implements PlistGenerationConfLocal, PlistGenerationConfRemote
{
	
	String userId = "", termId = "", lckGroup = "", loginSite = "",
	        loginEmpCode = "";
	ibase.utility.E12GenericUtility genericUtility = new ibase.utility.E12GenericUtility();
	CommonConstants commonConstants = new CommonConstants();
	ibase.webitm.ejb.sys.UtilMethods utilMethods = ibase.webitm.ejb.sys.UtilMethods.getInstance();

	String autoConfReqd = "", validUpto = "", exclSeries = "", finEntStr = "",
	        itemLotReqd = "", defSiteOwn = "";

	
	public String confirm(String tranID, String xtraParams, String forcedFlag) throws RemoteException, ITMException
	{
		String retString = "";
		boolean isConn = false;
		Connection conn = null;

		try
		{
			retString = confirm(tranID, xtraParams, forcedFlag, conn, isConn);

			if (retString != null && retString.length() > 0)
			{
				throw new Exception("Exception while calling confirm for tran  Id:[" + tranID + "]");
			}
		} catch (Exception exception)
		{
			System.out.println("Exception in [InvHoldConfEJB] getCurrdateAppFormat " + exception.getMessage());
		}
		return retString;
	}

	public String confirm(String tranId, String xtraParams, String forcedFlag, Connection conn, boolean connStatus) throws RemoteException, ITMException
	{
		PreparedStatement pstmtSql = null, pstmt1 = null, pstmt2 = null, pstmt3 = null,pstmt4=null;
		ResultSet rs = null, rs1 = null, rs2 = null,rs3=null;
//		FinCommon finCommon = null;
		//GenericUtility genericUtility = null;
		ITMDBAccessEJB itmDBAccessEJB = null;
//		ValidatorEJB validatorEJB = null;
		System.out.println("tran id = " + tranId);
		// boolean connStatus=false;
		String retString = "", sql = "", confirm = "", sysDate = "", itemCode = "",unit = "", lotNoFrom = "", lotNoTo = "";
		String rateType = "", isExcisable = "", siteCodeMfg = "", siteCodeOwn = "", itemSer = "", chgReffNo = "", remarks = "", unitPack = "",
				parentTranId = "", reffNo = "", reffNoOld = "", priceList = "",effFromStr="",validUptoStr="",empFname="",empMname="",empLname="";
		double minQty = 0.0, maxQty = 0.0, rate = 0.0, minRate = 0.0, maxRate = 0.0, shiperSize = 0.0;
		String xmlStringPhdrDtl = "";
		int lineNo = 0, insertGenCd = 0, packListCnt = 0, insertCount = 0,shelfLife = 0, itemOwnCnt = 0, updateCount=0;
		DistCommon distCommon = null;
		Timestamp systemdate = null, effFrom = null, validUpToDt = null;
		StringBuffer xmlBuff = null;

		try
		{
			if (conn == null)
			{
				ConnDriver connDriver = null;
				connDriver = new ConnDriver();
				//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
				conn.setAutoCommit(false);
				connDriver = null;
				connStatus = true;
			}
//			finCommon = new FinCommon();
			//genericUtility = new GenericUtility();
			itmDBAccessEJB = new ITMDBAccessEJB();
//			validatorEJB = new ValidatorEJB();

			userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginSite = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			termId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "termId");
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");

			sql = "select confirmed from pricegen_hdr where tran_id = ?";
			pstmtSql = conn.prepareStatement(sql);
			pstmtSql.setString(1, tranId);
			rs = pstmtSql.executeQuery();
			if (rs.next())
			{
				confirm = rs.getString("confirmed") == null ? "N" : rs.getString("confirmed");
			}
			rs.close();
			rs = null;
			pstmtSql.close();
			pstmtSql = null;

			/**
			 * Check the transaction already confirm
			 * */
			if ("Y".equalsIgnoreCase(confirm))
			{
				System.out.println("Price list already confirm");
				retString = itmDBAccessEJB.getErrorString("", "VTRCONF1", "", "", conn);
				return retString;
			} else
			{
				/**
				 * Select all required DISPARM
				 * */
				distCommon = new DistCommon();

				autoConfReqd = checkNull(distCommon.getDisparams("999999", "AUTO_CONF_PLIST", conn));	// To auto confirm generated price list [Y,N]
				itemLotReqd = checkNull(distCommon.getDisparams("999999", "ITEM_LOTPACK_REQD", conn));	// item_lot_packsize entry [Y,N]

				java.util.Date today = new java.util.Date();
				Calendar cal = Calendar.getInstance();
				cal.setTime(today);
				today = cal.getTime();
				SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
				sysDate = sdf.format(today);
				System.out.println("System date  :- [" + sysDate + "]");
				
				if (autoConfReqd == null || autoConfReqd.trim().length() == 0 || autoConfReqd.trim().equalsIgnoreCase("NULLFOUND"))
				{
					autoConfReqd = "N";
				}
				if (itemLotReqd == null || itemLotReqd.trim().length() == 0 || itemLotReqd.trim().equalsIgnoreCase("NULLFOUND"))
				{
					itemLotReqd = "N";
				}

				systemdate = Timestamp.valueOf(genericUtility.getValidDateString(sysDate, genericUtility.getApplDateFormat(),
						genericUtility.getDBDateFormat()) + " 00:00:00.0");	
				
					xmlBuff = new StringBuffer();

					xmlBuff.append("<?xml version='1.0' encoding='ISO-8859-1'?><DocumentRoot>");
					xmlBuff.append("<description>Datawindow Root</description>");
					xmlBuff.append("<group0>");
					xmlBuff.append("<description>Group0 description</description>");
					xmlBuff.append("<Header0>");
					xmlBuff.append("<objName><![CDATA[").append("pricelist_tran").append("]]></objName>");
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
					xmlBuff.append("<description>Header0 members</description>");

					/**
					 * Select and Generate header xml
					 *  for 'pricelist_hdr'
					 * */
					sql = "select price_list,emp_code__aprv,remarks,ref_no,ref_no_old from pricegen_hdr where tran_id=?";
					pstmtSql = conn.prepareStatement(sql);
					pstmtSql.setString(1, tranId);
					rs = pstmtSql.executeQuery();
					if (rs.next())
					{
						priceList = checkNull(rs.getString("price_list"));
						reffNo = checkNull(rs.getString("ref_no"));
						reffNoOld = checkNull(rs.getString("ref_no_old"));

						xmlBuff.append("<Detail1 dbID=\"\" domID=\"1\" objContext=\"1\" objName=\"pricelist_tran\">");
						xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");
						xmlBuff.append("<tran_id/>");
						xmlBuff.append("<tran_date><![CDATA[" + sysDate + "]]></tran_date>");
						xmlBuff.append("<price_list><![CDATA[" + priceList + "]]></price_list>");
						xmlBuff.append("<ref_no><![CDATA[" + reffNo + "]]></ref_no>");
						xmlBuff.append("<ref_no_old><![CDATA[" + reffNoOld + "]]></ref_no_old>");
						xmlBuff.append("<chg_date><![CDATA[" + sysDate + "]]></chg_date>");
						xmlBuff.append("<chg_user><![CDATA[" + userId + "]]></chg_user>");
						xmlBuff.append("<chg_term><![CDATA[" + termId + "]]></chg_term>");
						xmlBuff.append("</Detail1>");
					}
					pstmtSql.close();
					pstmtSql = null;
					rs.close();
					rs = null;

				/**
				 * Select and generate details xml 
				 * for 'pricegen_hdr' and
				 * */

				sql = "select d.item_code,d.unit,d.eff_from,d.valid_upto,d.lot_no__from,d.lot_no__to,d.min_qty,d.max_qty,d.rate,d.rate_type," +
				" d.min_rate,d.max_rate,d.is_excisable,d.site_code__mfg,d.site_code__own,i.item_ser," +
				" d.shipper_size,d.shelf_life,d.chg_ref_no,d.remarks,d.unit__pack" +
				" from pricegen_det d,item i where d.item_code=i.item_code and d.tran_id=?";
				pstmt1 = conn.prepareStatement(sql);
				pstmt1.setString(1, tranId);
				rs1 = pstmt1.executeQuery();
				while (rs1.next())
				{
					itemCode = checkNull(rs1.getString("item_code"));
					unit = checkNull(rs1.getString("unit"));
					effFrom = rs1.getTimestamp("eff_from");
					validUpToDt = rs1.getTimestamp("valid_upto");
					lotNoFrom = checkNull(rs1.getString("lot_no__from"));
					lotNoTo = checkNull(rs1.getString("lot_no__to"));
					minQty = rs1.getDouble("min_qty");
					maxQty = rs1.getDouble("max_qty");
					rate = rs1.getDouble("rate");
					rateType = checkNull(rs1.getString("rate_type"));
					minRate = rs1.getDouble("min_rate");
					maxRate = rs1.getDouble("max_rate");
					isExcisable = checkNull(rs1.getString("is_excisable"));
					siteCodeMfg = checkNull(rs1.getString("site_code__mfg"));
					siteCodeOwn = checkNull(rs1.getString("site_code__own"));
					itemSer = checkNull(rs1.getString("item_ser"));
					shiperSize = rs1.getDouble("shipper_size");
					shelfLife = rs1.getInt("shelf_life");
					chgReffNo = checkNull(rs1.getString("chg_ref_no"));
					remarks = checkNull(rs1.getString("remarks"));
					unitPack = checkNull(rs1.getString("unit__pack"));

					/**
					 * Check price list is exist 
					 * for item code and lot no
					 * */
//							plistDetailCnt++;
							lineNo++;
							System.out.println("Line no :- ["+lineNo+"]");
							
							effFromStr=genericUtility.getValidDateString(effFrom.toString(), genericUtility.getDBDateFormat(),
									genericUtility.getApplDateFormat());
							validUptoStr=genericUtility.getValidDateString(validUpToDt.toString(), genericUtility.getDBDateFormat(),
									genericUtility.getApplDateFormat());

							xmlBuff.append("<Detail2 dbID=\"\" domID=\"1\" objContext=\"2\" objName=\"pricelist_tran\">");
							xmlBuff.append("<attribute pkNames=\"\" selected=\"N\" status=\"N\" updateFlag=\"A\"/>");
							xmlBuff.append("<tran_id/>");
							xmlBuff.append("<line_no><![CDATA[" + lineNo + "]]></line_no>");
							xmlBuff.append("<item_code><![CDATA[" + itemCode + "]]></item_code>");
							xmlBuff.append("<lot_no__from><![CDATA[" + lotNoFrom + "]]></lot_no__from>");
							xmlBuff.append("<lot_no__to><![CDATA[" + lotNoTo + "]]></lot_no__to>");
							xmlBuff.append("<min_qty><![CDATA[" + minQty + "]]></min_qty>");
							xmlBuff.append("<max_qty><![CDATA[" + maxQty + "]]></max_qty>");
							xmlBuff.append("<rate><![CDATA[" + rate + "]]></rate>");
							xmlBuff.append("<rate_type><![CDATA[" + rateType + "]]></rate_type>");
							xmlBuff.append("<min_rate><![CDATA[" + minRate + "]]></min_rate>");
							xmlBuff.append("<max_rate><![CDATA[" + maxRate + "]]></max_rate>");
							xmlBuff.append("<unit><![CDATA[" + unit + "]]></unit>");
							xmlBuff.append("<eff_from><![CDATA[" + effFromStr + "]]></eff_from>");
							xmlBuff.append("<valid_upto><![CDATA[" + validUptoStr + "]]></valid_upto>");
							xmlBuff.append("<chg_ref_no><![CDATA[" + chgReffNo + "]]></chg_ref_no>");
							xmlBuff.append("</Detail2>");

							/**
							 * Check is_excisable for 'gencodes' insert
							 * */
							if ("N".equalsIgnoreCase(isExcisable))
							{
								System.out.println("Inserting gencodes");
								insertGenCd = insertGenCodes(itemCode, lotNoFrom, loginSite, termId, loginEmpCode, sysDate, conn);
								System.out.println("Gen codes insert :- [" + insertGenCd + "]");
							}
							/**
							 * Check ITEM_LOT_PACKSIZE is required
							 * */
							if ("Y".equalsIgnoreCase(itemLotReqd))
							{
								System.out.println("ITEM_LOT_PACKSIZE is required");

								sql = "select count(*) as COUNT from ITEM_LOT_PACKSIZE where item_code=? and lot_no__from=? and lot_no__to=?";
								pstmt2 = conn.prepareStatement(sql);
								pstmt2.setString(1, itemCode);
								pstmt2.setString(2, lotNoFrom);
								pstmt2.setString(3, lotNoTo);
								rs2 = pstmt2.executeQuery();
								if (rs2.next())
								{
									packListCnt = rs2.getInt("COUNT");
								}
								pstmt2.close();
								pstmt2 = null;
								rs2.close();
								rs2 = null;

								System.out.println("ITEM_LOT_PACKSIZE count :- [" + packListCnt + "]");
								if (packListCnt == 0)
								{
									insertCount = insertItemLotPackSize(itemCode, lotNoFrom, lotNoTo, unit, loginSite, termId, loginEmpCode,
											sysDate, conn, shelfLife, siteCodeMfg, siteCodeOwn, shiperSize, shelfLife, unitPack);
									System.out.println("ITEM_LOT_PACKSIZE insert count :- [" + insertCount + "]");
								}
							}

							/**
							 * Check before insert in 'item_lot_own' record is
							 * already exist
							 * */

							sql = "select count(*) as COUNT from item_lot_own where site_code=? and item_code=? and lot_no__from=?"
									+ " and lot_no__to=?";
							pstmt2 = conn.prepareStatement(sql);
							pstmt2.setString(1, siteCodeOwn);			// use site code own
							pstmt2.setString(2, itemCode);
							pstmt2.setString(3, lotNoFrom);
							pstmt2.setString(4, lotNoTo);
							rs2 = pstmt2.executeQuery();
							if (rs2.next())
							{
								itemOwnCnt = rs2.getInt("COUNT");
							}
							pstmt2.close();
							pstmt2 = null;
							rs2.close();
							rs2 = null;

							if (itemOwnCnt == 0)
							{
								insertCount = insertIntoItemOwn(loginSite, itemCode, itemSer, lotNoFrom, lotNoTo, loginEmpCode, termId,
										sysDate, siteCodeOwn,conn);
								System.out.println("Item lot own insert count :- [" + insertCount + "]");
							} 
					}// END of detail while loop
				pstmt1.close();
				pstmt1=null;
				rs1.close();
				rs1=null;
				
					xmlBuff.append("</Header0>");
					xmlBuff.append("</group0>");
					xmlBuff.append("</DocumentRoot>");
					xmlStringPhdrDtl = xmlBuff.toString();
					System.out.println("XML generated  :- ["+xmlStringPhdrDtl+"]");
					
						/**
						 * Call save data
						 * */
						retString = saveData(loginSite, xmlStringPhdrDtl, xtraParams, conn);
						System.out.println("Master statefull SAVE return :- ["+retString+"]");
						
						if (retString.indexOf("Success") > -1)
						{
							System.out.println("Master statefull SAVE success..connection commite");
							conn.commit();

							String[] arrayForTranIdIssue = retString.split("<TranID>");
							int endIndexIssue = arrayForTranIdIssue[1].indexOf("</TranID>");
							parentTranId = arrayForTranIdIssue[1].substring(0, endIndexIssue);
							System.out.println("@V@ Parent Tran id :- [" + parentTranId + "]");

							/**
							 * Check auto confirm required
							 * for newly generated price list
							 * */
							if ("Y".equalsIgnoreCase(autoConfReqd))
							{
								System.out.println("Transaction is commited before confirm !!");
								retString = confirmTran("pricelist_tran", parentTranId, xtraParams, "", conn);
								System.out.println("Confirm return :- [" + retString + "]");
								if (retString.indexOf("VTSUCC1") <= -1)
								{
									parentTranId="";
								}
							}
						}
					
					/**
					 * Update confirm status,parentTranId and log in employee code
					 *  for tranId
					 * in 'pricegen_hdr'
					 * */
					if(parentTranId!=null && parentTranId.trim().length() > 0)
					{
						systemdate=Timestamp.valueOf(genericUtility.getValidDateString(sysDate, genericUtility.getApplDateFormat(),
								genericUtility.getDBDateFormat()) + " 00:00:00.0");
											
						
						sql = "update pricegen_hdr set confirmed='Y',conf_date=?, tran_id__plgen=?, emp_code__aprv=? where tran_id=?";
						pstmt3 = conn.prepareStatement(sql);
						pstmt3.setTimestamp(1, systemdate);
						pstmt3.setString(2, parentTranId);
						pstmt3.setString(3, loginEmpCode);		// Log in employee code
						pstmt3.setString(4, tranId);
						updateCount = pstmt3.executeUpdate();

						pstmt3.close();
						pstmt3 = null;
//						conn.commit();
						
						System.out.println("Update count :- [" + updateCount + "]");
					}
					else
					{
						System.out.println("Connection rollback at 'pricegen_hdr' update fail");
						conn.rollback();
					}
					if(updateCount>0)
					{
						System.out.println("Price list is confirm !!");
						retString = itmDBAccessEJB.getErrorString("", "VTCONFIRM", "", "", conn);	//Confirm successfully
					}
				}
//			}
		} catch (Exception e)
		{
			if (conn != null)
			{
				try
				{
					conn.rollback();
				} catch (java.sql.SQLException e1)
				{
					e1.printStackTrace();
				}
			}
			System.out.println("Exception : " + e);
			e.printStackTrace();
			throw new ITMException(e);
		} finally
		{
			try
			{
				if (retString != null && retString.trim().length() > 0)
				{
					if (conn != null && !conn.isClosed() && connStatus)
					{
						if (retString.indexOf("VTCONFIRM") > -1)
						{
							conn.commit();
						} else
						{
							conn.rollback();
						}
						conn.close();
						conn = null;
					}
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (pstmtSql != null)
				{
					pstmtSql.close();
					pstmtSql = null;
				}
				// conn.close();
			} catch (Exception e)
			{
				System.out.println("Exception : " + e);
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return retString;
	}
	
	private int insertIntoItemOwn(String loginSite2, String itemCode, String itemSer, String lotNoFrom, String lotNoTo, String loginEmpCode2,
			String termId2, String sysDate, String siteCodeOwn,Connection conn)
	{
		int insertCount = 0;
		String sql = "";
		PreparedStatement pstmt = null;
		Timestamp systemdate = null;

		try
		{
			systemdate = Timestamp.valueOf(genericUtility.getValidDateString(sysDate, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat()) + " 00:00:00.0");

			sql = "insert into item_lot_own (site_code,item_code,item_ser,lot_no__from,lot_no__to,chg_date,chg_user,chg_term)" +
			" values(?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, siteCodeOwn);		// Site code own
			pstmt.setString(2, itemCode);
			pstmt.setString(3, itemSer);
			pstmt.setString(4, lotNoFrom);
			pstmt.setString(5, lotNoTo);
			pstmt.setTimestamp(6, systemdate);
			pstmt.setString(7, loginEmpCode2);
			pstmt.setString(8, termId2);
			insertCount = pstmt.executeUpdate();

			pstmt.close();
			pstmt = null;
		} catch (ITMException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (java.sql.SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return insertCount;
	}

	public String confirmTran(String businessObj, String tranId, String xtraParams, String forceFlag, Connection conn) throws ITMException
	{
		String methodName = "",compName = "",retString = "",serviceCode = "",serviceURI = "",actionURI = "",sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			sql = "SELECT SERVICE_CODE,COMP_NAME FROM SYSTEM_EVENTS WHERE OBJ_NAME = ? AND EVENT_CODE = 'pre_confirm' ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, businessObj);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				serviceCode = rs.getString("SERVICE_CODE");
				compName = rs.getString("COMP_NAME");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			System.out.println("serviceCode = " + serviceCode + " compName " + compName);

			sql = "SELECT SERVICE_URI,METHOD_NAME FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, serviceCode);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				methodName = rs.getString("METHOD_NAME");
				serviceURI = rs.getString("SERVICE_URI");
			}
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			
			actionURI = "http://NvoServiceurl.org/" + methodName;
			System.out.println("serviceURI = " + serviceURI + " compName = " + compName);

			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(serviceURI));
			call.setOperationName(new javax.xml.namespace.QName("http://NvoServiceurl.org", methodName));
			call.setUseSOAPAction(true);
			call.setSOAPActionURI(actionURI);
			Object[] aobj = new Object[4];

			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "component_name"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "xtra_params"), XMLType.XSD_STRING, ParameterMode.IN);
			call.addParameter(new javax.xml.namespace.QName("http://NvoServiceurl.org", "forced_flag"), XMLType.XSD_STRING, ParameterMode.IN);

			aobj[0] = new String(compName);
			aobj[1] = new String(tranId);
			aobj[2] = new String(xtraParams);
			aobj[3] = new String("");

			// System.out.println("@@@@@@@@@@loginEmpCode:"
			// +genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginEmpCode")+":");
			System.out.println("@@@@@@ call.setReturnType(XMLType.XSD_STRING) executed........");
			call.setReturnType(XMLType.XSD_STRING);

			retString = (String) call.invoke(aobj);

			System.out.println("Confirm Complete @@@@@@@@@@@ Return string from NVO is:==>[" + retString + "]");

		} catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		} finally
		{
			try
			{

				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				/*
				 * if( conn != null ){ conn.close(); conn = null; }
				 */
			} catch (Exception e)
			{
				System.out.println("Exception inCalling confirmed");
				e.printStackTrace();
				try
				{
					conn.rollback();

				} catch (Exception s)
				{
					System.out.println("Unable to rollback");
					s.printStackTrace();
				}
				throw new ITMException(e);
				}
		}
		return retString;
	}

	private int insertItemLotPackSize(String itemCode, String lotNoFrom, String lotNoTo, String unit, String loginSite2, String termId2,
			String loginEmpCode2, String sysDate, Connection conn, int shelfLife, String siteCodeMfg, String siteCodeOwn, double shiperSize,
			int shelfLife2, String unitPack) throws ITMException
	{
		String sqlStr = "";
		PreparedStatement pstmt = null;;
		int insertCoun = 0;
		Timestamp systemdate = null;

		try
		{
			systemdate = Timestamp.valueOf(genericUtility.getValidDateString(sysDate, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat()) + " 00:00:00.0");

			sqlStr = "INSERT INTO ITEM_LOT_PACKSIZE (ITEM_CODE,LOT_NO__FROM,LOT_NO__TO,UNIT__PACK,CHG_DATE,CHG_USER,CHG_TERM," +
			"SHIPPER_SIZE,SITE_CODE__MFG,SITE_CODE__OWN,UNIT__INNER_LABEL,SHELF_LIFE) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(sqlStr);
			pstmt.setString(1, itemCode);
			pstmt.setString(2, lotNoFrom);
			pstmt.setString(3, lotNoTo);
			pstmt.setString(4, unitPack);
			pstmt.setTimestamp(5, systemdate);
			pstmt.setString(6, loginEmpCode2);
			pstmt.setString(7, termId2);
			pstmt.setDouble(8, shiperSize);
			pstmt.setString(9, siteCodeMfg);
			pstmt.setString(10, siteCodeOwn);
			pstmt.setString(11, unit);
			pstmt.setInt(12, shelfLife);
			insertCoun = pstmt.executeUpdate();

			pstmt.close();
			pstmt = null;

		} catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return insertCoun;
	}

	private int insertGenCodes(String itemCode, String lotNo, String loginSite, String termId, String empCode,
			String sysDate, Connection conn) throws Exception
	{
		int insertCount = 0;
		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Timestamp systemdate = null;
		int noRecords = 0;

		try
		{
			systemdate = Timestamp.valueOf(genericUtility.getValidDateString(sysDate, genericUtility.getApplDateFormat(),
					genericUtility.getDBDateFormat()) + " 00:00:00.0");
			
			sql = "select count(1) from gencodes where fld_name='BATCH_EXEMPTED' and mod_name='W_EXC_EXEMPTED' and fld_value=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, itemCode.trim() + "," + lotNo.trim());
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				noRecords = rs.getInt(1);
			}
			pstmt.close();
			pstmt = null;
			rs.close();
			rs = null;
			if (noRecords == 0)
			{
				sql = "insert into gencodes (fld_name, mod_name, fld_value, descr, chg_date, chg_user, chg_term,active) "
						+ " values(?,?,?,?,?,?,?,?)";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "BATCH_EXEMPTED");
				pstmt.setString(2, "W_EXC_EXEMPTED");
				pstmt.setString(3, itemCode.trim() + "," + lotNo.trim());
				pstmt.setString(4, itemCode.trim() + "," + lotNo.trim());
				pstmt.setTimestamp(5, systemdate);
				pstmt.setString(6, empCode);
				pstmt.setString(7, termId);
				pstmt.setString(8, "Y");
				insertCount = pstmt.executeUpdate();

				pstmt.close();
				pstmt = null;
			}

		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ITMException itm)
		{
			itm.printStackTrace();
		}
		return insertCount;
	}

	private String saveData(String siteCode, String xmlString, String xtraParams, Connection conn) throws ITMException
	{
		System.out.println("saving data...........");
		InitialContext ctx = null;
		String retString = null;
		MasterStatefulLocal masterStateful = null; // for ejb3
		ibase.utility.UserInfoBean userInfo;
		String chgUser = "", chgTerm = "";
		String loginCode = "", loginEmpCode = "", loginSiteCode = "";
		E12GenericUtility genericUtility = new E12GenericUtility();
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext(appConnect.getProperty());
			masterStateful = (MasterStatefulLocal) ctx.lookup("ibase/MasterStatefulEJB/local");
			System.out.println("-----------masterStateful------- " + masterStateful);
			String[] authencate = new String[2];
			authencate[0] = "";
			authencate[1] = "";
			System.out.println("xmlString to masterstateful [" + xmlString + "]");
			userInfo = new ibase.utility.UserInfoBean();
			System.out.println("xtraParams>>>>" + xtraParams);
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgUser");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "chgTerm");
			loginCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			loginEmpCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginEmpCode");
			loginSiteCode = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginSiteCode");
			userInfo.setEmpCode(loginEmpCode);
			userInfo.setRemoteHost(chgTerm);
			userInfo.setSiteCode(loginSiteCode);
			userInfo.setLoginCode(loginCode);
			userInfo.setEntityCode(loginEmpCode);
			System.out.println("userInfo>>>>>" + userInfo);

			System.out.println("chgUser :" + chgUser);
			System.out.println("chgTerm :" + chgTerm);
			System.out.println("loginCode :" + loginCode);
			System.out.println("loginEmpCode :" + loginEmpCode);

			retString = masterStateful.processRequest(userInfo, xmlString, true, conn);
			System.out.println("--retString - -" + retString);
		} catch (ITMException itme)
		{
			System.out.println("ITMException :CreateDistOrder :saveData :==>");
			throw itme;
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception :CreateDistOrder :saveData :==>");
			throw new ITMException(e);
		}
		return retString;
	}


	private String checkNull(String input)
	{
		if (input == null)
		{
			input = "";
		}
		return input;
	}

}
