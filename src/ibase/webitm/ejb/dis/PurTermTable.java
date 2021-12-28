package ibase.webitm.ejb.dis;
import ibase.system.config.*;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.*;
import java.util.ArrayList;
import org.w3c.dom.*;
@javax.ejb.Stateless
public class PurTermTable extends ValidatorEJB implements PurTermTableLocal, PurTermTableRemote
{
	/*
	 * The method defined with no paramter and returns nothing
	 */
	/*
	public String wfValData() throws RemoteException, ITMException
	{
		return "";
	}
	 */
	/**
	 * The public method is used for validation of required fields which inturn called overloded method
	 * Returns validation string if exist otherwise returns null in XML format
	 * @param currXmlDataStr contains the current form data in XML format
	 * @param hdrXmlDataStr contains always header form data in XML format
	 * @param allXmlDataStr contains all forms data in XML format 
	 * @param objContext represents form no
	 * @param editFlag the mode of the transaction(A-Add or E-Edit)
	 * @param xtraParams contais additional information such as loginEmpCode,loginCode,chgTerm etc
	 */ 
	//changed by nasruddin 05-10-16
	//GenericUtility genericUtility = GenericUtility.getInstance();
	E12GenericUtility genericUtility = new E12GenericUtility();
	String winName = null;
	ValidatorEJB validator = null;

	public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		String errString = "";
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		try
		{      
			System.out.println("Val xmlString :: " + xmlString);
			System.out.println("Val xmlString1 :: " + xmlString1);
			System.out.println("Val xmlString2 :: " + xmlString2 );

			if (xmlString != null && xmlString.trim().length() > 0 )
			{
				dom = parseString(xmlString);
			}
			if (xmlString1 != null && xmlString1.trim().length() > 0 )
			{
				dom1 = parseString(xmlString1);
			}
			if ( xmlString2 != null && xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			errString = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
		}
		catch(Exception e)
		{
			throw new ITMException(e);
		}
		return errString;
	}

