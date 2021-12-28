/*
	Developed by : Juhi Mehta
	Description	 : Post Save for Supplier HR Details .
	Date		 : 27/05/16
*/

package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ejb.Stateless;

import org.w3c.dom.Document;

import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.dis.SupplierPosFormLocal;
import ibase.webitm.ejb.dis.SupplierPosFormRemote;
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

@Stateless
public class SupplierPosForm extends ValidatorEJB implements SupplierPosFormLocal, SupplierPosFormRemote
{
	public String postSaveForm() throws RemoteException,ITMException
	{
		return "";
	}
	
	public String postSaveForm(String arg1, String arg2, String objContext, String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		GenericUtility genericUtility = new GenericUtility();
		Document headerDom = null;
		Document detailDom = null;
		String errString = "";
		try
		{
			if(arg1 != null && arg1.trim().length() > 0 )
			{
				headerDom = genericUtility.parseString(arg1);
			}
			if(arg2 != null && arg2.trim().length() > 0 )
			{
				detailDom = genericUtility.parseString(arg2);
			}
			errString = executePostSaveForm(headerDom, detailDom, objContext, editFlag, xtraParams, conn);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}
	public String executePostSaveForm(Document headerDom, Document detailDom, String objContext, String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		GenericUtility genericUtility = new GenericUtility();
		
		String suppCode = "", labourInv = "", pfRegNo = "", esicRegNo = "", eciNo = "", ptaxNo = "", lwfNo = "", errString = "" ;
		String sql = "";
		int rowCount = 0, insCnt = 0, updCnt = 0;
		try
		{	
			conn.setAutoCommit(false);
			
			suppCode = checkNull(genericUtility.getColumnValue("supp_code", headerDom));
			labourInv = checkNull(genericUtility.getColumnValue("labour_involved", headerDom));
			pfRegNo = checkNull(genericUtility.getColumnValue("pf_reg_no", headerDom));
			esicRegNo = checkNull(genericUtility.getColumnValue("esic_reg_no", headerDom));
			eciNo = checkNull(genericUtility.getColumnValue("eci_no", headerDom));
			ptaxNo = checkNull(genericUtility.getColumnValue("ptax_no", headerDom));
			lwfNo = checkNull(genericUtility.getColumnValue("lwf_no", headerDom));
			
			sql = "SELECT COUNT(*) AS ROWCOUNT FROM SUPPLIER_OTHER_INFO WHERE SUPP_CODE = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, suppCode);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				rowCount = rs.getInt("ROWCOUNT");
			}
			close(pstmt,rs);
			if( rowCount == 0 )
			{
				sql = "";
				sql = "INSERT INTO SUPPLIER_OTHER_INFO(SUPP_CODE,LABOUR_INVOLVED,PF_REG_NO,ESIC_REG_NO,ECI_NO,PTAX_NO,LWF_NO) VALUES(?,?,?,?,?,?,?)";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, suppCode);
				pstmt.setString(2, labourInv);
				pstmt.setString(3, pfRegNo);
				pstmt.setString(4, esicRegNo);
				pstmt.setString(5, eciNo);
				pstmt.setString(6, ptaxNo);
				pstmt.setString(7, lwfNo);
				insCnt = pstmt.executeUpdate();
				if (insCnt == 1)
				{
					System.out.println("Inserted Into SUPPLIER_OTHER_INFO Successfully.....:: "+insCnt);
				}
				close(pstmt,rs);
			}
			else
			{
				sql = "";
				sql = "UPDATE SUPPLIER_OTHER_INFO SET LABOUR_INVOLVED = ? ,PF_REG_NO = ?,ESIC_REG_NO = ?,ECI_NO = ?,PTAX_NO = ?,LWF_NO = ? WHERE SUPP_CODE = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, labourInv);
				pstmt.setString(2, pfRegNo);
				pstmt.setString(3, esicRegNo);
				pstmt.setString(4, eciNo);
				pstmt.setString(5, ptaxNo);
				pstmt.setString(6, lwfNo);
				pstmt.setString(7, suppCode);
				updCnt = pstmt.executeUpdate();
				if (updCnt == 1)
				{
					System.out.println("Updated SUPPLIER_OTHER_INFO Successfully.....:: "+updCnt);
				}
				close(pstmt,rs);
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
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
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return errString;
	}
	private String checkNull(String input)
	{
		if(input == null)
		{
			input = "";
		}
		else 
		{
			input = input.trim();
		}
		return input;
	}
	
	private void close(PreparedStatement pstmt,ResultSet rs)
	{
		try
		{
			if (rs != null)
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
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	

}
