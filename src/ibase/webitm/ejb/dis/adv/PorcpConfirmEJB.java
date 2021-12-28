package ibase.webitm.ejb.dis.adv;

import ibase.scheduler.utility.interfaces.Schedule;
import ibase.system.config.ConnDriver;
import ibase.utility.BaseException;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
//import org.apache.poi.hsmf.datatypes.PropertyValue.TimePropertyValue;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PorcpConfirmEJB implements Schedule
{
	GenericUtility genericUtility = GenericUtility.getInstance();
	Document dom = null;

	boolean isError = false;

	@Override
	public String schedule(HashMap arg0) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	// @Override
	public String schedulePriority(String arg0) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String schedule(String scheduleParamXML) throws Exception, ITMException
	{
		System.out.println("\n\n\n\n\n\t :::::PorcpConfirmEJB[schedule(String scheduleXML)]is called ::::");
		Connection localConnection = null;
		ResultSet localResultSet1 = null;
		ResultSet localResultSet2 = null;
		ResultSet localResultSet3 = null;
//		Statement localStatement1 = null;
//		Statement localStatement2 = null;
		PreparedStatement localStatement1 = null;
		PreparedStatement localStatement2 = null;
		Statement localStatement3 = null;
		int i = 0;
		int j = 0;
		int k = 0;
		int m = 1;
		String str1 = "";
		String str2 = "";
		String str3 = "";
		String str4 = "";
		String str5 = "";
		String str6 = "";
		String str7 = "";
		String str8 = "";
		String str9 = "";
		String str10 = "";
		String str11 = "";
		String str12 = "";
		String loginSiteCode = "";
		String xtraParams = "",porcp_date="";
		
		int childNodeListLength = 0;
		String childNodeName = null;
		
		Timestamp porcpDate=null;
		
		String[] arrayOfString = new String[10000];
		String str13 = null;
		FileWriter localFileWriter = null;
		BufferedWriter localBufferedWriter = null;
		SimpleDateFormat localSimpleDateFormat1 = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
		SimpleDateFormat localSimpleDateFormat2 = new SimpleDateFormat("dd-MM-yyyy hh-mm");
		Document localDocument1 = null;

		ConnDriver localConnDriver = new ConnDriver();
		E12GenericUtility genericutility = new E12GenericUtility();
		// GenericUtility localGenericUtility = GenericUtility.getInstance();
		ibase.utility.UserInfoBean userInfo = null;
		try
		{
			localDocument1 = genericutility.parseString(scheduleParamXML);
			/**
			 * VALLABH KADAM
			 * Get porcp_date
			 * from scheduler argument
			 * [D16ASUN012]
			 * 26/APR/16
			 * */
			
			NodeList parentNodeList = null,childNodeList = null;
			Node parentNode = null,childNode = null;
//			SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());	
			
//			Node currDetail = null ;
			int noOfParam=0;
						
//			NodeList paramList = dom.getElementsByTagName( "SCHEDULE" );
			NodeList paramList = localDocument1.getElementsByTagName( "SCHEDULE" );
	        noOfParam = paramList.getLength();
	        
//	        parentNodeList = dom.getElementsByTagName("ACTUALPARAMETERS");
	        parentNodeList = localDocument1.getElementsByTagName("ACTUALPARAMETERS");
	        parentNode = parentNodeList.item(0);
			childNodeList = parentNode.getChildNodes();
			childNodeListLength = childNodeList.getLength();
			System.out.println("@V@ CshildNodeListLength :-["+childNodeListLength+"]");
			
			for(int ctr = 0; ctr < childNodeListLength; ctr++)
			{
				childNode = childNodeList.item(ctr);
				childNodeName = childNode.getNodeName();
							
				if(childNodeName!=null && !"#text".equalsIgnoreCase(childNodeName))
				{
					if(ctr==0)
					{
						porcp_date = childNode.getFirstChild().getNodeValue();
					}	
				}
			}		
			System.out.println("@V@ Purchase order Receipt Date :-"+porcp_date);
			
			porcpDate = Timestamp.valueOf(genericUtility.getValidDateString(porcp_date.trim(),genericUtility.getApplDateFormat(),
	        		genericUtility.getDBDateFormat())+ " 00:00:00");
			
			System.out.println("@V@ Purchase order Receipt Date In DB Date format:-"+porcpDate);
			
			/**
			 * VALLABH KADAM
			 * Get porcp_date
			 * from scheduler argument
			 * [D16ASUN012]
			 * 26/APR/16
			 * */
			
			
			System.out.println("\n\n\n\n\n PorcpConfirmEJB[schedule(String scheduleXML)] Setting XtraParam..... ");
			str6 = getXtraParam(localDocument1);
			System.out.println("@V@ XtraParam :- ["+str6+"]");

			String str14 = "<?xml version=\"1.0\" encoding='" + CommonConstants.ENCODING + "'?>";
			try
			{
				str13 = CommonConstants.JBOSSHOME + File.separator + "applnlog" + File.separator + "PorcpConfLog_" + localSimpleDateFormat2.format(new Date()) + ".xml";
				localFileWriter = new FileWriter(str13, true);
				localBufferedWriter = new BufferedWriter(localFileWriter);
				localBufferedWriter.write(str14);
				localBufferedWriter.newLine();
				localBufferedWriter.newLine();
				localBufferedWriter.write("<PROCESSED");
				localBufferedWriter.write("    ");
				localBufferedWriter.write("START-TIME=");
				localBufferedWriter.write("'" + localSimpleDateFormat1.format(new Date()) + "'");
				localBufferedWriter.write(">");
				localBufferedWriter.newLine();
				localBufferedWriter.flush();
			} catch (Exception localException4)
			{
				System.out.println("[PorcpConfirmEJB:: schedule()] Exception While Creating Log File ..");
				localException4.printStackTrace();
			}

			localConnection = localConnDriver.getConnectDB("DriverITM");
			//localConnection = getConnection();
			localConnection.setAutoCommit(false);

			/**
			 * [D16ASUN012]
			 * VALLABH KADAM
			 * From bellow SQL the condition for
			 * A.TRAN_DATE >= (SYSDATE-7) change to A.TRAN_DATE >=porcpDate
			 * And A.SITE_CODE >= '00' and A.SITE_CODE <= 'ZZ' is removed
			 * [03/MAY/16] 
			 * */
			
			str4 = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE ='999999' AND VAR_NAME ='SITE_EXCLUDE_PORCP'";
			System.out.println("\n[PorcpConfirmEJB:: schedule()] SQL to get Excludes Site is.." + str4);
//			localStatement1 = localConnection.createStatement();
			localStatement1 = localConnection.prepareStatement(str4);
			localResultSet1 = localStatement1.executeQuery();
			if (localResultSet1.next())
			{
				str11 = localResultSet1.getString("VAR_VALUE");
			}
			if (localResultSet1 != null)
			{
				localResultSet1.close();
				localResultSet1 = null;
			}
			if (localStatement1 != null)
			{
				localStatement1.close();
				localStatement1 = null;
			}

			System.out.println("\n[PorcpConfirmEJB:: schedule()] excludedSites From DISPARM are .." + str11);
			Object localObject1;
			if (str11 == null || str11.trim().length() == 0)
			{
				str1 = "SELECT COUNT(*) AS COUNT FROM PORCP A, SITE B"
//						+ " WHERE A.TRAN_DATE >= (SYSDATE-7)"
						+ " WHERE A.TRAN_DATE >=?"
//						+ " AND A.SITE_CODE >= '00'"
//						+ " and A.SITE_CODE <= 'zz'"
						+ " AND A.SITE_CODE = B.SITE_CODE"
						+ " AND B.SITE_TYPE ='C'"
						+ " AND CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END ='N'"
						+ " AND A.TRAN_SER = 'P-RCP'";
			} 
			else
			{
				localObject1 = new StringTokenizer(str11, ",");
				m = ((StringTokenizer) localObject1).countTokens();
				int n = 0;
				System.out.println("\n[PorcpConfirmEJB:: schedule()]No Of Sites to Excludes are..." + m);
				while (((StringTokenizer) localObject1).hasMoreTokens())
				{
					arrayOfString[n] = ((StringTokenizer) localObject1).nextToken();

					if (m == 1)
					{
						str12 = str12 + "'" + arrayOfString[n] + "'";
					} else if (n == m - 1)
						str12 = str12 + "'" + arrayOfString[n] + "'";
					else
					{
						str12 = str12 + "'" + arrayOfString[n] + "'" + ",";
					}

					n++;
				}
				str12 = "(" + str12 + ")";

				System.out.println("\n[PorcpConfirmEJB:: schedule()] newExcludedSites .." + str12);
				str1 = "SELECT COUNT(*) AS COUNT FROM PORCP A, SITE B"
//						+ " WHERE A.TRAN_DATE >= (SYSDATE-7)"
						+ " WHERE A.TRAN_DATE >=?"
//						+ " AND A.SITE_CODE >= '00'"
//						+ " and A.SITE_CODE <= 'zz'"
						+ " AND A.SITE_CODE = B.SITE_CODE"
						+ " AND B.SITE_TYPE ='C'"
						+ " AND CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END ='N'"
						+ " AND A.SITE_CODE NOT IN " + str12 + ""
						+ " AND A.TRAN_SER = 'P-RCP'";
			}

			System.out.println("\n[PorcpConfirmEJB:: schedule()] Query to count record/s =" + str1);

//			localStatement1 = localConnection.createStatement();
//			localResultSet1 = localStatement1.executeQuery(str1);
			localStatement1 = localConnection.prepareStatement(str1);
			localStatement1.setTimestamp(1, porcpDate);
			localResultSet1 = localStatement1.executeQuery();
			if (localResultSet1.next())
			{
				i = localResultSet1.getInt("COUNT");
			}
			if (localResultSet1 != null)
			{
				localResultSet1.close();
				localResultSet1 = null;
			}
			if (localStatement1 != null)
			{
				localStatement1.close();
				localStatement1 = null;
			}
			if (i == 0)
			{
				localBufferedWriter.write("      ");
				localBufferedWriter.write("<STATUS><![CDATA[ERROR]]></STATUS>");
				localBufferedWriter.newLine();
				localBufferedWriter.write("      ");
				localBufferedWriter.write("<ERRMSG>");
				localBufferedWriter.write("Record/s Not Found in PORCP TABLE To Confirm..");
				localBufferedWriter.write("</ERRMSG>");
				localBufferedWriter.newLine();
				localBufferedWriter.write("      ");
				localBufferedWriter.write("<NOTE>");
				localBufferedWriter.write("UNSUCCESSFUL Scheduler Stopped !! To continue Again Start  Scheduler........ ");
				localBufferedWriter.write("</NOTE>");
				localBufferedWriter.flush();
				this.isError = true;
				return "unsuccessful";
			}
			localBufferedWriter.write("<TOT_RECORDS>");
			localBufferedWriter.write("<![CDATA[" + i + "]]>");
			localBufferedWriter.write("</TOT_RECORDS>");
			localBufferedWriter.newLine();
			
			/**
			 * [D16ASUN012]
			 * VALLABH KADAM
			 * From bellow SQL the condition for
			 * A.TRAN_DATE >= (SYSDATE-7) change to A.TRAN_DATE >=porcpDate
			 * And A.SITE_CODE >= '00' and A>SITE_CODE <= 'ZZ' is removed
			 * [03/MAY/16] 
			 * */
			
			if (str11 == null || str11.trim().length() == 0)
			{
				str2 = "SELECT A.TRAN_ID FROM PORCP A, SITE B"
//						+ " WHERE A.TRAN_DATE >= (SYSDATE-7)"
						+ " WHERE A.TRAN_DATE >=?"
//						+ " AND A.SITE_CODE >= '00'"
//						+ " and A.SITE_CODE <= 'zz'"
						+ " AND A.SITE_CODE = B.SITE_CODE"
						+ " and B.SITE_TYPE ='C'"
						+ " AND CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END ='N'"
						+ " AND A.TRAN_SER = 'P-RCP'";
			} 
			else
			{
				str2 = "SELECT A.TRAN_ID FROM PORCP A, SITE B"
//						+ " WHERE A.TRAN_DATE >= (SYSDATE-7)"
						+ " WHERE A.TRAN_DATE >=?"
//						+ " AND A.SITE_CODE >= '00'"
//						+ " and A.SITE_CODE <= 'zz'"
						+ " AND A.SITE_CODE = B.SITE_CODE"
						+ " and B.SITE_TYPE ='C'"
						+ " AND CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END ='N'"
						+ " AND A.SITE_CODE NOT IN " + str12 + ""
						+ " AND A.TRAN_SER = 'P-RCP'";
			}

			System.out.println("\n[PorcpConfirmEJB:: schedule()] Very First Query is =" + str2);

//			localStatement2 = localConnection.createStatement();
//			localResultSet2 = localStatement2.executeQuery(str2);
			localStatement2 = localConnection.prepareStatement(str2);
			localStatement2.setTimestamp(1, porcpDate);
			localResultSet2 = localStatement2.executeQuery();

			while (localResultSet2.next())
			{
				str8 = localResultSet2.getString("TRAN_ID");

				System.out.println("@V@ TranId is:- [" + str8+"]");
				str3 = "SELECT TRAN_ID, CONFIRMED,SITE_CODE FROM PORCP WHERE TRAN_ID = '" + str8 + "' ";
				System.out.println("\n[PorcpConfirmEJB:: schedule()] Query is =" + str3);
				localStatement3 = localConnection.createStatement();

				localBufferedWriter.newLine();
				localBufferedWriter.write("<TRAN_ID_ATT TRAN_ID='" + str8 + "' ");
				Object localObject2;
				try
				{
					localResultSet3 = localStatement3.executeQuery(str3);
				} catch (SQLException localSQLException)
				{
					localObject2 = "ORA-00054: resource busy and acquire with NOWAIT specified";
					localBufferedWriter.write(">");
					localBufferedWriter.newLine();
					localBufferedWriter.write("      ");
					localBufferedWriter.write("      ");
					localBufferedWriter.write("<ERRMSG>");
					localBufferedWriter.write("" + localSQLException.getMessage() + "");
					localBufferedWriter.write("</ERRMSG>");
					localBufferedWriter.newLine();
					localBufferedWriter.write("</TRAN_ID_ATT>");
					localBufferedWriter.newLine();
					localBufferedWriter.flush();
					if (localSQLException.getMessage() != null)
					{
						if (((String) localObject2).equals(localSQLException.getMessage().trim()))
						{
							System.out.println("\n[PorcpConfirmEJB:: schedule()] Continuing for next batchId..");
							continue;
						}
					}
				}
				if (localResultSet3.next())
				{
					str5 = localResultSet3.getString("CONFIRMED");
					str8 = localResultSet3.getString("TRAN_ID");
					str10 = localResultSet3.getString("SITE_CODE");
					localBufferedWriter.write("  ");
					localBufferedWriter.write("SITE_CODE =");
					localBufferedWriter.write("'" + str10 + "'");
					localBufferedWriter.write(">");
					localBufferedWriter.newLine();

					if (localResultSet3 != null)
					{
						localResultSet3.close();
						localResultSet3 = null;
					}
					if (localStatement3 != null)
					{
						localStatement3.close();
						localStatement3 = null;
					}
					Node localNode1 = null,localNode3=null;
					localObject2 = null;
					NamedNodeMap localNamedNodeMap = null;
					String str15 = "";
					String str16 = "";
					String str17 = "";
					String str18 = "";

					String str19 = getResponse(str8, str6, localConnection);

					System.out.println("@V@ Get Responce :- [" + str19+"]");

					Document localDocument2 = genericutility.parseString(str19);

					Node localNode2 = localDocument2.getElementsByTagName("error").item(0);
					if (localNode2 != null)
					{
						localNamedNodeMap = localNode2.getAttributes();
					}
					if (localNamedNodeMap != null)
					{
						str15 = localNamedNodeMap.getNamedItem("type").getNodeValue().trim();
					}

					System.out.println("[PorcpConfirmEJB :: schedule()] msgType.." + str15);
					str7 = localNamedNodeMap.getNamedItem("id").getNodeValue().trim();

					System.out.println("[PorcpConfirmEJB :: schedule()] msgNo.." + str7);
					if ((str15 != null && str15.trim().length() > 0) && (str7 != null && str7.trim().length() > 0))
					{
						if (str15.equalsIgnoreCase("P"))
						{
							System.out.println("[PorcpConfirmEJB :: schedule()] Returning Success..");
							str9 = "success";
							localBufferedWriter.write("      ");
							localBufferedWriter.write("<STATUS><![CDATA[CONFIRMED]]></STATUS>");
							localBufferedWriter.newLine();
							localBufferedWriter.write("      ");
						} else if (str15.equalsIgnoreCase("E"))
						{
							localBufferedWriter.write("      ");
							localBufferedWriter.write("<STATUS><![CDATA[ERROR]]></STATUS>");
							localBufferedWriter.newLine();
							try
							{
								localNode1 = localDocument2.getElementsByTagName("message").item(0);
							} catch (Exception localException7)
							{
								System.out.println("[PorcpConfirmEJB :: schedule()]Exception arises while getting error  message from response");
							}
							if (localNode1 != null && localNode1.getFirstChild()!=null)
							{
								str16 = localNode1.getFirstChild().getNodeValue();
							}
							localBufferedWriter.write("      ");
							localBufferedWriter.write("<ERROR_ID><![CDATA[" + str7 + " ]]></ERROR_ID>");
							localBufferedWriter.newLine();
							localBufferedWriter.write("      ");
							localBufferedWriter.write("<ERR_MSG>'" + str16 + "'</ERR_MSG>");
							localBufferedWriter.newLine();
							localBufferedWriter.write("      ");
							try
							{
								localObject2 = localDocument2.getElementsByTagName("description").item(0);
//								localNode3 = localDocument2.getElementsByTagName("description").item(0);
							} catch (Exception localException8)
							{
								System.out.println("[PorcpConfirmEJB :: schedule()]Exception arises while getting error description from response");
							}

							if (localObject2 != null && ((Node) localObject2).getFirstChild()!=null)
							{
								str17 = ((Node) localObject2).getFirstChild().getNodeValue();
							}
//							if (localNode3 != null && localNode3.getFirstChild()!=null)
//							{
//								str17 = localNode3.getFirstChild().getNodeValue();
//							}
							localBufferedWriter.write("<ERR_DESCR>'" + str17 + "'</ERR_DESCR>");
							localBufferedWriter.newLine();
							localBufferedWriter.write("      ");
							try
							{
								str18 = localDocument2.getElementsByTagName("trace").item(0).getFirstChild().getNodeValue();
							} catch (Exception localException9)
							{
								System.out.println("[PorcpConfirmEJB :: schedule()]Exception arises while getting error trace from response");
							}
							localBufferedWriter.write("<ERR_TRACE>'" + str18 + "'</ERR_TRACE>");
							System.out.println("[PorcpConfirmEJB :: schedule()] Returning UnSuccess..");
							str9 = "unsuccessful";
						}

					}

					if ((this.isError) || (str9.equals("unsuccessful")))
					{
						j++;
						System.out.println("\n[PorcpConfirmEJB:: schedule()] retValue is .." + str9);
						localBufferedWriter.newLine();
						localBufferedWriter.write("      ");
						localBufferedWriter.write("<NOTE>");
						localBufferedWriter.write("UNSUCCESSFUL !!! Skipped the current tranID ");
						localBufferedWriter.write("</NOTE>");

						localBufferedWriter.newLine();
						localBufferedWriter.write("</TRAN_ID_ATT>");
						localBufferedWriter.newLine();
						localBufferedWriter.flush();
					} else
					{
						k++;
						localBufferedWriter.newLine();
						localBufferedWriter.write("</TRAN_ID_ATT>");
						localBufferedWriter.newLine();
						localBufferedWriter.flush();
					}
				}
			}
			if (localResultSet2 != null)
			{
				localResultSet2.close();
				localResultSet2 = null;
			}
			if (localStatement2 != null)
			{
				localStatement2.close();
				localStatement2 = null;
			}
		} catch (Exception localException2)
		{
			System.out.println("[PorcpConfirmEJB :: schedule()]Exception Arises ...");
			try
			{
				localException2.printStackTrace();
				if (j == 0)
				{
					localBufferedWriter.newLine();
					localBufferedWriter.write("      ");
					localBufferedWriter.write("" + localException2.getMessage() + "");
					localBufferedWriter.newLine();
					localBufferedWriter.write("      ");
					localBufferedWriter.newLine();
					localBufferedWriter.write("</TRAN_ID_ATT>");
					localBufferedWriter.newLine();
					localBufferedWriter.flush();
				}
				throw new BaseException(localException2);
			} catch (Exception localException5)
			{
			}
		} finally
		{
			System.out.println("\n ::::: [PorcpConfirmEJB:: schedule()]INSIDE FINALLY..Closing ResultSets, Statements & Conection......");
			try
			{
				if (localResultSet2 != null)
				{
					localResultSet2.close();
					localResultSet2 = null;
				}
				if (localResultSet3 != null)
				{
					localResultSet3.close();
					localResultSet3 = null;
				}
				if (localStatement2 != null)
				{
					localStatement2.close();
					localStatement2 = null;
				}
				if (localStatement3 != null)
				{
					localStatement3.close();
					localStatement3 = null;
				}
				if (localConnection != null)
				{
					localConnection.close();
					localConnection = null;
				}
				localBufferedWriter.newLine();
				localBufferedWriter.write("<TOT_RECORDS_CONFIRMED>");
				localBufferedWriter.write("<![CDATA[" + k + "]]>");
				localBufferedWriter.write("</TOT_RECORDS_CONFIRMED>");
				localBufferedWriter.newLine();
				localBufferedWriter.write("<PROCESS_END-TIME>");
				localBufferedWriter.write("<![CDATA[" + localSimpleDateFormat1.format(new Date()) + "]]>");
				localBufferedWriter.write("</PROCESS_END-TIME>");
				localBufferedWriter.newLine();
				localBufferedWriter.write("</PROCESSED>");
				localBufferedWriter.newLine();
				localBufferedWriter.flush();
				localBufferedWriter.close();
				System.out.println("\n ::::: [PorcpConfirmEJB:: schedule()]INSIDE FINALLY..Closed ResultSets, Statements & Conection......");
			} catch (Exception localException10)
			{
				System.out.println("[PorcpConfirmEJB :: schedule()]Exception...Arises in Finally...");
				localException10.printStackTrace();
			}

			System.out.println("\n ::::: [PorcpConfirmEJB:: schedule()] INSIDE FINALLY..All Connections Closed......");
		}

		return str9;
	}

	String getResponse(String paramString1, String paramString2, Connection paramConnection) throws Exception
	{
		System.out.println("@V@ In Get Responce ........");
		String str1 = "gbf_post";
		String str2 = "";
		String str3 = "";
		String str4 = "";
		String str5 = "";
		String str6 = "";
		String str7 = "";
		String str8 = null;
		String str9 = "true";
		ResultSet localResultSet1 = null;
		ResultSet localResultSet2 = null;
		Statement localStatement1 = null;
		Statement localStatement2 = null;
		try
		{
			if (paramString1 != null)
			{
				System.out.println("@V@ getResponse()] Calling WEB Services method..");
				str6 = "http://NvoServiceurl.org/" + str1;
				System.out.println("@V@ String 6 :- ["+str6+"]");
				str8 = "SELECT SERVICE_CODE,COMP_NAME,COMP_TYPE FROM SYSTEM_EVENTS WHERE OBJ_NAME ='porcp' AND EVENT_CODE = 'pre_confirm' ";
				System.out.println("@V@ [PorcpConfirmEJB:: getResponse()] 1st selectSql :-[" + str8+"]");
				localStatement1 = paramConnection.createStatement();
				localResultSet1 = localStatement1.executeQuery(str8);
				if (localResultSet1.next())
				{
					str4 = localResultSet1.getString("SERVICE_CODE");
					str2 = localResultSet1.getString("COMP_NAME");
					str7 = localResultSet1.getString("COMP_TYPE");
				}
				if (str8 != null)
					str8 = null;
				if (localResultSet1 != null)
				{
					localResultSet1.close();
					localResultSet1 = null;
				}
				if (localStatement1 != null)
				{
					localStatement1.close();
					localStatement1 = null;
				}
				str8 = "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE =  '" + str4 + "' ";
				System.out.println("@V@ [PorcpConfirmEJB] 2nd selectSql :-[" + str8+"]");
				localStatement2 = paramConnection.createStatement();
				localResultSet2 = localStatement2.executeQuery(str8);
				if (localResultSet2.next())
				{
					str5 = localResultSet2.getString("SERVICE_URI");
				}
				System.out.println("@V@ [PorcpConfirmEJB:: getResponse()] ServiceCode :: " + str4);
				System.out.println("@V@ [PorcpConfirmEJB:: getResponse()] compName :: " + str2);
				System.out.println("@V@ [PorcpConfirmEJB:: getResponse()] compType :: " + str7);
				System.out.println("@V@ [PorcpConfirmEJB:: getResponse()] serviceURI :: " + str5);
				System.out.println("@V@ [PorcpConfirmEJB:: getResponse()] methodName :: " + str1);
				System.out.println("@V@ [PorcpConfirmEJB:: getResponse()]tranID :: " + paramString1);
				System.out.println("@V@ [PorcpConfirmEJB:: getResponse()]xtraParams :: " + paramString2);
				System.out.println("@V@ [PorcpConfirmEJB:: getResponse()]forcedFlag :: " + str9);

				Service localService = new Service();
				Call localCall = (Call) localService.createCall();
				localCall.setTargetEndpointAddress(new URL(str5));
				localCall.setOperationName(new QName("http://NvoServiceurl.org", str1));
				localCall.setUseSOAPAction(true);
				localCall.setSOAPActionURI(str6);
				Object[] arrayOfObject = new Object[4];

				System.out.println("@V@ Adding First Parameter ie. component_name=" + str2 + " to call...");
				localCall.addParameter(new QName("http://NvoServiceurl.org", "component_name"), XMLType.XSD_STRING, ParameterMode.IN);

				System.out.println("@V@ Adding Second Parameter ie. tran_id=" + paramString1 + " to call...");
				localCall.addParameter(new QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING, ParameterMode.IN);

				System.out.println("@V@ Adding Third Parameter ie. xtra_params=" + paramString2 + " to call...");
				localCall.addParameter(new QName("http://NvoServiceurl.org", "xtra_params"), XMLType.XSD_STRING, ParameterMode.IN);

				System.out.println("@V@ Adding Fourth Parameter ie. forced_flag=" + str9 + " to call...");
				localCall.addParameter(new QName("http://NvoServiceurl.org", "forced_flag"), XMLType.XSD_STRING, ParameterMode.IN);

				arrayOfObject[0] = new String(str2);
				arrayOfObject[1] = new String(paramString1);
				arrayOfObject[2] = new String(paramString2);
				arrayOfObject[3] = new String(str9);

				localCall.setReturnType(XMLType.XSD_STRING);
				str3 = (String) localCall.invoke(arrayOfObject);

				System.out.println("@V@ [PorcpConfirmEJB:: getResponse()]Return value [" + str3 + "]");
			} 
			else
			{
				System.out.println("@V@ [PorcpConfirmEJB:: getResponse()]TranId is " + paramString1 + " Continuing for next..");
			}
		} 
		catch (Exception localException)
		{
			System.out.println("@V@ [PorcpConfirmEJB:: getResponse()]Exception Arises ..." + localException);
			throw new ITMException(localException); //Added By Mukesh Chauhan on 09/08/19
		} 
		finally
		{
			try
			{
				if (localResultSet1 != null)
				{
					localResultSet1.close();
					localResultSet1 = null;
				}
				if (localStatement1 != null)
				{
					localStatement1.close();
					localStatement1 = null;
				}
				if (localResultSet2 != null)
				{
					localResultSet2.close();
					localResultSet2 = null;
				}
				if (localStatement2 != null)
				{
					localStatement2.close();
					localStatement2 = null;
				}
			} catch (SQLException localSQLException3)
			{
				System.out.println("@V@ [PorcpConfirmEJB:: getResponse()] Following Exception arises, While Closing ResultSets,Statements & Connection in Finally..");
				localSQLException3.printStackTrace();
			}
		}

		return str3;
	}

	public String getXtraParam(Document paramDocument) throws Exception
	{
		NodeList localNodeList1 = null;
		NodeList localNodeList2 = null;
		NodeList localNodeList3 = null;
		NodeList localNodeList4 = null;
		Node localNode1 = null;
		Node localNode2 = null;
		Node localNode3 = null;
		Node localNode4 = null;
		String str1 = null;
		String str2 = null;
		String str3 = "";
		String runMode="I";

		localNodeList1 = paramDocument.getElementsByTagName("CRITERIA");
		if (localNodeList1 != null)
			localNode1 = localNodeList1.item(0);
		if (localNode1 != null)
		{
			str1 = localNode1.getFirstChild().getNodeValue();
		}
		try
		{
			if ((str1 == null) || (str1.equals("null")))
			{
				localNodeList2 = paramDocument.getElementsByTagName("siteCode");
				if (localNodeList2 != null)
					localNode2 = localNodeList2.item(0);
				if (localNode2 != null)
					str1 = localNode2.getFirstChild().getNodeValue();
				System.out.println("@V@ [PorcpConfirmEJB:: schedule()] Taking siteCode From <siteCode>Tag as .." + str1);
			}
		} catch (Exception localException1)
		{
			System.out.println("@V@ [PorcpConfirmEJB:: schedule()] Following Exception arises while getting sitecode..");
			localException1.printStackTrace();
			throw new ITMException(localException1); //Added By Mukesh Chauhan on 09/08/19
		}

		localNodeList3 = paramDocument.getElementsByTagName("OWNER");
		localNode3 = localNodeList3.item(0);
		str2 = localNode3.getFirstChild().getNodeValue();
		System.out.println("@V@ [PorcpConfirmEJB:: schedule()]Taking loginCode from <OWNER> tag ....." + str2);
		try
		{
			if (str2 == null)
			{
				localNodeList4 = paramDocument.getElementsByTagName("loginCode");
				if (localNodeList4 != null)
					localNode4 = localNodeList4.item(0);
				if (localNode4 != null)
					str2 = localNode4.getFirstChild().getNodeValue();
				System.out.println("@V@  [PorcpConfirmEJB:: schedule()] Taking loginCode From <loginCode>Tag as.." + str2);
			}
		} catch (Exception localException2)
		{
			System.out.println("@V@ [PorcpConfirmEJB:: schedule()] Exception arises while getting sitecode as ..");
			localException2.printStackTrace();
		}

		if ((str2 != null) && (str1 != null))
		{
//			str3 = "loginCode=" + str2 + "~~loginSiteCode=" + str1;
			str3 = "loginCode=" + str2 + "~~loginSiteCode=" + str1+"~~runMode="+runMode;
			System.out.println("xtraParams.." + str3);
		} else
		{
			System.out.println("@V@ loginCode.." + str2);
			System.out.println("@V@ siteCode.." + str1);
		}
		return str3;
	}
	/*
	 * public String schedule(String paramString) throws RemoteException,
	 * BaseException { System.out.println(
	 * "\n\n\n\n\n\t :::::PorcpConfirmEJB[schedule(String scheduleXML)]is called ::::"
	 * ); Connection localConnection = null; ResultSet localResultSet1 = null;
	 * ResultSet localResultSet2 = null; ResultSet localResultSet3 = null;
	 * Statement localStatement1 = null; Statement localStatement2 = null;
	 * Statement localStatement3 = null; int i = 0; int j = 0; int k = 0; int m
	 * = 1; String str1 = ""; String str2 = ""; String str3 = ""; String str4 =
	 * ""; String str5 = ""; String str6 = ""; String str7 = ""; String str8 =
	 * ""; String str9 = ""; String str10 = ""; String str11 = ""; String str12
	 * = ""; String[] arrayOfString = new String[10000]; String str13 = null;
	 * FileWriter localFileWriter = null; BufferedWriter localBufferedWriter =
	 * null; SimpleDateFormat localSimpleDateFormat1 = new
	 * SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS"); SimpleDateFormat
	 * localSimpleDateFormat2 = new SimpleDateFormat("dd-MM-yyyy hh-mm");
	 * Document localDocument1 = null;
	 * 
	 * ConnDriver localConnDriver = new ConnDriver(); GenericUtility
	 * localGenericUtility = GenericUtility.getInstance(); try { localDocument1
	 * = localGenericUtility.parseString(paramString); System.out.println(
	 * "\n\n\n\n\n PorcpConfirmEJB[schedule(String scheduleXML)] Setting XtraParam..... "
	 * ); str6 = getXtraParam(localDocument1);
	 * 
	 * String str14 = "<?xml version=\"1.0\" encoding='" +
	 * CommonConstants.ENCODING + "'?>"; try { str13 = CommonConstants.JBOSSHOME
	 * + File.separator + "applnlog" + File.separator + "PorcpConfLog_" +
	 * localSimpleDateFormat2.format(new Date()) + ".xml"; localFileWriter = new
	 * FileWriter(str13, true); localBufferedWriter = new
	 * BufferedWriter(localFileWriter); localBufferedWriter.write(str14);
	 * localBufferedWriter.newLine(); localBufferedWriter.newLine();
	 * localBufferedWriter.write("<PROCESSED");
	 * localBufferedWriter.write("    ");
	 * localBufferedWriter.write("START-TIME="); localBufferedWriter.write("'" +
	 * localSimpleDateFormat1.format(new Date()) + "'");
	 * localBufferedWriter.write(">"); localBufferedWriter.newLine();
	 * localBufferedWriter.flush(); } catch (Exception localException4) {
	 * System.out.println(
	 * "[PorcpConfirmEJB:: schedule()] Exception While Creating Log File ..");
	 * localException4.printStackTrace(); }
	 * 
	 * localConnection = localConnDriver.getConnectDB("DriverITM");
	 * localConnection.setAutoCommit(false);
	 * 
	 * str4 =
	 * "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE ='999999' AND VAR_NAME ='SITE_EXCLUDE_PORCP'"
	 * ; System.out.println(
	 * "\n[PorcpConfirmEJB:: schedule()] SQL to get Excludes Site is.." + str4);
	 * localStatement1 = localConnection.createStatement(); localResultSet1 =
	 * localStatement1.executeQuery(str4); if (localResultSet1.next()) { str11 =
	 * localResultSet1.getString("VAR_VALUE"); } if (localResultSet1 != null) {
	 * localResultSet1.close(); localResultSet1 = null; } if (localStatement1 !=
	 * null) { localStatement1.close(); localStatement1 = null; }
	 * System.out.println
	 * ("\n[PorcpConfirmEJB:: schedule()] excludedSites From DISPARM are .." +
	 * str11); Object localObject1; if (str11 == null) { str1 =
	 * "SELECT COUNT(*) AS COUNT FROM PORCP A, SITE B WHERE A.TRAN_DATE >= (SYSDATE-7) AND A.SITE_CODE >= '00' and A.SITE_CODE <= 'zz' AND A.SITE_CODE = B.SITE_CODE AND B.SITE_TYPE ='C' AND CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END ='N' AND A.TRAN_SER = 'P-RCP'"
	 * ; } else { localObject1 = new StringTokenizer(str11, ","); m =
	 * ((StringTokenizer)localObject1).countTokens(); int n = 0;
	 * System.out.println
	 * ("\n[PorcpConfirmEJB:: schedule()]No Of Sites to Excludes are..." + m);
	 * while (((StringTokenizer)localObject1).hasMoreTokens()) {
	 * arrayOfString[n] = ((StringTokenizer)localObject1).nextToken();
	 * 
	 * if (m == 1) { str12 = str12 + "'" + arrayOfString[n] + "'"; } else if (n
	 * == m - 1) str12 = str12 + "'" + arrayOfString[n] + "'"; else { str12 =
	 * str12 + "'" + arrayOfString[n] + "'" + ","; }
	 * 
	 * n++; } str12 = "(" + str12 + ")";
	 * 
	 * System.out.println("\n[PorcpConfirmEJB:: schedule()] newExcludedSites .."
	 * + str12); str1 =
	 * "SELECT COUNT(*) AS COUNT FROM PORCP A, SITE B WHERE A.TRAN_DATE >= (SYSDATE-7) AND A.SITE_CODE >= '00' and A.SITE_CODE <= 'zz' AND A.SITE_CODE = B.SITE_CODE AND B.SITE_TYPE ='C' AND CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END ='N' AND A.SITE_CODE NOT IN "
	 * + str12 + "  AND A.TRAN_SER = 'P-RCP'"; }
	 * 
	 * System.out.println(
	 * "\n[PorcpConfirmEJB:: schedule()] Query to count record/s =" + str1);
	 * 
	 * localStatement1 = localConnection.createStatement(); localResultSet1 =
	 * localStatement1.executeQuery(str1); if (localResultSet1.next()) { i =
	 * localResultSet1.getInt("COUNT"); } if (localResultSet1 != null) {
	 * localResultSet1.close(); localResultSet1 = null; } if (localStatement1 !=
	 * null) { localStatement1.close(); localStatement1 = null; } if (i == 0) {
	 * localBufferedWriter.write("      ");
	 * localBufferedWriter.write("<STATUS><![CDATA[ERROR]]></STATUS>");
	 * localBufferedWriter.newLine(); localBufferedWriter.write("      ");
	 * localBufferedWriter.write("<ERRMSG>");
	 * localBufferedWriter.write("Record/s Not Found in PORCP TABLE To Confirm.."
	 * ); localBufferedWriter.write("</ERRMSG>"); localBufferedWriter.newLine();
	 * localBufferedWriter.write("      "); localBufferedWriter.write("<NOTE>");
	 * localBufferedWriter.write(
	 * "UNSUCCESSFUL Scheduler Stopped !! To continue Again Start  Scheduler........ "
	 * ); localBufferedWriter.write("</NOTE>"); localBufferedWriter.flush();
	 * this.isError = true; return "unsuccessful"; }
	 * localBufferedWriter.write("<TOT_RECORDS>");
	 * localBufferedWriter.write("<![CDATA[" + i + "]]>");
	 * localBufferedWriter.write("</TOT_RECORDS>");
	 * localBufferedWriter.newLine(); if (str11 == null) { str2 =
	 * "SELECT A.TRAN_ID FROM PORCP A, SITE B WHERE A.TRAN_DATE >= (SYSDATE-7) AND A.SITE_CODE >= '00' and A.SITE_CODE <= 'zz' AND A.SITE_CODE = B.SITE_CODE and B.SITE_TYPE ='C' AND CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END ='N' AND A.TRAN_SER = 'P-RCP'"
	 * ; } else { str2 =
	 * "SELECT A.TRAN_ID FROM PORCP A, SITE B WHERE A.TRAN_DATE >= (SYSDATE-7) AND A.SITE_CODE >= '00' and A.SITE_CODE <= 'zz' AND A.SITE_CODE = B.SITE_CODE and B.SITE_TYPE ='C' AND CASE WHEN CONFIRMED IS NULL THEN 'N' ELSE CONFIRMED END ='N' AND A.SITE_CODE NOT IN "
	 * + str12 + " AND A.TRAN_SER = 'P-RCP'"; }
	 * 
	 * System.out.println("\n[PorcpConfirmEJB:: schedule()] Very First Query is ="
	 * + str2);
	 * 
	 * localStatement2 = localConnection.createStatement(); localResultSet2 =
	 * localStatement2.executeQuery(str2);
	 * 
	 * while (localResultSet2.next()) { str8 =
	 * localResultSet2.getString("TRAN_ID");
	 * 
	 * System.out.println(
	 * "\n\n\n\t[PorcpConfirmEJB:: schedule()] Current tranId...is.." + str8);
	 * str3 = "SELECT TRAN_ID, CONFIRMED,SITE_CODE FROM PORCP WHERE TRAN_ID = '"
	 * + str8 + "' ";
	 * System.out.println("\n[PorcpConfirmEJB:: schedule()] Query is =" + str3);
	 * localStatement3 = localConnection.createStatement();
	 * 
	 * localBufferedWriter.newLine();
	 * localBufferedWriter.write("<TRAN_ID_ATT TRAN_ID='" + str8 + "' "); Object
	 * localObject2; try { localResultSet3 = localStatement3.executeQuery(str3);
	 * } catch (SQLException localSQLException) { localObject2 =
	 * "ORA-00054: resource busy and acquire with NOWAIT specified";
	 * localBufferedWriter.write(">"); localBufferedWriter.newLine();
	 * localBufferedWriter.write("      "); localBufferedWriter.write("      ");
	 * localBufferedWriter.write("<ERRMSG>"); localBufferedWriter.write("" +
	 * localSQLException.getMessage() + "");
	 * localBufferedWriter.write("</ERRMSG>"); localBufferedWriter.newLine();
	 * localBufferedWriter.write("</TRAN_ID_ATT>");
	 * localBufferedWriter.newLine(); localBufferedWriter.flush(); if
	 * (localSQLException.getMessage() != null) { if
	 * (((String)localObject2).equals(localSQLException.getMessage().trim())) {
	 * System
	 * .out.println("\n[PorcpConfirmEJB:: schedule()] Continuing for next batchId.."
	 * ); continue; } } } if (localResultSet3.next()) { str5 =
	 * localResultSet3.getString("CONFIRMED"); str8 =
	 * localResultSet3.getString("TRAN_ID"); str10 =
	 * localResultSet3.getString("SITE_CODE"); localBufferedWriter.write("  ");
	 * localBufferedWriter.write("SITE_CODE ="); localBufferedWriter.write("'" +
	 * str10 + "'"); localBufferedWriter.write(">");
	 * localBufferedWriter.newLine();
	 * 
	 * if (localResultSet3 != null) { localResultSet3.close(); localResultSet3 =
	 * null; } if (localStatement3 != null) { localStatement3.close();
	 * localStatement3 = null; } Node localNode1 = null; localObject2 = null;
	 * NamedNodeMap localNamedNodeMap = null; String str15 = ""; String str16 =
	 * ""; String str17 = ""; String str18 = "";
	 * 
	 * String str19 = getResponse(str8, str6, localConnection);
	 * 
	 * System.out.println("\n\n\n[PorcpConfirmEJB :: schedule()] response ..as..\n"
	 * + str19);
	 * 
	 * Document localDocument2 = localGenericUtility.parseString(str19);
	 * 
	 * Node localNode2 = localDocument2.getElementsByTagName("error").item(0);
	 * if (localNode2 != null) { localNamedNodeMap = localNode2.getAttributes();
	 * } if (localNamedNodeMap != null) { str15 =
	 * localNamedNodeMap.getNamedItem("type").getNodeValue().trim(); }
	 * 
	 * System.out.println("[PorcpConfirmEJB :: schedule()] msgType.." + str15);
	 * str7 = localNamedNodeMap.getNamedItem("id").getNodeValue().trim();
	 * 
	 * System.out.println("[PorcpConfirmEJB :: schedule()] msgNo.." + str7); if
	 * ((str15 != null) && (str7 != null)) { if (str15.equalsIgnoreCase("P")) {
	 * System
	 * .out.println("[PorcpConfirmEJB :: schedule()] Returning Success.."); str9
	 * = "success"; localBufferedWriter.write("      ");
	 * localBufferedWriter.write("<STATUS><![CDATA[CONFIRMED]]></STATUS>");
	 * localBufferedWriter.newLine(); localBufferedWriter.write("      "); }
	 * else if (str15.equalsIgnoreCase("E")) {
	 * localBufferedWriter.write("      ");
	 * localBufferedWriter.write("<STATUS><![CDATA[ERROR]]></STATUS>");
	 * localBufferedWriter.newLine(); try { localNode1 =
	 * localDocument2.getElementsByTagName("message").item(0); } catch
	 * (Exception localException7) { System.out.println(
	 * "[PorcpConfirmEJB :: schedule()]Exception arises while getting error  message from response"
	 * ); } if (localNode1 != null) { str16 =
	 * localNode1.getFirstChild().getNodeValue(); }
	 * localBufferedWriter.write("      ");
	 * localBufferedWriter.write("<ERROR_ID><![CDATA[" + str7 +
	 * " ]]></ERROR_ID>"); localBufferedWriter.newLine();
	 * localBufferedWriter.write("      ");
	 * localBufferedWriter.write("<ERR_MSG>'" + str16 + "'</ERR_MSG>");
	 * localBufferedWriter.newLine(); localBufferedWriter.write("      "); try {
	 * localObject2 =
	 * localDocument2.getElementsByTagName("description").item(0); } catch
	 * (Exception localException8) { System.out.println(
	 * "[PorcpConfirmEJB :: schedule()]Exception arises while getting error description from response"
	 * ); }
	 * 
	 * if (localObject2 != null) { str17 =
	 * ((Node)localObject2).getFirstChild().getNodeValue(); }
	 * localBufferedWriter.write("<ERR_DESCR>'" + str17 + "'</ERR_DESCR>");
	 * localBufferedWriter.newLine(); localBufferedWriter.write("      "); try {
	 * str18 =
	 * localDocument2.getElementsByTagName("trace").item(0).getFirstChild
	 * ().getNodeValue(); } catch (Exception localException9) {
	 * System.out.println(
	 * "[PorcpConfirmEJB :: schedule()]Exception arises while getting error trace from response"
	 * ); } localBufferedWriter.write("<ERR_TRACE>'" + str18 + "'</ERR_TRACE>");
	 * System
	 * .out.println("[PorcpConfirmEJB :: schedule()] Returning UnSuccess..");
	 * str9 = "unsuccessful"; }
	 * 
	 * }
	 * 
	 * if ((this.isError) || (str9.equals("unsuccessful"))) { j++;
	 * System.out.println("\n[PorcpConfirmEJB:: schedule()] retValue is .." +
	 * str9); localBufferedWriter.newLine();
	 * localBufferedWriter.write("      "); localBufferedWriter.write("<NOTE>");
	 * localBufferedWriter
	 * .write("UNSUCCESSFUL !!! Skipped the current tranID ");
	 * localBufferedWriter.write("</NOTE>");
	 * 
	 * localBufferedWriter.newLine();
	 * localBufferedWriter.write("</TRAN_ID_ATT>");
	 * localBufferedWriter.newLine(); localBufferedWriter.flush(); } else { k++;
	 * localBufferedWriter.newLine();
	 * localBufferedWriter.write("</TRAN_ID_ATT>");
	 * localBufferedWriter.newLine(); localBufferedWriter.flush(); } } } if
	 * (localResultSet2 != null) { localResultSet2.close(); localResultSet2 =
	 * null; } if (localStatement2 != null) { localStatement2.close();
	 * localStatement2 = null; } } catch (Exception localException2) {
	 * System.out
	 * .println("[PorcpConfirmEJB :: schedule()]Exception Arises ..."); try {
	 * localException2.printStackTrace(); if (j == 0) {
	 * localBufferedWriter.newLine(); localBufferedWriter.write("      ");
	 * localBufferedWriter.write("" + localException2.getMessage() + "");
	 * localBufferedWriter.newLine(); localBufferedWriter.write("      ");
	 * localBufferedWriter.newLine();
	 * localBufferedWriter.write("</TRAN_ID_ATT>");
	 * localBufferedWriter.newLine(); localBufferedWriter.flush(); } throw new
	 * BaseException(localException2); } catch (Exception localException5) { } }
	 * finally { System.out.println(
	 * "\n ::::: [PorcpConfirmEJB:: schedule()]INSIDE FINALLY..Closing ResultSets, Statements & Conection......"
	 * ); try { if (localResultSet2 != null) { localResultSet2.close();
	 * localResultSet2 = null; } if (localResultSet3 != null) {
	 * localResultSet3.close(); localResultSet3 = null; } if (localStatement2 !=
	 * null) { localStatement2.close(); localStatement2 = null; } if
	 * (localStatement3 != null) { localStatement3.close(); localStatement3 =
	 * null; } if (localConnection != null) { localConnection.close();
	 * localConnection = null; } localBufferedWriter.newLine();
	 * localBufferedWriter.write("<TOT_RECORDS_CONFIRMED>");
	 * localBufferedWriter.write("<![CDATA[" + k + "]]>");
	 * localBufferedWriter.write("</TOT_RECORDS_CONFIRMED>");
	 * localBufferedWriter.newLine();
	 * localBufferedWriter.write("<PROCESS_END-TIME>");
	 * localBufferedWriter.write("<![CDATA[" + localSimpleDateFormat1.format(new
	 * Date()) + "]]>"); localBufferedWriter.write("</PROCESS_END-TIME>");
	 * localBufferedWriter.newLine(); localBufferedWriter.write("</PROCESSED>");
	 * localBufferedWriter.newLine(); localBufferedWriter.flush();
	 * localBufferedWriter.close(); System.out.println(
	 * "\n ::::: [PorcpConfirmEJB:: schedule()]INSIDE FINALLY..Closed ResultSets, Statements & Conection......"
	 * ); } catch (Exception localException10) { System.out.println(
	 * "[PorcpConfirmEJB :: schedule()]Exception...Arises in Finally...");
	 * localException10.printStackTrace(); }
	 * 
	 * System.out.println(
	 * "\n ::::: [PorcpConfirmEJB:: schedule()] INSIDE FINALLY..All Connections Closed......"
	 * ); }
	 * 
	 * return str9; }
	 * 
	 * public String getXtraParam(Document paramDocument) throws Exception {
	 * NodeList localNodeList1 = null; NodeList localNodeList2 = null; NodeList
	 * localNodeList3 = null; NodeList localNodeList4 = null; Node localNode1 =
	 * null; Node localNode2 = null; Node localNode3 = null; Node localNode4 =
	 * null; String str1 = null; String str2 = null; String str3 = "";
	 * 
	 * localNodeList1 = paramDocument.getElementsByTagName("CRITERIA"); if
	 * (localNodeList1 != null) localNode1 = localNodeList1.item(0); if
	 * (localNode1 != null) { str1 = localNode1.getFirstChild().getNodeValue();
	 * } try { if ((str1 == null) || (str1.equals("null"))) { localNodeList2 =
	 * paramDocument.getElementsByTagName("siteCode"); if (localNodeList2 !=
	 * null) localNode2 = localNodeList2.item(0); if (localNode2 != null) str1 =
	 * localNode2.getFirstChild().getNodeValue(); System.out.println(
	 * "\n\n [PorcpConfirmEJB:: schedule()] Taking siteCode From <siteCode>Tag as .."
	 * + str1); } } catch (Exception localException1) { System.out.println(
	 * "[PorcpConfirmEJB:: schedule()] Following Exception arises while getting sitecode.."
	 * ); localException1.printStackTrace(); }
	 * 
	 * localNodeList3 = paramDocument.getElementsByTagName("OWNER"); localNode3
	 * = localNodeList3.item(0); str2 =
	 * localNode3.getFirstChild().getNodeValue(); System.out.println(
	 * "\n[PorcpConfirmEJB:: schedule()]Taking loginCode from <OWNER> tag ....."
	 * + str2); try { if (str2 == null) { localNodeList4 =
	 * paramDocument.getElementsByTagName("loginCode"); if (localNodeList4 !=
	 * null) localNode4 = localNodeList4.item(0); if (localNode4 != null) str2 =
	 * localNode4.getFirstChild().getNodeValue(); System.out.println(
	 * "\n [PorcpConfirmEJB:: schedule()] Taking loginCode From <loginCode>Tag as.."
	 * + str2); } } catch (Exception localException2) { System.out.println(
	 * "[PorcpConfirmEJB:: schedule()] Exception arises while getting sitecode as .."
	 * ); localException2.printStackTrace(); }
	 * 
	 * if ((str2 != null) && (str1 != null)) { str3 = "loginCode=" + str2 +
	 * "~~loginSiteCode=" + str1; System.out.println("xtraParams.." + str3); }
	 * else { System.out.println("loginCode.." + str2);
	 * System.out.println("siteCode.." + str1); } return str3; }
	 * 
	 * String getResponse(String paramString1, String paramString2, Connection
	 * paramConnection) throws Exception { String str1 = "gbf_post"; String str2
	 * = ""; String str3 = ""; String str4 = ""; String str5 = ""; String str6 =
	 * ""; String str7 = ""; String str8 = null; String str9 = "true"; ResultSet
	 * localResultSet1 = null; ResultSet localResultSet2 = null; Statement
	 * localStatement1 = null; Statement localStatement2 = null; try { if
	 * (paramString1 != null) { System.out.println(
	 * "\n[PorcpConfirmEJB:: getResponse()] Calling WEB Services method..");
	 * str6 = "http://NvoServiceurl.org/" + str1; str8 =
	 * "SELECT SERVICE_CODE,COMP_NAME,COMP_TYPE FROM SYSTEM_EVENTS WHERE OBJ_NAME ='porcp' AND EVENT_CODE = 'pre_confirm' "
	 * ;
	 * System.out.println("\n[PorcpConfirmEJB:: getResponse()] 1st selectSql :: \n"
	 * + str8); localStatement1 = paramConnection.createStatement();
	 * localResultSet1 = localStatement1.executeQuery(str8); if
	 * (localResultSet1.next()) { str4 =
	 * localResultSet1.getString("SERVICE_CODE"); str2 =
	 * localResultSet1.getString("COMP_NAME"); str7 =
	 * localResultSet1.getString("COMP_TYPE"); } if (str8 != null) str8 = null;
	 * if (localResultSet1 != null) { localResultSet1.close(); localResultSet1 =
	 * null; } if (localStatement1 != null) { localStatement1.close();
	 * localStatement1 = null; } str8 =
	 * "SELECT SERVICE_URI FROM SYSTEM_EVENT_SERVICES WHERE SERVICE_CODE =  '" +
	 * str4 + "' ";
	 * System.out.println("\n[PorcpConfirmEJB:: getResponse()] 2nd selectSql :: \n"
	 * + str8); localStatement2 = paramConnection.createStatement();
	 * localResultSet2 = localStatement2.executeQuery(str8); if
	 * (localResultSet2.next()) { str5 =
	 * localResultSet2.getString("SERVICE_URI"); }
	 * System.out.println("\n[PorcpConfirmEJB:: getResponse()] ServiceCode :: "
	 * + str4);
	 * System.out.println("\n[PorcpConfirmEJB:: getResponse()] compName :: " +
	 * str2);
	 * System.out.println("\n[PorcpConfirmEJB:: getResponse()] compType :: " +
	 * str7);
	 * System.out.println("\n[PorcpConfirmEJB:: getResponse()] serviceURI :: " +
	 * str5);
	 * System.out.println("\n[PorcpConfirmEJB:: getResponse()] methodName :: " +
	 * str1); System.out.println("[PorcpConfirmEJB:: getResponse()]tranID :: " +
	 * paramString1);
	 * System.out.println("[PorcpConfirmEJB:: getResponse()]xtraParams :: " +
	 * paramString2);
	 * System.out.println("[PorcpConfirmEJB:: getResponse()]forcedFlag :: " +
	 * str9);
	 * 
	 * Service localService = new Service(); Call localCall =
	 * (Call)localService.createCall(); localCall.setTargetEndpointAddress(new
	 * URL(str5)); localCall.setOperationName(new
	 * QName("http://NvoServiceurl.org", str1));
	 * localCall.setUseSOAPAction(true); localCall.setSOAPActionURI(str6);
	 * Object[] arrayOfObject = new Object[4];
	 * 
	 * System.out.println("Adding First Parameter ie. component_name=" + str2 +
	 * " to call..."); localCall.addParameter(new
	 * QName("http://NvoServiceurl.org", "component_name"), XMLType.XSD_STRING,
	 * ParameterMode.IN);
	 * 
	 * System.out.println("Adding Second Parameter ie. tran_id=" + paramString1
	 * + " to call..."); localCall.addParameter(new
	 * QName("http://NvoServiceurl.org", "tran_id"), XMLType.XSD_STRING,
	 * ParameterMode.IN);
	 * 
	 * System.out.println("Adding Third Parameter ie. xtra_params=" +
	 * paramString2 + " to call..."); localCall.addParameter(new
	 * QName("http://NvoServiceurl.org", "xtra_params"), XMLType.XSD_STRING,
	 * ParameterMode.IN);
	 * 
	 * System.out.println("Adding Fourth Parameter ie. forced_flag=" + str9 +
	 * " to call..."); localCall.addParameter(new
	 * QName("http://NvoServiceurl.org", "forced_flag"), XMLType.XSD_STRING,
	 * ParameterMode.IN);
	 * 
	 * arrayOfObject[0] = new String(str2); arrayOfObject[1] = new
	 * String(paramString1); arrayOfObject[2] = new String(paramString2);
	 * arrayOfObject[3] = new String(str9);
	 * 
	 * localCall.setReturnType(XMLType.XSD_STRING); str3 =
	 * (String)localCall.invoke(arrayOfObject);
	 * 
	 * System.out.println("[PorcpConfirmEJB:: getResponse()]Return value [" +
	 * str3 + "]"); } else {
	 * System.out.println("[PorcpConfirmEJB:: getResponse()]TranId is " +
	 * paramString1 + " Continuing for next.."); } } catch (Exception
	 * localException) {
	 * System.out.println("[PorcpConfirmEJB:: getResponse()]Exception Arises ..."
	 * + localException); } finally { try { if (localResultSet1 != null) {
	 * localResultSet1.close(); localResultSet1 = null; } if (localStatement1 !=
	 * null) { localStatement1.close(); localStatement1 = null; } if
	 * (localResultSet2 != null) { localResultSet2.close(); localResultSet2 =
	 * null; } if (localStatement2 != null) { localStatement2.close();
	 * localStatement2 = null; } } catch (SQLException localSQLException3) {
	 * System.out.println(
	 * "\n[PorcpConfirmEJB:: getResponse()] Following Exception arises, While Closing ResultSets,Statements & Connection in Finally.."
	 * ); localSQLException3.printStackTrace(); } }
	 * 
	 * return str3; }
	 */
}