	public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams) throws RemoteException, ITMException 
	{
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		String childNodeName = null;
		StringBuffer valueXmlString = new StringBuffer();
		String userId = "";
		String sql = "";
		String errCode = "";
		String errorType = "";
		String errString = "", termTable = "";
		int ctr = 0;
		int currentFormNo = 0;
		int cnt = 0;
		ArrayList errList = new ArrayList();
		ArrayList errFields = new ArrayList();
		SimpleDateFormat dateFormat2 = null; //new SimpleDateFormat(genericUtility.getApplDateFormat());
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String termCode = ""; 
		ConnDriver connDriver = new ConnDriver();
		int childNodeListLength=0;
		StringBuffer errStringXml = new StringBuffer("<?xml version = \"1.0\"?> \r\n <Root> <Errors>");
		try
		{
			dateFormat2 = new SimpleDateFormat(genericUtility.getApplDateFormat());
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			userId = getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			if ((objContext != null) && (objContext.trim().length() > 0))
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			valueXmlString = new StringBuffer("<?xml version = \"1.0\"?> <Root> <header> <editFlag>");
			valueXmlString.append(editFlag).append("</editFlag> </header>");
			switch (currentFormNo)
			{
			case 1:
				parentNodeList = dom.getElementsByTagName("Detail1");
				parentNode = parentNodeList.item(0);
				childNodeList = parentNode.getChildNodes();
				//valueXmlString.append("<Detail1>");
				childNodeListLength = childNodeList.getLength();
				for (ctr = 0; ctr < childNodeListLength; ctr++)
				{
					childNode = childNodeList.item(ctr);
					childNodeName = childNode.getNodeName();
					if (childNodeName.equalsIgnoreCase("term_code"))
					{
					 
						termCode = this.genericUtility.getColumnValue("term_code", dom);
						//Comment By Nasruddin [20-SEP-16] 
						//if(termCode != null && termCode.trim().length() > 0 )
						//{
							sql = "select count(*) from pur_term where term_code = ?";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1,termCode);
							rs = pstmt.executeQuery();
							if (rs.next())
							{
								cnt = rs.getInt(1);
							}
							if (cnt == 0)
							{
								errCode = "VTTERM1";
								errList.add(errCode);
								errFields.add(childNodeName.toLowerCase());
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						//}
					}		
					//Changed By Nasruddin Start [20-SEP-16]
					else if(childNodeName.equalsIgnoreCase("term_table"))
					{
						termTable = this.genericUtility.getColumnValue("term_table", dom);
						
						sql = "SELECT COUNT(1) FROM PUR_TERM_TABLE WHERE TERM_TABLE = ?";
						pstmt = conn.prepareStatement(sql);
						pstmt.setString(1,termTable);
						rs = pstmt.executeQuery();
						if (rs.next())
						{
							cnt = rs.getInt(1);
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if (cnt == 0)
						{
							errCode = "VTITABL1";
							errList.add(errCode);
							errFields.add(childNodeName.toLowerCase());
						}
						else
						{
							if("A".equalsIgnoreCase(editFlag))
							{
								termCode = this.genericUtility.getColumnValue("term_code", dom);
								sql ="SELECT COUNT(1) FROM PUR_TERM_TABLE WHERE TERM_TABLE = ? AND TERM_CODE = ?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1,termTable);
								pstmt.setString(2, termCode);
								rs = pstmt.executeQuery();
								if (rs.next())
								{
									cnt = rs.getInt(1);
								}
								rs.close();
								rs = null;
								pstmt.close();
								pstmt = null;
								if (cnt > 0)
								{
									errCode = "VTDUPENTRY";
									errList.add(errCode);
									errFields.add(childNodeName.toLowerCase());
								}
							}
						}
					}
					//Changed By Nasruddin  [20-SEP-16] END
				}
				//valueXmlString.append("</Detail1>");
				//break;
				
			}
			int errListSize = errList.size();
			cnt = 0;
			String errFldName = null;
			if ((errList != null) && (errListSize > 0))
			{
				for (cnt = 0; cnt < errListSize; cnt++)
				{
					errCode = (String)errList.get(cnt);
					errFldName = (String)errFields.get(cnt);
					errString = getErrorString(errFldName, errCode, userId);
					errorType = errorType(conn, errCode);
					if (errString.length() > 0)
					{
						String bifurErrString = errString.substring(errString.indexOf("<Errors>") + 
								8, errString.indexOf("<trace>"));
						bifurErrString = bifurErrString + 
								errString.substring(errString.indexOf("</trace>") + 
										8, errString.indexOf("</Errors>"));
						errStringXml.append(bifurErrString);
						errString = "";
					}
					if (errorType.equalsIgnoreCase("E"))
					{
						break;
					}
				}

				errStringXml.append("</Errors> </Root> \r\n");
			}
			else
			{
				errStringXml = new StringBuffer("");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			errString = e.getMessage();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				if (conn != null)
				{
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

					conn.close();
				}
				conn = null;
			}
			catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
		errString = errStringXml.toString();
		return errString;
	}

//	public String itemChanged() throws RemoteException, ITMException
//	{
//		return "";
//	}

	/**
	 * The public method is used for itemchange of required fields which inturn called overloded method
	 * Returns itemchange string in XML format
	 * @param currXmlDataStr contains the current form data in XML format
	 * @param hdrXmlDataStr contains always header form data in XML format
	 * @param allXmlDataStr contains all forms data in XML format 
	 * @param objContext represents form no
	 * @param editFlag the mode of the transaction(A-Add or E-Edit)
	 * @param xtraParams contais additional information such as loginEmpCode,loginCode,chgTerm etc
	 */
	public String itemChanged(String xmlString, String xmlString1,String objContext,String currentColumn,String editFlag,String xtraParams)
	{
		System.out.println(" $$ xmlString ::"+xmlString);
		System.out.println(" $$ xmlString2 ::"+xmlString1);

		Document dom=null;
		Document dom1=null;
		String valueXmlString="";
		System.out.println("in item chanfe methode purchase term table -111-->>.");
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			valueXmlString=itemChanged(dom, dom1, objContext, currentColumn, editFlag, xtraParams);
		}catch(Exception e)
		{
			System.out.println(e);
		}
		return valueXmlString;
	}
//
	/**
	 * The public overloded method is used for itemchange of required fields 
	 * Returns itemchange string in XML format
	 * @param currDom contains the current form data 
	 * @param hdrDom contains always header form data
	 * @param allDom contains all forms data 
	 * @param objContext represents form no
	 * @param editFlag the mode of the transaction(A-Add or E-Edit)
	 * @param xtraParams contais additional information such as loginEmpCode,loginCode,chgTerm etc
	 */
	public String itemChanged(Document dom,Document dom1,String objContext,String currentColumn,String editFlag,String xtraParams) throws RemoteException,ITMException
	{
		String sql = "";		
		String loginCode = "";
		String descr = "",inputNos = "",printYn = "",mandatory = "" ; 
		StringBuffer valueXmlString = new StringBuffer();
		int currentFormNo = 0;
		SimpleDateFormat dateFormat2 = null;

		Connection connObject = null;
		PreparedStatement pstmt = null;
		ResultSet rSet = null;
		String termCode = "";
		//changed by nasruddin 07-10-16
		E12GenericUtility genericUtility = new E12GenericUtility();
		//GenericUtility genericUtility = GenericUtility.getInstance();
		try
		{
			dateFormat2=new SimpleDateFormat(genericUtility.getApplDateFormat());
			ConnDriver connDriver = null;
			connDriver = new ConnDriver();
			//Changes and Commented By Bhushan on 13-06-2016 :START
			//connObject = connDriver.getConnectDB("DriverValidator");
			connObject = getConnection();
			//Changes and Commented By Bhushan on 13-06-2016 :END
			
			loginCode = checkNull(genericUtility.getValueFromXTRA_PARAMS( xtraParams, "loginEmpCode" ));
			System.out.println("loginCode =["+loginCode+"]");

			if( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
				System.out.println("in item chanfe methode purchase term table --->>.");
			}
			
			valueXmlString = new StringBuffer( "<?xml version=\"1.0\"?>\r\n<Root>\r\n<Header>\r\n<editFlag>" );
			valueXmlString.append( editFlag ).append( "</editFlag>\r\n</Header>\r\n" );
			switch ( currentFormNo )
			{
			case 1:
			{
				valueXmlString.append( "<Detail1>\r\n" );			
				System.out.println("Enter in case 1");

				if ( currentColumn.trim().equalsIgnoreCase( "term_code" ))
				{
					System.out.println("Enter in itm default");
					termCode =  genericUtility.getColumnValue("term_code",dom) ;
					System.out.println("termCode--->>."+termCode);

					sql = "select descr,input_nos,print_yn,mandatory from pur_term where term_code  = ?";
					pstmt = connObject.prepareStatement(sql);
					pstmt.setString(1, termCode);
					rSet = pstmt.executeQuery();
					if( rSet.next() )
					{
						
						descr = rSet.getString("descr")==null?"":rSet.getString("descr");
						inputNos = rSet.getString("input_nos")==null?"":rSet.getString("input_nos");
						printYn = rSet.getString("print_yn")==null?"":rSet.getString("print_yn");
						mandatory = rSet.getString("mandatory")==null?"":rSet.getString("mandatory");//mandatory
					}
					valueXmlString.append( "<descr><![CDATA[" ).append( checkNull( descr ) ).append( "]]></descr>\r\n" );
					valueXmlString.append( "<input_nos><![CDATA[" ).append( checkNull( inputNos ) ).append( "]]></input_nos>\r\n" );
					valueXmlString.append( "<print_yn><![CDATA[" ).append( checkNull( printYn ) ).append( "]]></print_yn>\r\n" );
					valueXmlString.append("<mandatory><![CDATA[" ).append(mandatory).append( "]]></mandatory>\r\n");
					System.out.println("Itm default Rohan" + valueXmlString.toString());
				}
				valueXmlString.append( "</Detail1>\r\n" );		 	 
			}
			valueXmlString.append( "</Root>\r\n" );	
			break;		
			}

		}//End of TRY itemChanged 
		catch(Exception e)
		{
			System.out.println( "Exception :CRemittanceTrEJB :itemChanged(Document,String):" + e.getMessage() + ":" );
			valueXmlString = valueXmlString.append( genericUtility.createErrorString(e) );
			throw new ITMException(e);

		}//End of Catch
		finally
		{
			try
			{
				if( rSet != null )
				{
					rSet.close();
					rSet = null;
				}
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if( connObject != null && ! connObject.isClosed() )
				{
					connObject.close();
				}
			}
			catch(Exception e)
			{
				System.out.println( "Exception :CRemittanceTrEJB:itemChanged :==>\n"+e.getMessage());
				throw new ITMException(e);
			}
		}//End of Finally
		
		System.out.println(valueXmlString.toString()+"  Parsing String");
		System.out.println( "\n****ValueXmlString :" + valueXmlString.toString() + ":********" );
		return valueXmlString.toString();
	}

	private String errorType(Connection conn, String errorCode) throws ITMException
	{
		String msgType = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			String sql = "SELECT MSG_TYPE FROM MESSAGES WHERE MSG_NO = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, errorCode);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				msgType = rs.getString("MSG_TYPE");
			}
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
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
			throw new ITMException(ex); //Added By Mukesh Chauhan on 05/08/19
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
				if (pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return msgType;
	}
	//To check Null String
	private String checkNull( String input )
	{
		if( input == null )
		{
			input = "";
		}
		else
		{
			input = input.trim();
		}
		return input;
	}
	 private double round(double round,int scale) throws ITMException
	    {
	        return Math.round(round*Math.pow(10, scale))/Math.pow(10, scale);
	    }
	 private String getCurrentUpdateFlag(Node currDetail)
	    {
	        NodeList currDetailList = null;
	        String updateStatus = "",nodeName = "";
	        int currDetailListLength = 0;

	        currDetailList = currDetail.getChildNodes();
	        currDetailListLength = currDetailList.getLength();
	        for (int i=0;i< currDetailListLength;i++)
	        {
	            nodeName = currDetailList.item(i).getNodeName();
	            if (nodeName.equalsIgnoreCase("Attribute"))
	            {
	                updateStatus =currDetailList.item(i).getAttributes().getNamedItem("updateFlag").getNodeValue();
	                break;
	            }
	        }
	        return updateStatus;
	    }
}
