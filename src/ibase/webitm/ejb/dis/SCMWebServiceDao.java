package ibase.webitm.ejb.dis;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.naming.InitialContext;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ibase.system.config.AppConnectParm;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.DBAccessEJB;
import ibase.webitm.ejb.ITMUploadFileEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

public class SCMWebServiceDao extends ValidatorEJB
{
	E12GenericUtility e12GenericUtility = new E12GenericUtility();
	
	public String addTransaction(String dataXML, String userCode, String passWD, String authenticationStatus) throws ITMException
	{
		String retString = "";
		ITMUploadFileEJB imtUploadFileRemote = null;
		DBAccessEJB dbAccessEJBLocal = null;
		UserInfoBean userInfoBean = null;
		Document errDom = null, dataXMLDom = null;
		StringBuffer retSBuff = new StringBuffer("<Root>");
		File dumpDir = null;
		File logDumpDir = null;
		Connection conn = null;
		String sql = "" , tranid ="";
		PreparedStatement pstmt = null ;
		ResultSet rs = null ;
		boolean  isError = false ;
		try
		{
			System.out.println("SCMWebServiceDao.addTransaction().dataXML["+dataXML+"]");
			System.out.println("SCMWebServiceDao.addTransaction().userCode["+userCode+"]");
			
			dbAccessEJBLocal = new DBAccessEJB(); 
			//userInfoBean = dbAccessEJBLocal.createUserInfo(userCode);
			//Changed by Pankaj T. on 26-07-19 for creating user_info from authentication response - start
			Document document = new E12GenericUtility().parseString( authenticationStatus );
			userInfoBean = createUserInfo(userCode, passWD, document);
			System.out.println("SCMWebServiceDao.addTransaction() siteCode from userInfo: [" +userInfoBean.getSiteCode()+ "]");
			//Changed by Pankaj T. on 26-07-19 for creating user_info from authentication response - end
			if(dataXML != null && dataXML.trim().length() > 0)
			{
				dataXMLDom = e12GenericUtility.parseString(dataXML);
				Node detail1Node = dataXMLDom.getElementsByTagName("Detail1").item(0);
				String objName = detail1Node.getAttributes().getNamedItem("objName").getNodeValue();
				System.out.println("objName["+objName+"]");
				
				// Added By PriyankaC on 10April2018 [START].
				if("rcpdishnr_adv".equalsIgnoreCase(objName))
				{
					System.out.println("In Side Receipt advance :");
					String RefNo  = (this.e12GenericUtility.getColumnValueFromNode("ref_no", detail1Node));
					String CustCode  = (this.e12GenericUtility.getColumnValueFromNode("cust_code", detail1Node));
					//Added By PriyankaC on 16JAN2019.
					String uniqueId  = (this.e12GenericUtility.getColumnValueFromNode("unique_id", detail1Node));
					this.setUserInfo(userInfoBean);
					conn = getConnection();
					if(RefNo == null || RefNo.trim().length() ==0)
					{
						if(uniqueId == null || uniqueId.trim().length() ==0)
						{
							System.out.println("inside unique id ");
							retSBuff.append("<msg_code><![CDATA[]]></msg_code>");
							retSBuff.append("<msg_descr><![CDATA[Receipt against the given GUID is Not found]]></msg_descr>");
							retSBuff.append("<result><![CDATA[FAILED]]></result>");
							retSBuff.append("</Root>");
							retString = retSBuff.toString();
							return retString;
						}
						else
						{
							sql = "select tran_id ,ref_no from receipt where GUID = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, uniqueId);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								tranid = E12GenericUtility.checkNull(rs.getString("tran_id"));
								RefNo = E12GenericUtility.checkNull(rs.getString("ref_no"));
							}
							System.out.println("tranid["+tranid+"]" + "RefNo["+RefNo+"]");
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
						
						System.out.println("Calling setNodeValue in RefNo.:");
						setNodeValue( dataXMLDom, "ref_no",  RefNo );
						dataXML = e12GenericUtility.serializeDom(dataXMLDom);
						System.out.println("Calling dataXML :" +dataXML);
					}
					//Added By PriyankaC on 16JAN2019.
					else
					{
						sql = "select tran_id from receipt where ref_no = ? and cust_code = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, RefNo);
						pstmt.setString(2, CustCode);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							tranid = E12GenericUtility.checkNull(rs.getString("tran_id"));
						}
						System.out.println("tranid["+tranid+"]");
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
					System.out.println("Calling setNodeValue :");
					setNodeValue( dataXMLDom, "receipt_no",  tranid );
					dataXML = e12GenericUtility.serializeDom(dataXMLDom);
					System.out.println("Calling Final dataXML  :" +dataXML);
				}
				// Added By PriyankaC on 10April2018 [END].
				if( CommonConstants.UPLOAD_LOC == null && CommonConstants.J2EE_VERSION.equals( "1" ))
				{
					System.out.println("UPLOAD_LOC is null");
					dumpDir = new File(( new File( CommonConstants.APPLICATION_CONTEXT ) ).getParentFile().getParent() + File.separator + "dump" );				
					logDumpDir = new File( dumpDir .getParent()  + File.separator + "logs") ;
				}
				else
				{
					 System.out.println("DUMP LOG::");
					dumpDir = new File( ( new File( CommonConstants.UPLOAD_LOC ) ) + File.separator + "dump" );
					if ( ! dumpDir.exists() )
					{
						dumpDir.mkdir();
					}
					
					logDumpDir = new File( ( new File( CommonConstants.UPLOAD_LOC ) ) + File.separator + "logs" );
					if ( ! logDumpDir.exists() )
					{
						logDumpDir.mkdir();
					}
				}
				
				String newFileName = System.currentTimeMillis() + "_" + userInfoBean.getLoginCode() + "_hdr_det_data.xml";
				
				BufferedWriter writer = null;
				try
				{
				    writer = new BufferedWriter( new FileWriter( dumpDir.getAbsolutePath() + File.separator + newFileName));
				    writer.write( dataXML);
				}
				catch ( IOException e)
				{
					System.out.println("SCMWebServiceDao.addTransaction().writer closing exption in catch["+e.getMessage()+"]");
				}
				finally
				{
				    try
				    {
				        if ( writer != null)
				        {
				        	writer.close();
				        }
				    }
				    catch ( IOException e)
				    {
				    	System.out.println("writer closing exception in finally["+e.getMessage()+"]");
				    }
				}
				
				String [] fileInfoArr = new String[8];
				fileInfoArr[0] =  "false";
				fileInfoArr[1] = newFileName;
				fileInfoArr[2] = objName;
				fileInfoArr[3] = "";
				fileInfoArr[4] = "";
				fileInfoArr[5] = "false";
				fileInfoArr[6] = "false";
				fileInfoArr[7] = "true";

				imtUploadFileRemote = new ITMUploadFileEJB();
				retString = imtUploadFileRemote.insertFileData(fileInfoArr , userInfoBean , dumpDir , CommonConstants.APPLICATION_CONTEXT, "", false, false);
				
				System.out.println("retString after upload call["+retString+"]");
				
				
				if(retString != null && retString.trim().length()>0 && retString.indexOf("<Errors>") >-1)
				{
					errDom = e12GenericUtility.parseString(retString);
					
					NodeList errNodeList = errDom.getElementsByTagName("error");
					int errNodeListLen = errNodeList.getLength();
					
					for(int i=0; i<errNodeListLen; i++)
					{
						String errorMessage = "";
						Node eachErrNode = errNodeList.item(i);
						NodeList eachErrNodeList = eachErrNode.getChildNodes();
						int eachErrNodeListLen = eachErrNodeList.getLength();
						
						String msgNo = eachErrNode.getAttributes().getNamedItem("id").getNodeValue();
						System.out.println("msgNo["+msgNo+"]");
						for(int j=0; j<eachErrNodeListLen; j++)
						{
							Node eachNode = eachErrNodeList.item(j);
							String nodeName = eachNode.getNodeName();
							String nodeValue = eachNode.getFirstChild() != null ? eachNode.getFirstChild().getNodeValue():"";
							
							if("description".equalsIgnoreCase(nodeName))
							{
								errorMessage = nodeValue;
							}
						}
						retSBuff.append("<tran_id><![CDATA[]]></tran_id>");
						retSBuff.append("<msg_code><![CDATA["+msgNo+"]]></msg_code>");
						retSBuff.append("<msg_descr><![CDATA["+errorMessage+"]]></msg_descr>");
						retSBuff.append("<result><![CDATA[FAILED]]></result>");
					}
				}
				else
				{
					if ( retString.indexOf("Success") > -1 && retString.indexOf("<TranID>") > 0 )
					{
					    String tranId = retString.substring( retString.indexOf("<TranID>")+8, retString.indexOf("</TranID>"));
					    
					    retSBuff.append("<tran_id><![CDATA["+tranId+"]]></tran_id>");
						retSBuff.append("<msg_code><![CDATA[]]></msg_code>");
						retSBuff.append("<msg_descr><![CDATA[Transaction is saved]]></msg_descr>");
						retSBuff.append("<result><![CDATA[SUCCESS]]></result>");
					}
				}
				retSBuff.append("</Root>");
				
				retString = retSBuff.toString();
			}
			
			System.out.println("Final retString["+retString+"]");
		}
		catch(Exception e)
		{
			System.out.println("SCMWebServiceDao.addTransaction()["+e.getMessage()+"]");
			throw new ITMException (e);
		}
		finally 
		{
			try
			{
				if(conn != null && !conn.isClosed() )
				{
					conn.close();
					conn = null;
				}	
			}
			catch (SQLException sql1)
			{
				System.out.println("SQL Exception" +sql1.getMessage());
			}
			
		}
		
		return retString;
	}
	
