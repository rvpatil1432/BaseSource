package ibase.webitm.ejb.dis;
import org.w3c.dom.Document;

import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.utility.TransIDGenerator;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;

@Stateless
public class SRLContainerPos extends ValidatorEJB implements SRLContainerPosLocal,SRLContainerPosRemote {
	E12GenericUtility genericUtility= new  E12GenericUtility();
	public String postSave(String xmlString,String serialNo,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException
	{
		System.out.println("------- SRLContainerPos postSave method called-----------------SaleOrderPostSave : ");
		System.out.println("tranId Is["+serialNo+"] \n xml String is ["+xmlString+"] \n ");
		Document dom = null;
		String errString="";
		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);				
				errString = postSave(dom,serialNo,xtraParams,conn);
				System.out.println("errString >>>>>> ["+errString+"]");
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : SRLContainerPos.java : postSave : ==>\n"+e.getMessage());
			throw new ITMException(e);
		}		
		return errString;
	}	
	public String postSave(Document dom,String tranId,String xtraParams,Connection conn) throws ITMException
	{
		boolean isError = false;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//Commented and Added by sarita on 13 MAY 2019 [START]
		//String errorString = null;
		String errorString = "";
		//Commented and Added by sarita on 13 MAY 2019 [END]
		String sql = "" ,invType = "",headerItemCode = "",dtllotSl = "" ;
		int countDet = 1 , countHdr = 0;
		ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();
		try
		{			
			sql = "select inventory_type , item_code from srl_container where serial_no = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				invType = checkNullAndTrim(rs.getString("inventory_type"));
				headerItemCode = checkNullAndTrim(rs.getString("item_code"));
				System.out.println("inventory_type ["+invType+"] \t item_code ["+headerItemCode+"]");
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
			//Validation for Itemcode if inventory type is 0 [START]
			if("0".equalsIgnoreCase(invType))
			{
				sql = "select count(*) as cnt from srl_contents where ? in item_code and serial_no = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1,headerItemCode);
				pstmt.setString(2,tranId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					countHdr = rs.getInt("cnt");
					System.out.println("countHdr is ::: ["+countHdr+"]");
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
				if(countHdr == 0)
				{
					errorString = itmDBAccessEJB.getErrorString("","VTITEMINVT","","",conn);
					isError = true;
					return errorString;
				}
			}
			//Validation for Itemcode if inventory type is 0 [END]
			
			//Validation for lot_sl = > In details lot_sl should not be same [START]
			sql = "select distinct(lot_sl),count(1) from srl_contents where serial_no= ? group by lot_sl having count(1) > 1";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				countDet = 2;
				System.out.println("countDet ["+countDet+"]] ");
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
			System.out.println("Count ::::: ["+countDet+"]");
			if(countDet > 1)
			{
				errorString = itmDBAccessEJB.getErrorString("","INVDTLRCD","","",conn);
				isError = true;
				return errorString;
			}		
			//Validation for lot_sl = > In details lot_sl should not be same [END]					
		}
		catch(Exception e)
		{
			System.out.println("Exception : SRLContainerPos -->["+e.getMessage()+"]");
			e.printStackTrace();
			try
			{
				if(isError == true)
				{
					conn.rollback();
				}
				else
				{
					conn.commit();
				}
			}
			catch(Exception e1)
			{
				System.out.println("Exception while rollbacking transaction....");
				e1.printStackTrace();
			}
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		finally
		{
			try
			{
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
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("errorString :: ["+errorString+"]");
		return errorString;
	}	
	private static String checkNullAndTrim(String input)
	{
		if (input==null)
		{
			input="";
		}
		return input.trim();
	}
}
