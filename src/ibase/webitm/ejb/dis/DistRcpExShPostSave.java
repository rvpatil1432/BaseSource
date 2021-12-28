/********************************************************
	Title 	 : DistRcpExShPostSave
	Date  	 : 17/MAY/15
	Developer: Pankaj R.
 ********************************************************/

package ibase.webitm.ejb.dis;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import ibase.utility.CommonConstants;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;
import ibase.utility.E12GenericUtility;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.w3c.dom.Node;

import javax.ejb.Stateless;


@Stateless
public class DistRcpExShPostSave extends ValidatorEJB implements DistRcpExShPostSaveLocal,DistRcpExShPostSaveRemote {
	public String postSave(String xmlString,String tranId,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException
	{

		System.out.println("------------ postSave method called-----------------DistRcpExShPostSave : ");
		System.out.println("tranId111--->>["+tranId+"]");
		System.out.println("xml String--->>["+xmlString+"]");
		Document dom = null;
		String errString="",lineNoRcp="";
		
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
			System.out.println("Exception : DistRcpExShPostSave.java : postSave : ==>\n"+e.getMessage());
			throw new ITMException(e);
		}		
		return errString;
	}
	public String postSave(Document dom,String tranId,String xtraParams,Connection conn) throws ITMException
	{
		System.out.println("in DistRcpExShPostSave postSave tran_id---->>["+tranId+"]");
		ResultSet rs=null;
		PreparedStatement pstmt=null;
		String sql="",errorString="";
		
		NodeList parentList = null;
		NodeList childList = null;
		Node parentNode = null;
		Node childNode = null;
		String lineNoDom="",lineNo="";
		int count=0;
		
		double quantityRcp=0,quantityActual=0,rate=0;
		double shortageAmt=0,shortageAmtH=0;
		
		try
		{
			E12GenericUtility genericUtility= new  E12GenericUtility();
			
			parentList = dom.getElementsByTagName("Detail2");
			int parentNodeListLength = parentList.getLength();
			for (int prntCtr = parentNodeListLength; prntCtr > 0; prntCtr-- )
			{	
				parentNode = parentList.item(prntCtr-1);
				childList = parentNode.getChildNodes();
				for (int ctr = 0; ctr < childList.getLength(); ctr++)
				{
					childNode = childList.item(ctr);

					if ( childNode != null && childNode.getFirstChild() != null &&  
					childNode.getNodeName().equalsIgnoreCase("line_no") )
					{
						lineNoDom = childNode.getFirstChild().getNodeValue().trim();
						System.out.println("lineNo["+lineNo+"]lineNoDom["+lineNoDom+"]");
						if (lineNo.equalsIgnoreCase(lineNoDom))
						{
							System.out.println("Break from here as line No match");
							break;
						}	
					}
					if ( childNode != null && childNode.getFirstChild() != null &&  
					childNode.getNodeName().equalsIgnoreCase("qty_rcp") )
					{
						quantityRcp = Double.valueOf(childNode.getFirstChild().getNodeValue().trim());
						System.out.println("quantityRcp-->["+quantityRcp+"]");
					}
					if ( childNode != null && childNode.getFirstChild() != null &&  
							childNode.getNodeName().equalsIgnoreCase("qty_actual") )
					{
						quantityActual = Double.valueOf(childNode.getFirstChild().getNodeValue().trim());
						System.out.println("quantityActual-->["+quantityActual+"]");
					}
					if ( childNode != null && childNode.getFirstChild() != null &&  
							childNode.getNodeName().equalsIgnoreCase("rate") )
					{
						rate = Double.valueOf(childNode.getFirstChild().getNodeValue().trim());
					}
					
				}
				System.out.println("rate-->["+rate+"] lineNo-->["+lineNo+"]");
				if(quantityRcp>quantityActual)
				{
				  shortageAmt=((quantityRcp)-(quantityActual)) * rate;
				  System.out.println("ShortageAmt--->>["+shortageAmt+"]");
								
				  shortageAmtH=shortageAmtH + shortageAmt;
				  System.out.println("ShortageAmtHHH--->>["+shortageAmtH+"]");
				}
				
			}//for loop
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
			System.out.println("shortageAmtH-->["+shortageAmtH+"]");
	
			{
				sql="update distrcp_exsh_hdr set shortage_amt = ? where tran_id = ? ";
				pstmt=conn.prepareStatement(sql);
		
				pstmt.setDouble(1, shortageAmtH);
//				pstmt.setDate(2, new java.sql.Date(new java.util.Date().getTime()));
				pstmt.setString(2, tranId);
				System.out.println("Shortage Amount : "+shortageAmtH);			
				count=pstmt.executeUpdate();
				System.out.println("post count---->String>["+count+"]");
				if(count >0 )
				{
					errorString="";
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : DistRcpExShPostSave -->["+e.getMessage()+"]");
			e.printStackTrace();
			throw new ITMException(e);
			
		}finally
		{
			try
			{	
				if( errorString != null && errorString.trim().length() >  0 )
				{
					conn.rollback();
					System.out.println("Transaction rollback... ");
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
				e.printStackTrace();
				throw new ITMException(e);
			}
		}
		return errorString;
	}
}

