package ibase.webitm.ejb.dis;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.awt.Checkbox;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@javax.ejb.Stateless
public class PostOrderIC extends ValidatorEJB implements PostOrderICLocal, PostOrderICRemote 
{
	E12GenericUtility genericUtility = new E12GenericUtility();	
	DistCommon discommon = new DistCommon();
	ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
	
	@Override
	public String wfValData() throws RemoteException, ITMException
	{
		return "";
	}

	/**
	 * The public method is used for converting the current form data into a document(DOM)
	 * The dom is then given as argument to the overloaded function wfValData to perform validation
	 * Returns validation string if exists else returns null in XML format
	 * @param xmlString contains the current form data in XML format
	 * @param xmlString1 contains all the header information in the XML format
	 * @param xmlString2 contains the data of all the forms in XML format
	 * @param objContext represents the form number
	 * @param editFlag represents the mode of transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information such as loginEmpCode,loginCode,chgTerm etc
	 */	
	@Override
	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String errString = null;
		try
		{
			System.out.println( "xmlString inside wfValData :::::::" + xmlString);
			System.out.println( "xmlString1 inside wfValData :::::::" + xmlString1);
			System.out.println( "xmlString2 inside wfValData :::::::" + xmlString2);
			
			if(xmlString != null && xmlString.trim().length()!=0)
			{
				dom = genericUtility.parseString(xmlString); 
			}
			if(xmlString1 != null && xmlString1.trim().length()!=0)
			{
				dom1 = genericUtility.parseString(xmlString1); 
			}
			if(xmlString2 != null && xmlString2.trim().length()!=0)
			{
				dom2 = genericUtility.parseString(xmlString2); 
			}
			errString = wfValData( dom, dom1, dom2, objContext, editFlag, xtraParams);
			System.out.println( "ErrString: " + errString);
		}
		catch(Exception e)
		{
			errString = genericUtility.createErrorString(e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		return (errString); 
	}
	/**
	 * The public overloaded method takes a document as input and is used for the validation of required fields 
	 * Returns validation string if exist otherwise returns null in XML format
	 * @param currFormDataDom contains the current form data as a document object model
	 * @param hdrDataDom contains all the header information
	 * @param allFormDataDom contains the field data of all the forms 
	 * @param objContext represents form number
	 * @param editFlag represents the mode of transaction(A-Add or E-Edit)
	 * @param xtraParams contains additional information such as loginEmpCode,loginCode,chgTerm etc
	 * @throws ITMException 
	 */
	//Modified by Anjali R. on [25/10/2018][Throws ITMException]
	//public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) 
	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws ITMException
	{   				
		System.out.println("wfValData inside ----->>");
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		E12GenericUtility genericUtility;
		String errString = "", userId = "";

		int count = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
	
		StringBuffer errStringXml = new StringBuffer("<?xml version=\"1.0\"?>\r\n<Root><Errors>");
		
		try
		{	
			int currentFormNo = 0, childNodeListLength = 0, ctr = 0, cnt = 0;
			String childNodeName = "", errorType = "", errCode = "";
			ArrayList<String> errList = new ArrayList<String>();
			ArrayList<String> errFields = new ArrayList<String>();
			
			String mfrom_tran_id = "", mto_tran_id = "", ld_from_date = "", ld_to_date = "";
			
			conn = getConnection();
			genericUtility = new E12GenericUtility();	
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());

			System.out.println("xtraParam----->>["+xtraParams+"]");
			System.out.println("editFlag ------------>>["+editFlag+"]");

			if (objContext != null && objContext.trim().length() > 0) 
			{
				currentFormNo  = Integer.parseInt(objContext);
			}	
			
			switch (currentFormNo)  
			{
			case 1:
/*				System.out.println("------in detail1 validation----------------");
				System.out.println("DOM in case 1---->>["+genericUtility.serializeDom(dom).toString()+"]");
				System.out.println("DOM1 in case 1----->>["+genericUtility.serializeDom(dom1).toString()+"]");
				System.out.println("DOM2 in case 1 ----->>["+genericUtility.serializeDom(dom2).toString()+"]");	*/

				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				childNodeListLength  = childNodeList.getLength();

				for (ctr = 0; ctr < childNodeListLength; ctr++) 
				{/*					
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					System.out.println("childNodeName ------->>["+childNodeName+"]");

					if("tran_id__to".equalsIgnoreCase(childNodeName))
					{
						System.out.println("---------- Inside tran_id__to ------------------ ");
						mfrom_tran_id = checkNullAndTrim(genericUtility.getColumnValue("tran_id__fr", dom));
						mto_tran_id = checkNullAndTrim(genericUtility.getColumnValue("tran_id__to", dom));
						System.out.println("----------- mfrom_tran_id inside tran_id__to ------------"+mfrom_tran_id);
						System.out.println("----------- mto_tran_id inside tran_id__to ------------"+mto_tran_id);
						
						count = mfrom_tran_id.compareTo(mto_tran_id);
						System.out.println("count inside tran_id__to =========>>"+count);
						
						if((count < 0))
						{
							errCode = "VTPOSTORD3";		
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if("cust_code__to".equalsIgnoreCase(childNodeName))
					{
						System.out.println("---------- Inside cust_code__to ------------------ ");
						mfrom_tran_id = checkNullAndTrim(genericUtility.getColumnValue("cust_code__fr", dom));
						mto_tran_id = checkNullAndTrim(genericUtility.getColumnValue("cust_code__to", dom));
						
						System.out.println("----------- mfrom_tran_id inside cust_code__to ------------"+mfrom_tran_id);
						System.out.println("----------- mto_tran_id inside cust_code__to ------------"+mto_tran_id);
						
						count = mfrom_tran_id.compareTo(mto_tran_id);
						System.out.println("count inside cust_code__to =========>>"+count);
						
						if((count < 0))
						{
							errCode = "VTPOSTORD3";		
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
					}
					if("desp_date__fr".equalsIgnoreCase(childNodeName) || "desp_date__to".equalsIgnoreCase(childNodeName))
					{
						System.out.println("---------- Inside desp_date__fr ------------------ ");
						mfrom_tran_id = checkNullAndTrim(genericUtility.getColumnValue("tran_id__fr", dom));
						mto_tran_id = checkNullAndTrim(genericUtility.getColumnValue("tran_id__to", dom));
						ld_from_date = checkNullAndTrim(genericUtility.getColumnValue("desp_date__fr", dom));
						ld_to_date = checkNullAndTrim(genericUtility.getColumnValue("desp_date__to", dom));
						
						System.out.println("----------- mfrom_tran_id inside desp_date__fr ------------"+mfrom_tran_id);
						System.out.println("----------- mto_tran_id inside desp_date__fr ------------"+mto_tran_id);
						System.out.println("----------- ld_from_date inside desp_date__fr ------------"+ld_from_date);
						System.out.println("----------- ld_to_date inside desp_date__fr ------------"+ld_to_date);
						
						Date ld_from_date1 = sdf.parse(ld_from_date);
						Date ld_to_date1 = sdf.parse(ld_to_date);

						if(ld_from_date.length() > 0)
						{
							errCode = "VTPOSTORD3";		
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else if(ld_to_date.length() > 0)
						{
							errCode = "VTPOSTORD2";		
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}

						else if(ld_to_date1.before(ld_from_date1))
						{
							errCode = "VTPOSTORD3";	
							errList.add( errCode );
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							long diff = ld_from_date1.getTime() - ld_to_date1.getTime();
							
							if(diff > 30 && mfrom_tran_id.equalsIgnoreCase("0") && mto_tran_id.equalsIgnoreCase("Z"))
							{
								errCode = "VTDAYS1";	
								errList.add( errCode );
								errFields.add(childNodeName.toLowerCase());
								break;
							}
						}
					}
				*/}
				break;			
			}//End of switch statement

			
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if ( errList != null && errListSize > 0 )
			{
				for (cnt = 0; cnt < errListSize; cnt++ )
				{
					errCode = errList.get(cnt);
					errFldName = errFields.get(cnt);
					System.out.println("errCode .........." + errCode);
					errString = itmDBAccess.getErrorString( errFldName, errCode, userId ,"",conn);
					errorType =  errorType( conn, errCode );
					if ( errString.length() > 0)
					{
						String bifurErrString = errString.substring( errString.indexOf("<Errors>") + 8, errString.indexOf("<trace>"));
						bifurErrString =bifurErrString+errString.substring( errString.indexOf("</trace>") + 8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						System.out.println("errStringXml .........."+errStringXml);
						errString = "";
					}
					if ( errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}
				errList.clear();
				errList = null;
				errFields.clear();
				errFields = null;

				errStringXml.append("</Errors></Root>\r\n");
			}
			else
			{
				errStringXml = new StringBuffer( "" );
			}	
			errString = errStringXml.toString();
		}
		catch ( Exception e )
		{
			System.out.println ( "Exception: PostOrderIC: wfValData( Document currFormDataDom ): " + e.getMessage() + ":" );
			//Modified by Anjali R. on [25/10/2018][Start]
			e.printStackTrace();
			throw new ITMException(e);
			//Modified by Anjali R. on [25/10/2018][End]
		}
		finally
		{
			try
			{
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
			}
			catch(Exception e)
			{
				System.out.println( "Exception : PostOrderIC : " + e.getMessage() );
				//Modified by Anjali R. on [25/10/2018][Start]
				e.printStackTrace();
				throw new ITMException(e);
				//Modified by Anjali R. on [25/10/2018][End]
			}
		}
		System.out.println( "errString>>>>>>>::" + errString );
		return errString;
	}

	
	//Modified by Anjali R. on [25/10/2018][Throws ITMException]
	//private String errorType( Connection conn , String errorCode )
	private String errorType( Connection conn , String errorCode ) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null ; 
		ResultSet rs = null;
		try
		{			
			String  sql = " SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO =   ? ";

			pstmt = conn.prepareStatement( sql );			
			pstmt.setString(1, errorCode);			
			rs = pstmt.executeQuery();
			while( rs.next() )
			{
				msgType = rs.getString("MSG_TYPE");
			}
			if(pstmt != null)
			{
				pstmt.close();
				pstmt = null;
			}
			if(rs != null)
			{
				rs.close();
				rs = null;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			//Modified by Anjali R. on [25/10/2018][Start]
			throw new ITMException(ex);
			//Modified by Anjali R. on [25/10/2018][End]
		}		
		finally
		{
			try
			{
				if ( rs != null )
				{
					rs.close();
					rs = null;
				}
				if ( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}		
		return msgType;
	}


	@Override
	public String itemChanged() throws RemoteException, ITMException
	{
		return "";
	}

	@Override
	public String itemChanged(String xmlString, String xmlString1, String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException, ITMException
	{
		Document currFormDataDom = null;
		Document hdrDataDom = null;
		Document allFormDataDom = null;
		String errString = null;
		E12GenericUtility genericUtility = new E12GenericUtility();
		System.out.println("xmlString ["+xmlString+"]");
		System.out.println("xmlString1 ["+xmlString1+"]");
		System.out.println("xmlString2 ["+xmlString2+"]");
		try
		{
			if (xmlString != null && xmlString.trim().length()!=0)
			{
				currFormDataDom = genericUtility.parseString(xmlString); 
			}
			if (xmlString1 != null && xmlString1.trim().length()!=0)
			{
				hdrDataDom = genericUtility.parseString(xmlString1); 
			}
			if (xmlString2 != null && xmlString2.trim().length()!=0)
			{
				allFormDataDom = genericUtility.parseString(xmlString2); 
			}
			System.out.println ( "Calling  itemChanged( currFormDataDom, hdrDataDom, allFormDataDom, objContext, currentColumn, editFlag, xtraParams )");
			errString = itemChanged( currFormDataDom, hdrDataDom, allFormDataDom, objContext, currentColumn, editFlag, xtraParams );
			System.out.println ( "ErrString :" + errString);
		}
		catch (Exception e)
		{
			System.out.println ( "Exception : PostOrderIC:itemChanged(String,String):" + e.getMessage() + ":" );
			throw new ITMException(e);
		}
		System.out.println ( "returning from PostOrderIC: itemChanged \n[" + errString + "]" );

		return errString;
	}

	@Override
	public String itemChanged( Document currFormDataDom, Document hdrDataDom, Document allFormDataDom, String objContext, String currentColumn, String editFlag, String xtraParams ) throws RemoteException,ITMException
	{	
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;		
		int currentFormNo = 0;
		StringBuffer valueXmlString;
		String sql = "", currDate = "", ls_var_value = "", loginSite = "", userID = "", li_usr_lev = "", ls_post_upto = "", 
				ls_posting_upto_editable = "", ls_sale_order_fr = "", ls_site_code_ship = "", from_trid = "", to_trid = "", ls_cust_code = "",
				ls_descr = "",li_ib_changed = "", ld_from_date = "", adv_adj_mode = "", refresh_db = "";
		int count = 0;
		Date ldt_desp_date = null, tempDate = null;
		
		//Modified by Anjali R. on [25/10/2018][Start]
		String postOrdClubOrd = "";
		String postOrdClubPendOrd = "";
		DistCommon  distCommon = null;
		//Modified by Anjali R. on [25/10/2018][End]
		
		System.out.println("xtraParams=["+xtraParams+"]");
		System.out.println("currentColumn inside itemChanged................. : ["+currentColumn+"]");
		System.out.println("currentFormNo inside itemChanged................. : ["+currentFormNo+"]");
		
		valueXmlString = new StringBuffer( "<?xml version=\"1.0\"?><Root><Header><editFlag>" );
		valueXmlString.append( editFlag ).append( "</editFlag></Header>" );
		try
		{
			conn = getConnection();
			
			SimpleDateFormat sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//currDate = sdf.format(new java.util.Date());
			
			distCommon = new DistCommon();//Modified by Anjali R. on[25/10/2018]
			
			loginSite = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginSiteCode" ));	
			userID = checkNull(genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode"));	
			
			if( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}

			switch ( currentFormNo )  
			{
			case 1:
			{
				System.out.println("<<--------- Inside Case1 itemChanged --------------->> ");
				valueXmlString.append( "<Detail1>\r\n" );
				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
				{
					System.out.println("<<--------- Inside Case1 itemChanged for item default --------------->> ");
					
					valueXmlString.append( "<ib_changed><![CDATA[" ).append( "0" ).append( "]]></ib_changed>\r\n" );
					valueXmlString.append( "<adj_drcr><![CDATA[" ).append( "Y" ).append( "]]></adj_drcr>\r\n" );			// Adjust C.N./D.N:
					valueXmlString.append( "<adj_cust_adv><![CDATA[" ).append( "Y" ).append( "]]></adj_cust_adv>\r\n" );	// Adjust Cust Adv:
					valueXmlString.append( "<adv_adj_mode><![CDATA[" ).append( "C" ).append( "]]></adv_adj_mode>\r\n" );	// Adjust Advance
					valueXmlString.append( "<club_order><![CDATA[" ).append( "N" ).append( "]]></club_order>\r\n" );		// Club Order:
					valueXmlString.append( "<club_pend_ord><![CDATA[" ).append( "N" ).append( "]]></club_pend_ord>\r\n" );	// Club Pending Ord:
					
					//Modified by Anjali R. on [25/10/2018][club_order and club_pend_ord values set from disparam][Start]
					postOrdClubOrd  = checkNull(distCommon.getDisparams("999999", "POSTORD_CLUBORD", conn));
					postOrdClubPendOrd  = checkNull(distCommon.getDisparams("999999", "POSTORD_CLUBPENDORD", conn));
					System.out.println("postOrdClubOrd--["+postOrdClubOrd+"]postOrdClubPendOrd--["+postOrdClubPendOrd+"]");
					
					if((!"Y".equalsIgnoreCase(postOrdClubOrd)) || "NULLFOUND".equalsIgnoreCase(postOrdClubOrd))
					{
						valueXmlString.append( "<club_order><![CDATA[" ).append( "N" ).append( "]]></club_order>\r\n" );	
					}
					else
					{
						valueXmlString.append( "<club_order><![CDATA[" ).append(postOrdClubOrd ).append( "]]></club_order>\r\n" );	
					}
					
					if((!"Y".equalsIgnoreCase(postOrdClubPendOrd)) || "NULLFOUND".equalsIgnoreCase(postOrdClubPendOrd))
					{
						valueXmlString.append( "<club_pend_ord><![CDATA[" ).append( "N" ).append( "]]></club_pend_ord>\r\n" );	
					}
					else
					{
						valueXmlString.append( "<club_pend_ord><![CDATA[" ).append(postOrdClubPendOrd ).append( "]]></club_pend_ord>\r\n" );	
					}
					//Modified by Anjali R. on [25/10/2018][club_order and club_pend_ord values set from disparam][End]
					
					
					//Added By PriyankaC on 11/09/2018 To set By default value from disparm [START].
					sql = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = ? AND VAR_NAME = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, "999999");
					pstmt.setString(2, "POST_ORD_STOCK_ALLOC");
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_var_value = checkNullAndTrim(rs.getString("VAR_VALUE"));
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if(rs != null)
					{
						rs.close();
						rs = null;
					}				
					System.out.println("ls_var_value for POST_ORD_STOCK_ALLOC-------------->> "+ls_var_value);
					
					if("N".equalsIgnoreCase(ls_var_value))
					{
						valueXmlString.append( "<alloc_stock><![CDATA[" ).append( "N" ).append( "]]></alloc_stock>\r\n" );	
					}
					else
					{
						valueXmlString.append( "<alloc_stock><![CDATA[" ).append( "N" ).append( "]]></alloc_stock>\r\n" );	
					}
					//Added By PriyankaC on 11/09/2018 To set By default value from disparm [END].
					ls_var_value = "";
					sql = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = ? AND VAR_NAME = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, "999999");
					pstmt.setString(2, "ADJUST_DR_CR_NOTE");
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_var_value = checkNullAndTrim(rs.getString("VAR_VALUE"));
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if(rs != null)
					{
						rs.close();
						rs = null;
					}				
					if(ls_var_value.length() == 0)
					{
						ls_var_value = "Y";
					}
					System.out.println("ls_var_value for ADJUST_DR_CR_NOTE-------------->> "+ls_var_value);
					
					if("N".equalsIgnoreCase(ls_var_value))
					{
						valueXmlString.append( "<adj_drcr><![CDATA[" ).append( "N" ).append( "]]></adj_drcr>\r\n" );	//// Adjust C.N./D.N:
					}
					else if("Y".equalsIgnoreCase(ls_var_value))
					{
						valueXmlString.append( "<adj_drcr><![CDATA[" ).append( "Y" ).append( "]]></adj_drcr>\r\n" );	//// Adjust C.N./D.N:
					}
					
					ls_var_value = "";
					ls_var_value = checkNullAndTrim(discommon.getDisparams("999999", "ADJ_ADV_CUST_SALE", conn));
					System.out.println("ls_var_value for ADJ_ADV_CUST_SALE -------------->> "+ls_var_value);
					if("C".equalsIgnoreCase(ls_var_value)  || "NULLFOUND".equalsIgnoreCase(ls_var_value))
					{
						valueXmlString.append( "<adv_adj_mode><![CDATA[" ).append("C").append( "]]></adv_adj_mode>\r\n" );		// Adjust Advance
						adv_adj_mode = "C";
					}
					else if("S".equalsIgnoreCase(ls_var_value))
					{
						valueXmlString.append( "<adv_adj_mode><![CDATA[" ).append("S").append( "]]></adv_adj_mode>\r\n" );		// Adjust Advance
						adv_adj_mode = "S";
					}

					ls_var_value = "";
					sql = "SELECT VAR_VALUE  FROM DISPARM WHERE PRD_CODE = ? AND VAR_NAME = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, "999999");
					pstmt.setString(2, "ADJUST_CUST_ADV");
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_var_value = checkNullAndTrim(rs.getString("VAR_VALUE"));
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if(rs != null)
					{
						rs.close();
						rs = null;
					}
					System.out.println("ls_var_value for ADJUST_CUST_ADV -------------->> "+ls_var_value);

					if("N".equalsIgnoreCase(ls_var_value))
					{
						valueXmlString.append( "<adj_cust_adv><![CDATA[" ).append( "N" ).append( "]]></adj_cust_adv>\r\n" );	//// Adjust Cust Adv:
						valueXmlString.append( "<adv_adj_mode protect = '1'><![CDATA[" ).append(adv_adj_mode).append( "]]></adv_adj_mode>\r\n" ); 	// Adjust Advance
					}
					else if("Y".equalsIgnoreCase(ls_var_value))	
					{
						valueXmlString.append( "<adj_cust_adv><![CDATA[" ).append( "Y" ).append( "]]></adj_cust_adv>\r\n" );	// Adjust Cust Adv:
						valueXmlString.append( "<adv_adj_mode protect = '0'><![CDATA[" ).append(adv_adj_mode).append( "]]></adv_adj_mode>\r\n" );	// Adjust Advance
					}
					
					refresh_db = checkNullAndTrim(genericUtility.getColumnValue("refresh_db", currFormDataDom));
					valueXmlString.append( "<refresh_db protect = '1'><![CDATA[" ).append(refresh_db).append( "]]></refresh_db>\r\n" );
					
					valueXmlString.append( "<site_code><![CDATA[" ).append(loginSite).append( "]]></site_code>\r\n" );
					
					sql = "SELECT USR_LEV FROM USERS WHERE CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, userID);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						li_usr_lev = checkNullAndTrim(rs.getString("USR_LEV"));
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if(rs != null)
					{
						rs.close();
						rs = null;
					}
					System.out.println("li_usr_lev  -------------->> "+li_usr_lev);

					if(li_usr_lev.equalsIgnoreCase("2"))
					{
						valueXmlString.append( "<site_code protect = '1'><![CDATA[" ).append(loginSite).append( "]]></site_code>\r\n" );
					}
					
					ls_var_value = "";
					sql = "SELECT VAR_VALUE FROM DISPARM WHERE PRD_CODE = ? AND VAR_NAME = ?  ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, "999999");
					pstmt.setString(2, "ADJUST_NEW_PRODUCT_INVOICE");
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_var_value = checkNullAndTrim(rs.getString("VAR_VALUE"));
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if(rs != null)
					{
						rs.close();
						rs = null;
					}
					
					System.out.println("ls_var_value for ADJUST_NEW_PRODUCT_INVOICE -------------->> "+ls_var_value);
					if("N".equalsIgnoreCase(ls_var_value))
					{
						valueXmlString.append( "<adj_new_product_invoice><![CDATA[" ).append("N").append( "]]></adj_new_product_invoice>\r\n" );
					}
					else
					{
						valueXmlString.append( "<adj_new_product_invoice><![CDATA[" ).append("Y").append( "]]></adj_new_product_invoice>\r\n" );
					}
							
					ls_post_upto = checkNullAndTrim(discommon.getDisparams("999999", "POST_SORDER_UPTO", conn));
					System.out.println("ls_post_upto -------------->> "+ls_post_upto);
					if(ls_post_upto.length() > 0)
					{
						valueXmlString.append( "<posting_upto><![CDATA[" ).append(ls_post_upto).append( "]]></posting_upto>\r\n" );
					}

					ls_posting_upto_editable = checkNullAndTrim(discommon.getDisparams("999999", "POSTORD_POSTING_UPTO_EDITABL", conn));
					System.out.println("ls_posting_upto_editable -------------->> "+ls_posting_upto_editable);

					if(ls_posting_upto_editable.length() > 0 && !"Y".equalsIgnoreCase(ls_posting_upto_editable))
					{
						valueXmlString.append( "<posting_upto protect = '1'><![CDATA[" ).append(ls_post_upto).append( "]]></posting_upto>\r\n" );
					}
					else
					{
						valueXmlString.append( "<posting_upto protect = '0'><![CDATA[" ).append(ls_post_upto).append( "]]></posting_upto>\r\n" );
					}

					sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, loginSite);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_descr = checkNullAndTrim(rs.getString("DESCR"));
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if(rs != null)
					{
						rs.close();
						rs = null;
					}
					valueXmlString.append( "<site_descr><![CDATA[" ).append(ls_descr).append( "]]></site_descr>\r\n" );
					
				}
				if(currentColumn.trim().equalsIgnoreCase("tran_id__fr"))
				{
					System.out.println("item changed for tran_id__fr  -------------->> ");
					
					ls_sale_order_fr = checkNullAndTrim(genericUtility.getColumnValue("tran_id__fr", currFormDataDom));
					
					sql = "SELECT DUE_DATE, SITE_CODE__SHIP FROM SORDER WHERE SALE_ORDER = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_sale_order_fr);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ldt_desp_date = rs.getDate("DUE_DATE");
						ls_site_code_ship = checkNullAndTrim(rs.getString("SITE_CODE__SHIP"));
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if(rs != null)
					{
						rs.close();
						rs = null;
					}
					String ldt_desp = (ldt_desp_date == null) ? "" :  sdf.format(ldt_desp_date);
					
					valueXmlString.append( "<desp_date__fr><![CDATA[" ).append(ldt_desp).append( "]]></desp_date__fr>\r\n" );
					valueXmlString.append( "<site_code><![CDATA[" ).append(ls_site_code_ship).append( "]]></site_code>\r\n" );
					
					
					sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_site_code_ship);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_descr = checkNullAndTrim(rs.getString("DESCR"));
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if(rs != null)
					{
						rs.close();
						rs = null;
					}
					valueXmlString.append( "<site_descr><![CDATA[" ).append(ls_descr).append( "]]></site_descr>\r\n" );
					
				}
				if(currentColumn.trim().equalsIgnoreCase("tran_id__to"))
				{
					System.out.println("item changed for tran_id__to  -------------->> ");
					
					from_trid = checkNullAndTrim(genericUtility.getColumnValue("tran_id__fr", currFormDataDom));
					to_trid = checkNullAndTrim(genericUtility.getColumnValue("tran_id__to", currFormDataDom));
					
					System.out.println("item changed for from_trid  -------------->> "+from_trid);
					System.out.println("item changed for to_trid  -------------->> "+to_trid);
					

					count = from_trid.compareTo(to_trid);
					System.out.println("count for tran_id__to =========>>"+count);
					if((count > 0))
					{
						valueXmlString.append( "<tran_id__to><![CDATA[" ).append("").append( "]]></tran_id__to>\r\n" );
					}
					
					if(!from_trid.equalsIgnoreCase(to_trid))
					{
						valueXmlString.append( "<cust_code__fr><![CDATA[" ).append("0").append( "]]></cust_code__fr>\r\n" );
						valueXmlString.append( "<cust_code__to><![CDATA[" ).append("Z").append( "]]></cust_code__to>\r\n" );
					}
					else if(from_trid.equalsIgnoreCase(to_trid))
					{
						sql = "SELECT CUST_CODE FROM SORDER WHERE SALE_ORDER = ? "; //Changes Reverted by Nandkumar Gadkari As discuss with manoharan sir on 28/08/18 
						 //sql = "SELECT CUST_CODE__BIL FROM SORDER WHERE SALE_ORDER = ? ";//Add by Ajay on 14/05/18
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1, to_trid);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							
							ls_cust_code = checkNullAndTrim(rs.getString("CUST_CODE"));//changes  Nandkumar Gadkari on 28/08/18 
							//ls_cust_code = checkNullAndTrim(rs.getString("CUST_CODE__BIL"));//Add by Ajay on 14/05/18
						}
						if(pstmt != null)
						{
							pstmt.close();
							pstmt = null;
						}
						if(rs != null)
						{
							rs.close();
							rs = null;
						}
						valueXmlString.append( "<cust_code__fr><![CDATA[" ).append(ls_cust_code).append( "]]></cust_code__fr>\r\n" );
						valueXmlString.append( "<cust_code__to><![CDATA[" ).append(ls_cust_code).append( "]]></cust_code__to>\r\n" );
					}
					
					sql = "SELECT DUE_DATE FROM SORDER WHERE SALE_ORDER = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, to_trid);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ldt_desp_date = rs.getDate("DUE_DATE");
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if(rs != null)
					{
						rs.close();
						rs = null;
					}
					String ldt_desp = (ldt_desp_date == null) ? "" :  sdf.format(ldt_desp_date);
					valueXmlString.append( "<desp_date__to><![CDATA[" ).append(ldt_desp).append( "]]></desp_date__to>\r\n" );
					
				}
				
				
				if(currentColumn.trim().equalsIgnoreCase("desp_date__fr") || currentColumn.trim().equalsIgnoreCase("desp_date__to") 
						|| currentColumn.trim().equalsIgnoreCase("cust_code__fr") || currentColumn.trim().equalsIgnoreCase("cust_code__to"))
				{
					System.out.println("inside =========>>");
					valueXmlString.append( "<ib_changed><![CDATA[" ).append( "1" ).append( "]]></ib_changed>\r\n" );	// No such tag found
				}
				
				if(currentColumn.trim().equalsIgnoreCase("site_code"))
				{
					ls_var_value = checkNullAndTrim(genericUtility.getColumnValue("site_code", currFormDataDom));
					
					sql = "SELECT DESCR FROM SITE WHERE SITE_CODE = ? ";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, ls_var_value);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						ls_descr = checkNullAndTrim(rs.getString("DESCR"));
					}
					if(pstmt != null)
					{
						pstmt.close();
						pstmt = null;
					}
					if(rs != null)
					{
						rs.close();
						rs = null;
					}
					valueXmlString.append( "<site_descr><![CDATA[" ).append(ls_descr).append( "]]></site_descr>\r\n" );
				}
			
				if(currentColumn.trim().equalsIgnoreCase("adj_cust_adv"))	//// Adjust Cust Adv:
				{
					System.out.println("inside adj_cust_adv =========>>");
					
					ls_var_value = checkNullAndTrim(genericUtility.getColumnValue("adj_cust_adv", currFormDataDom));
					adv_adj_mode = checkNullAndTrim(genericUtility.getColumnValue("adv_adj_mode", currFormDataDom));
					System.out.println("ls_var_value for adj_cust_adv =========>>"+ls_var_value);
					
					if(ls_var_value.equalsIgnoreCase("Y"))
					{
						valueXmlString.append( "<adv_adj_mode protect = '0'><![CDATA[" ).append(adv_adj_mode).append( "]]></adv_adj_mode>\r\n" );		// Adjust Advance
					}
					else
					{
						valueXmlString.append( "<adv_adj_mode protect = '1'><![CDATA[" ).append(adv_adj_mode).append( "]]></adv_adj_mode>\r\n" );		// Adjust Advance
					}
				}
			
				if(currentColumn.trim().equalsIgnoreCase("club_pend_ord"))
				{
					System.out.println("inside club_pend_ord  =========>>");
					
					ls_var_value = checkNullAndTrim(genericUtility.getColumnValue("club_pend_ord", currFormDataDom));
					li_ib_changed = checkNullAndTrim(genericUtility.getColumnValue("ib_changed", currFormDataDom));
					ld_from_date = checkNullAndTrim(genericUtility.getColumnValue("desp_date__fr", currFormDataDom));
					
					if(ld_from_date != null || ld_from_date.length() != 0)
					{
						Calendar cal = Calendar.getInstance();
						cal.setTime(sdf.parse(ld_from_date));
						cal.add(Calendar.DATE, -30);
						tempDate = cal.getTime();
					}
					String ldt_desp = (tempDate == null) ? "" :  sdf.format(tempDate);
					
				
					if("Y".equalsIgnoreCase(ls_var_value))
					{						
						valueXmlString.append( "<club_order protect = '1'><![CDATA[" ).append("Y").append( "]]></club_order>\r\n" );
						valueXmlString.append( "<desp_date__fr><![CDATA[" ).append(ldt_desp).append( "]]></desp_date__fr>\r\n" );
						valueXmlString.append( "<tran_id__fr><![CDATA[" ).append("0").append( "]]></tran_id__fr>\r\n" );
						valueXmlString.append( "<tran_id__to><![CDATA[" ).append("Z").append( "]]></tran_id__to>\r\n" );
					}
					else
					{
						li_ib_changed = "1";
					}
				}
				valueXmlString.append( "</Detail1>\r\n" );
			} //Case 1. End
			break;
			}//End of switch block
			valueXmlString.append( "</Root>\r\n" );	 
		}
		catch (Exception e)
		{				
			e.printStackTrace();			
			//Modified by Anjali R. on [25/10/2018][Start]
			e.printStackTrace();
			throw new ITMException(e);
			//Modified by Anjali R. on [25/10/2018][End]

		}
		finally
		{	
			try
			{				
				if(rs!=null)
				{
					rs.close();
					rs = null;
				}
				if(pstmt!=null)
				{
					pstmt.close();
					pstmt = null;
				}
				if ( conn != null )
				{
					conn.close();
					conn = null;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		System.out.println( "valueXmlString.toString()>>>>>>>::"+valueXmlString.toString());
		return valueXmlString.toString();
	}

	private String checkNull(String input)	
	{
		if ( input == null )
		{
			input = "";
		}
		return input.trim();
	}

	private String checkNullAndTrim(String inputVal)
	{
		if ( inputVal == null )
		{
			inputVal = "";
		}
		else
		{
			inputVal = inputVal.trim();
		}
		return inputVal;
	}
}