	private InitialContext getInitialContext() throws ITMException
	{
		InitialContext ctx = null;
		try
		{
			AppConnectParm appConnect = new AppConnectParm();
			ctx = new InitialContext( appConnect.getProperty() );
		}
		catch( ITMException itme )
		{
			System.out.println( "SCMWebServiceDao : getInitialContext : "+ itme.getMessage() );
			throw itme;
		}
		catch(Exception e)
		{
			System.out.println( "SCMWebServiceDao : getInitialContext : "+ e.getMessage() );
			throw new ITMException(e);
		}
		return ctx;
	}
	
	//PriyankaC on 28FEB 2018 to get bank result..[START]
	
		public String getResult(List<String> siteCode , String userCode ,String password , String masterReq) throws ITMException
		{
			Connection conn = null;
			DBAccessEJB dbAccessEJBLocal = null;
			UserInfoBean userInfoBean = null;
			List<Bankdtl> bankdtlList = new ArrayList<Bankdtl>();
			HashMap<String,ArrayList<Bankdtl>> sh = new HashMap<String,ArrayList<Bankdtl>>();
			String bankSQL = "";
			String bankCode = "", bankName = "",userInfo = "",siteCd="",sitecod="";
			Bankdtl bank = null;
			PreparedStatement pstmt = null ;
			ResultSet rs = null ;
			boolean  isError = false ;

			try
			{
				dbAccessEJBLocal = new DBAccessEJB(); 
				userInfoBean = dbAccessEJBLocal.createUserInfo(userCode);
				setUserInfo(userInfoBean);
				userInfo = userInfoBean.toString();
				if("BANK_INFO".equalsIgnoreCase(masterReq))
				{
					System.out.println("Inside of  BANK_INFO : ");
					if(siteCode.isEmpty() == false)
					{
						Iterator itr = siteCode.iterator();
						while(itr.hasNext())
						{
							siteCd = siteCd +"'"+(String) itr.next()+ "'"+",";		
						}
					}
					if(siteCd != null && siteCd.length() != 0)
					{
						siteCd = siteCd.substring(0,siteCd.length()-1);
					}
					else
					{
						siteCd = "''";
					}
					System.out.println("siteCd : " +siteCd);
					conn = getConnection();
					if("'ALL_SITE'".equalsIgnoreCase(siteCd))
					{
						bankSQL   = " SELECT BANK_CODE,BANK_NAME,SITE_CODE FROM BANK GROUP BY( BANK_CODE,BANK_NAME, SITE_CODE)";
					}
					else
					{
						bankSQL   = "SELECT BANK_CODE, BANK_NAME,SITE_CODE FROM BANK WHERE SITE_CODE IN  ("+siteCd+") ";
					}
					System.out.println("SCMWebServiceDao.addTransaction().userInfo["+"'"+userInfo+"'"+"]");
					pstmt = conn.prepareStatement(bankSQL);
					rs = pstmt.executeQuery();
					while(rs.next())
					{
						bankCode = rs.getString("BANK_CODE");
						bankName = rs.getString("BANK_NAME");
						sitecod = rs.getString("SITE_CODE");

						bank = new Bankdtl(bankCode,bankName);
						if(sh.containsKey(sitecod))
						{
							ArrayList<Bankdtl> temp = sh.get(sitecod);							
							temp.add(bank);
							sh.put(sitecod, temp);
						}
						else
						{
							ArrayList<Bankdtl> temp2 = new ArrayList<Bankdtl>();
							temp2.add(bank);
							sh.put(sitecod, temp2);
						}
					}
					System.out.println("Final Map is :::"+sh);
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch (Exception e)
			{
				isError = true;
				System.out.println("StockWebServiceDao.getSKUStockData()"+ e);
				e.printStackTrace();
				throw new ITMException(e);
			}
			finally
			{
				try
				{
					if(isError)
					{
						if(rs != null)
						{
							rs.close();
							rs = null;
						}
						if(pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
						if(conn != null && !conn.isClosed())
						{
							conn.close();
							conn = null;
						}
					}
				}
				catch(Exception e)
				{
					System.out.println("StockWebServiceDao.getSKUStockData()");
					e.printStackTrace();
				}
			}
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			return gson.toJson(sh);

		}
		
		//Added By PriyankaC on 10April [START]
		private static void setNodeValue( Document dom, String nodeName, String nodeVal ) throws Exception
		{
			Node tempNode = dom.getElementsByTagName( nodeName ).item(0);

			if( tempNode != null )
			{
				if( tempNode.getFirstChild() == null )
				{
					CDATASection cDataSection = dom.createCDATASection( nodeVal );
					tempNode.appendChild( cDataSection );
				}
				else
				{
					tempNode.getFirstChild().setNodeValue(nodeVal);
				}
			}
			else
			{
				dom.createElement(nodeName).setNodeValue(nodeVal);
			}
			    tempNode = null;
		}
		//Added By PriyankaC on 10April [END]
		
	//Added by Pankaj T. on 26-07-19 for creating user_info from authentication response - start
	private ibase.utility.UserInfoBean createUserInfo( String user, String passwd, Document document ) throws ITMException
	{
		ibase.utility.UserInfoBean userInfo = null;
		try
		{
			userInfo = new ibase.utility.UserInfoBean();
			userInfo.setLoginPwd( passwd );

			String temp = null;
			if (document.getElementsByTagName("USER_CODE").item(0) != null) { 
				userInfo.setLoginCode(temp = document.getElementsByTagName("USER_CODE").item(0).getFirstChild().getNodeValue());  
			}
			if (document.getElementsByTagName("ENTRY_ID").item(0) != null) { 
				userInfo.setEntryId(temp = document.getElementsByTagName("ENTRY_ID").item(0).getFirstChild().getNodeValue());  
			}
			if (document.getElementsByTagName("ENTRY_TYPE").item(0) != null) { 
				userInfo.setEntryType(temp = document.getElementsByTagName("ENTRY_TYPE").item(0).getFirstChild().getNodeValue());  
			}
			if (document.getElementsByTagName("PROFILE_ID").item(0) != null) 
			{
				userInfo.setProfileId(document.getElementsByTagName("PROFILE_ID").item(0).getFirstChild().getNodeValue()); 
			}
			if (document.getElementsByTagName("EMP_CODE").item(0) != null) 
			{
				userInfo.setEmpCode(document.getElementsByTagName("EMP_CODE").item(0).getFirstChild().getNodeValue()); 
			}
			if (document.getElementsByTagName("EMP_FNAME").item(0) != null) 
			{
				userInfo.setEmpFName(document.getElementsByTagName("EMP_FNAME").item(0).getFirstChild().getNodeValue()); 
			}
			if (document.getElementsByTagName("EMP_MNAME").item(0) != null) 
			{
				userInfo.setEmpMName(document.getElementsByTagName("EMP_MNAME").item(0).getFirstChild().getNodeValue()); 
			}
			if (document.getElementsByTagName("EMP_LNAME").item(0) != null) 
			{
				userInfo.setEmpLName(document.getElementsByTagName("EMP_LNAME").item(0).getFirstChild().getNodeValue()); 
			}
			if (document.getElementsByTagName("SITE_CODE").item(0) != null) 
			{
				userInfo.setSiteCode(document.getElementsByTagName("SITE_CODE").item(0).getFirstChild().getNodeValue()); 
			}
			if (document.getElementsByTagName("IS_BI_USER").item(0) != null) 
			{
				userInfo.setIsBIUser(document.getElementsByTagName("IS_BI_USER").item(0).getFirstChild().getNodeValue()); 
			}
			if ( document.getElementsByTagName( "PROFILE_ID__RES" ).item(0) != null && document.getElementsByTagName( "PROFILE_ID__RES" ).item(0).getFirstChild() != null )  
			{
				userInfo.setProfileIdRes(document.getElementsByTagName( "PROFILE_ID__RES" ).item(0).getFirstChild().getNodeValue() ); 
			}
			if ( document.getElementsByTagName( "LOGGER_TYPE" ).item(0) != null && document.getElementsByTagName( "LOGGER_TYPE" ).item(0).getFirstChild() != null )  
			{
				userInfo.setLoggerType(document.getElementsByTagName( "LOGGER_TYPE" ).item(0).getFirstChild().getNodeValue() ); 
			}
			if ( document.getElementsByTagName( "STAN_CD__HQ_DESCR" ).item(0) != null && document.getElementsByTagName( "STAN_CD__HQ_DESCR" ).item(0).getFirstChild() != null )  
			{
				userInfo.setStanCode(document.getElementsByTagName( "STAN_CD__HQ_DESCR" ).item(0).getFirstChild().getNodeValue() ); 
			}
			if ( document.getElementsByTagName( "DESIGNATION" ).item(0) != null && document.getElementsByTagName( "DESIGNATION" ).item(0).getFirstChild() != null )  
			{
				userInfo.setDesignation(document.getElementsByTagName( "DESIGNATION" ).item(0).getFirstChild().getNodeValue() ); 
			}
			if ( document.getElementsByTagName( "DATE_JOIN" ).item(0) != null && document.getElementsByTagName( "DATE_JOIN" ).item(0).getFirstChild() != null )  
			{
				userInfo.setDateJoin(document.getElementsByTagName( "DATE_JOIN" ).item(0).getFirstChild().getNodeValue() ); 
			}
			if ( document.getElementsByTagName( "TRANS_DB" ).item(0) != null && document.getElementsByTagName( "TRANS_DB" ).item(0).getFirstChild() != null )  
			{
				userInfo.setTransDB(document.getElementsByTagName( "TRANS_DB" ).item(0).getFirstChild().getNodeValue() ); 
			}
			if ( document.getElementsByTagName( "USER_THEME" ).item(0) != null && document.getElementsByTagName( "USER_THEME" ).item(0).getFirstChild() != null )  
			{
				userInfo.setUserTheme( document.getElementsByTagName( "USER_THEME" ).item(0).getFirstChild().getNodeValue() ); 
			}
			if ( document.getElementsByTagName( "UX_INTERFACE" ).item(0) != null && document.getElementsByTagName( "UX_INTERFACE" ).item(0).getFirstChild() != null )  
			{
				userInfo.setUxInterface( document.getElementsByTagName( "UX_INTERFACE" ).item(0).getFirstChild().getNodeValue() ); 
			}
			if ( document.getElementsByTagName( "USER_GEO_FENCE_ENABLED" ).item(0) != null && document.getElementsByTagName( "USER_GEO_FENCE_ENABLED" ).item(0).getFirstChild() != null )  
			{
				userInfo.setUserGeoFence( document.getElementsByTagName( "USER_GEO_FENCE_ENABLED" ).item(0).getFirstChild().getNodeValue() ); 
			}
			if ( document.getElementsByTagName( "USER_NAME" ).item(0) != null && document.getElementsByTagName( "USER_NAME" ).item(0).getFirstChild() != null )  
			{
				userInfo.setUserName(document.getElementsByTagName( "USER_NAME" ).item(0).getFirstChild().getNodeValue() ); 
			}
			if ( document.getElementsByTagName( "IS_PASSWORD_STORE" ).item(0) != null && document.getElementsByTagName( "IS_PASSWORD_STORE" ).item(0).getFirstChild() != null )
			{
				userInfo.setIsPasswordStore(document.getElementsByTagName( "IS_PASSWORD_STORE" ).item(0).getFirstChild().getNodeValue() );
			}
			if ( document.getElementsByTagName( "DEFAULT_MENU" ).item(0) != null && document.getElementsByTagName( "DEFAULT_MENU" ).item(0).getFirstChild() != null )
			{
				userInfo.setDefaultMenu(document.getElementsByTagName( "DEFAULT_MENU" ).item(0).getFirstChild().getNodeValue() );
			}
			if ( document.getElementsByTagName( "FEATURES" ).item(0) != null && document.getElementsByTagName( "FEATURES" ).item(0).getFirstChild() != null )
			{
				userInfo.setFeatures( document.getElementsByTagName( "FEATURES" ).item(0).getFirstChild().getNodeValue() );
			}
			if ( document.getElementsByTagName( "ENTERPRISE" ).item(0) != null && document.getElementsByTagName( "ENTERPRISE" ).item(0).getFirstChild() != null )  
			{
				userInfo.setEnterprise( document.getElementsByTagName( "ENTERPRISE" ).item(0).getFirstChild().getNodeValue() ); 
			}
			if ( document.getElementsByTagName( "USER_GEO_POS_OPTION" ).item(0) != null && document.getElementsByTagName( "USER_GEO_POS_OPTION" ).item(0).getFirstChild() != null )  
			{
				userInfo.setGeoPosOption(( document.getElementsByTagName( "USER_GEO_POS_OPTION" ).item(0).getFirstChild().getNodeValue()) ); 
			}
			if ( document.getElementsByTagName( "ENTERPRISE_DESCR" ).item(0) != null && document.getElementsByTagName( "ENTERPRISE_DESCR" ).item(0).getFirstChild() != null )  
			{
				userInfo.setEnterpriseDescr( document.getElementsByTagName( "ENTERPRISE_DESCR" ).item(0).getFirstChild().getNodeValue() ); 
			}
			if ( document.getElementsByTagName( "NETWORK_OPTION" ).item(0) != null && document.getElementsByTagName( "NETWORK_OPTION" ).item(0).getFirstChild() != null )  
			{
				userInfo.setUserNetworkOption(( document.getElementsByTagName( "NETWORK_OPTION" ).item(0).getFirstChild().getNodeValue()) ); 
			}
			if ( document.getElementsByTagName( "IS_ALLOW_OFFLINE_EDIT" ).item(0) != null && document.getElementsByTagName( "IS_ALLOW_OFFLINE_EDIT" ).item(0).getFirstChild() != null )  
			{
				userInfo.setIsAllowOfflineEdit(( document.getElementsByTagName( "IS_ALLOW_OFFLINE_EDIT" ).item(0).getFirstChild().getNodeValue()) ); 
			}
			if ( document.getElementsByTagName( "IS_ALLOW_OFFLINE_DELETE" ).item(0) != null && document.getElementsByTagName( "IS_ALLOW_OFFLINE_DELETE" ).item(0).getFirstChild() != null )  
			{
				userInfo.setIsAllowOfflineDelete(( document.getElementsByTagName( "IS_ALLOW_OFFLINE_DELETE" ).item(0).getFirstChild().getNodeValue()) ); 
			}
		}
		catch( Exception e )
		{
			System.out.println("SCMWebServiceDao.createUserInfo() Exception: createUserInfo:==>\n"+e); 
			throw new ITMException( e );
		}
		System.out.println("SCMWebServiceDao.createUserInfo() UserInfoBean Created....." ); 
		return userInfo;
	}
	//Added by Pankaj T. on 26-07-19 for creating user_info from authentication response - end
}
