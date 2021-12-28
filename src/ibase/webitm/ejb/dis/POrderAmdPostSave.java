package ibase.webitm.ejb.dis;

import org.w3c.dom.Document;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ejb.Stateless;

@Stateless
public class POrderAmdPostSave extends ValidatorEJB implements
		POrderAmdPostSaveLocal, POrderAmdPostSaveRemote {
	E12GenericUtility genericUtility= new  E12GenericUtility();
	public String postSave(String xmlString, String tranid, String editFlag,
			String xtraParams, Connection conn) throws RemoteException,
			ITMException
			{
		System.out.println("--------------PostSave called-------------");
		System.out.println("tranid------------- " + tranid);
		Document dom = null;
		String errString = "";
		try {
			if (xmlString != null && xmlString.trim().length() > 0) {
				dom = parseString(xmlString);
				errString = postSave(dom, tranid, xtraParams, conn);
			}
		} 
		catch (Exception e) 
		{
			System.out.println("Exception : POrderAmdPostSave.java : postSave : ==>\n"+ e.getMessage());
			throw new ITMException(e);
		}
		return errString;
	}

	public String postSave(Document dom, String tranid, String xtraParams,Connection conn) 
	{

		System.out.println("in PorderAmdPostSave postSave tran_id---->>["
				+ tranid + "]");
		ResultSet rs = null, rs1 = null;
		PreparedStatement pstmt = null, pstmt1 = null;
		String sql = "", sql1 = "", errorString = "", frtType = "";
		double quantityStduom = 0.0, rateStduom = 0.0, discount = 0.0, taxAmt = 0.0, taxAmtHdr = 0.0, totAmtHdr = 0.0, quantity = 0, frtRate = 0, frtamtFixed = 0, totAmtDet = 0, ordAmtHdr = 0, frtAmt = 0, frtAmtQty = 0;
		int count = 0, lineNo = 0;
		try 
		{
			//GenericUtility genericUtility = GenericUtility.getInstance();
			tranid = genericUtility.getColumnValue("amd_no", dom);
			System.out.println("purchase orderamd---->>[" + tranid + "]");
			sql = "Select quantity__stduom,rate__stduom,discount,tax_amt,line_no from poamd_det where amd_no = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranid);
			rs = pstmt.executeQuery();
			while (rs.next()) 
			{
				quantityStduom = rs.getDouble(1);
				rateStduom = rs.getDouble(2);
				discount = rs.getDouble(3);
				taxAmt = rs.getDouble(4);
				lineNo = rs.getInt(5);
				System.out.println("quantitystduom>>>>>>>>>>> "+ quantityStduom);
				System.out.println("RateStduom>>>>>>>. " + rateStduom);
				System.out.println("TaxAmt>>>>>>>>>>> " + taxAmt);
				System.out.println("LineNo>>>>>>>>>>. " + lineNo);
				totAmtDet = (quantityStduom * rateStduom)
						- ((quantityStduom * rateStduom * discount) / 100)
						+ taxAmt;
				System.out.println("TotalamtDet>>>>>>>>>[ " + totAmtDet + "]");
				sql1 = "update poamd_det set tot_amt = ? where amd_no = ? and line_no = ?";
				pstmt1 = conn.prepareStatement(sql1);
				pstmt1.setDouble(1, totAmtDet);
				pstmt1.setString(2, tranid);
				pstmt1.setInt(3, lineNo);
				count = pstmt1.executeUpdate();
				
				//added- connection close-Monika-13-05-2019
				if (pstmt1 != null) 
				{
					pstmt1.close();
					pstmt1 = null;
				}
				
				
			}
			if (pstmt != null) 
			{
				pstmt.close();
				pstmt = null;
			}
			if (pstmt1 != null) 
			{
				pstmt1.close();
				pstmt1 = null;
			}
			if (rs != null) 
			{
				rs.close();
				rs = null;
			}

			sql = "select sum(tax_amt),sum(tot_amt),sum(quantity) from poamd_det where amd_no = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranid);
			rs = pstmt.executeQuery();
			while (rs.next()) 
			{
				taxAmtHdr = rs.getDouble(1);
				totAmtHdr = rs.getDouble(2);
				quantity = rs.getDouble(3);
			}
			System.out.println("Taxamthdr>>>>>>>>>> " + taxAmtHdr);
			System.out.println("Quantity>>>>>>>>>>>[ " + quantity + "]");
			System.out.println("TotAmtHdr>>>>>>>>>[" + totAmtHdr + "]");
			ordAmtHdr = totAmtHdr - taxAmtHdr;
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
			if (count > 0) //commented by cpatil for delete all line from detail then zero value updated on hdr 
			{
				sql = "update poamd_hdr set tax_amt = ?,tot_amt = ?,ord_amt = ? where amd_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setDouble(1, taxAmtHdr);
				pstmt.setDouble(2, totAmtHdr);
				pstmt.setDouble(3, ordAmtHdr);
				pstmt.setString(4, tranid);
				count = pstmt.executeUpdate();
				System.out.println("post count---->>[" + count + "]");
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
				/*if (count > 0) 
				{
					conn.commit();
				}*/
			}
			else
			{
				sql = "update poamd_hdr set tax_amt = TAX_AMT__O,tot_amt = TOT_AMT__O,ord_amt = ORD_AMT__O where amd_no = ? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, tranid);
				count = pstmt.executeUpdate();
				System.out.println("post count---->>[" + count + "]");
				if (pstmt != null) 
				{
					pstmt.close();
					pstmt = null;
				}
				
			}

			/*else
			{
				conn.rollback();
			}*/
		} 
		catch (Exception e) 
		{
			System.out.println("Exception : POrderAmdPostSave -->["
					+ e.getMessage() + "]");
			e.printStackTrace();
			try 
			{
			}
			catch (Exception e1) 
			{
				System.out.println("Exception while rollbacking transaction....");
				e1.printStackTrace();
			}
		}
	
		return errorString;
	}

}
