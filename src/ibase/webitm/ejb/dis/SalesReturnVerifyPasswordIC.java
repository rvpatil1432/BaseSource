package ibase.webitm.ejb.dis;


import ibase.utility.E12GenericUtility;
//import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.w3c.dom.Document;

@javax.ejb.Stateless
public class SalesReturnVerifyPasswordIC extends ValidatorEJB implements SalesReturnVerifyPasswordICLocal, SalesReturnVerifyPasswordICRemote 
{
	E12GenericUtility genericUtility = new E12GenericUtility();	
	//DistCommon discommon = new DistCommon();
	//ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
	
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
		System.out.println("-----------Calling SalesReturnVerifyPasswordIC----itemChanged(String)----------");						
		try
		{
			System.out.println("xmlString ["+xmlString+"]");
			System.out.println("xmlString1 ["+xmlString1+"]");
			System.out.println("xmlString2 ["+xmlString2+"]");
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
			errString = itemChanged( currFormDataDom, hdrDataDom, allFormDataDom, objContext, currentColumn, editFlag, xtraParams );			
		}
		catch (Exception e)
		{
			System.out.println("Exception :SalesReturnVerifyPasswordIC :itemChanged(String):"+ e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}		
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
		String sql = "", tranId = "",confirm = "", passwd = "";		
		System.out.println("-----------Calling SalesReturnVerifyPasswordIC----itemChanged(Document)----------");
		System.out.println("xtraParams=["+xtraParams+"]");		
		valueXmlString = new StringBuffer( "<?xml version=\"1.0\"?><Root><Header><editFlag>" );
		valueXmlString.append( editFlag ).append( "</editFlag></Header>" );
		try
		{
			conn = getConnection();			
			if( objContext != null && objContext.trim().length() > 0 )
			{
				currentFormNo = Integer.parseInt( objContext );
			}
			
			switch ( currentFormNo )  
			{
			case 1:
			{				
				System.out.println("inside itemChanged...Case - 1..currentColumn: ["+currentColumn+"] currentFormNo: ["+currentFormNo+"]");
				valueXmlString.append( "<Detail1>\r\n" );
				if( currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
				{																					
					tranId =  checkNull(genericUtility.getColumnValue("tran_id",currFormDataDom));
					System.out.println("Pavan Rane TranID["+tranId+"]");
					sql = "select confirmed, conf_passwd from sreturn where tran_id = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);				
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						confirm = checkNull(rs.getString(1)); 
						passwd = checkNull(rs.getString(2));
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;													
					//if isnull(ls_passwd) or mconfirm = 'Y' then
					if(passwd == null || passwd.length() == 0 || "Y".equals(confirm))
					{
						valueXmlString.append( "<conf_passwd protect = '1'><![CDATA[]]></conf_passwd>\r\n" );
					}					
					else {
						//gbf_itemchg_modifier_ds(dw_currobj,"conf_passwd","protect","0")
						valueXmlString.append( "<conf_passwd protect = '0'><![CDATA[]]></conf_passwd>\r\n" );
					}												
				}
				if( currentColumn.trim().equalsIgnoreCase( "tran_id" ) )
				{
					tranId =  checkNull(genericUtility.getColumnValue("tran_id",currFormDataDom));
					sql = "select confirmed, conf_passwd from sreturn where tran_id = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, tranId);				
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						confirm = rs.getString(1); 
						passwd = rs.getString(2);
					}
					rs.close();rs = null;
					pstmt.close();pstmt = null;													
					//if isnull(ls_passwd) or mconfirm = 'Y' then
					if(passwd == null || passwd.length() == 0 || "Y".equals(confirm))
					{						
						valueXmlString.append( "<conf_passwd protect = '1'><![CDATA[]]></conf_passwd>\r\n" );	
					}					
					else
					{						
						valueXmlString.append( "<conf_passwd protect = '0'><![CDATA[]]></conf_passwd>\r\n" );	
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
			System.out.println("Exception :SalesReturnVerifyPasswordIC :itemChanged(Document):"+ e.getMessage() + ":");			
			e.printStackTrace();
			throw new ITMException(e);
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
}
