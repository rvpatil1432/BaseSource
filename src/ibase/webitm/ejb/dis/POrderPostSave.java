package ibase.webitm.ejb.dis;
import org.w3c.dom.Document;


import ibase.utility.BaseLogger;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;


import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;




import javax.ejb.Stateless;

@Stateless
public class POrderPostSave extends ValidatorEJB implements POrderPostSaveLocal,POrderPostSaveRemote {
	E12GenericUtility genericUtility= new  E12GenericUtility();
	public String postSave(String xmlString,String tranId,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException
	{

		//System.out.println("------------ postSave method called-----------------POrderPostSave : ");
		BaseLogger.log("9", null, null, "inside postSave method called--tranId["+tranId+"]xmlString["+xmlString+"]");
		//System.out.println("tranId111--->>["+tranId+"]");
		//System.out.println("xml String--->>["+xmlString+"]");
		Document dom = null;
		String errString="";
		
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
			//System.out.println("Exception : POrderPostSave.java : postSave : ==>\n"+e.getMessage());
			BaseLogger.log("0", null, null, "Exception : POrderPostSave : postSave : ==>\n"+e.getMessage());
			throw new ITMException(e);
		}		
		return errString;
	}
	public String postSave(Document dom,String tranId,String xtraParams,Connection conn)
	{
		System.out.println("in PorderPostSave postSave tran_id---->>["+tranId+"]");
		ResultSet rs=null,rs1=null;
		PreparedStatement pstmt=null,pstmt1=null;
		String sql="",sql1="",errorString="",frtType = "";
		double quantityStduom = 0,rateStduom=0,discount=0,taxAmt=0,taxAmtHdr=0,totAmtHdr=0,
				quantity =0,frtRate = 0,frtAmtFixed =0,totAmtDet=0,ordAmtHdr=0,frtAmt = 0,frtAmtQty=0;
		int count=0,lineNo=0;
		//Added by sarita on 11 JUN 2018 to show validation[POADVMIS] on post save[START]
		ITMDBAccessEJB itmDBAccessEJB =  new ITMDBAccessEJB();
		//changes by sarita to change relAmt as double [START]
		//String ordAmt = "", totAmt = "", type = "", amtType = "",relAmt = "";
		String ordAmt = "", totAmt = "", type = "", amtType = "";
		double relAmt = 0;
		//changes by sarita to change relAmt as double [END]
		double lcAmount = 0 , advAmt = 0;
		//Added by sarita on 11 JUN 2018 to show validation[POADVMIS] on post save [END]
		try{
			//GenericUtility genericUtility = GenericUtility.getInstance();	
			tranId = genericUtility.getColumnValue("purc_order",dom);
			//System.out.println("purc_order--->>["+tranId+"]");
			BaseLogger.log("9", null, null, "purc_order--->>["+tranId+"]");
			sql = "Select Quantity__Stduom,Rate__Stduom,Discount,Tax_Amt,line_no from porddet where purc_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				quantityStduom=rs.getDouble(1);
				rateStduom =rs.getDouble(2);
				discount = rs.getDouble(3);
				taxAmt = rs.getDouble(4);
				lineNo = rs.getInt(5);
				
				//System.out.println("QuantityStduom>>>>>>> [" +quantityStduom +"]");
				//System.out.println("Rate_Stduom>>>>>>>>>>>> [" + rateStduom +"]");
				//System.out.println("Discount >>>>>>>>>>>>>>>>>> [" + discount +"]");
				//System.out.println("TaxAmt>>>>>>>>>>>>>>>>> [" +taxAmt +"]" );
				BaseLogger.log("9", null, null, "QuantityStduom[" +quantityStduom +"] Rate_Stduom["+rateStduom+"] Discount["+discount+"] taxAmt["+taxAmt+"]");
						
				totAmtDet= (quantityStduom * rateStduom)-((quantityStduom * rateStduom * discount)/100) + taxAmt;
				//System.out.println("totamtdet>>>>>>>>>>>>>>>>> [" +totAmtDet +"]" );
				BaseLogger.log("9", null, null, "totamtdet["+totAmtDet+"]");
				sql1 ="update porddet set tot_amt = ? where purc_order = ? and line_no = ?";
				pstmt1 =conn.prepareStatement(sql1);
				pstmt1.setDouble(1,totAmtDet);
				pstmt1.setString(2,tranId);
				pstmt1.setInt(3,lineNo);
				count = pstmt1.executeUpdate();
				pstmt1.close();
				pstmt1 = null;//[pstmt closed and nulled by Pavan R]
				
				
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			if (pstmt != null)
			{
		    	pstmt.close();
				pstmt=null;
			 }
			/*if(pstmt1 != null)
			{
				pstmt1.close();
				pstmt1 = null;
			}*/			
			
			sql = "select sum(tax_amt),sum(tot_amt),sum(quantity) from porddet where purc_order =? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranId);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				taxAmtHdr = rs.getDouble(1);
				totAmtHdr = rs.getDouble(2);
				quantity = 	rs.getDouble(3);
			}
			
			//System.out.println("taxAMthdr>>>>>>>>>>>>>>>>>>> " + taxAmtHdr);
			//System.out.println("TotalAmtheader!!!!!!!!!!!!!! " + totAmtHdr );
			//System.out.println("Quantity@@@@@@@@@@@@@@@@@@@  " + quantity);
			BaseLogger.log("9", null, null, "taxAMthdr["+taxAmtHdr+"]totAmtHdr["+totAmtHdr+"]quantity["+quantity+"]");
			ordAmtHdr = totAmtHdr - taxAmtHdr;
			
			sql1 ="select frt_rate,frt_type ,frt_amt__fixed from porder where purc_order = ?";
			pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setString(1, tranId);
			rs1 = pstmt1.executeQuery();
			if(rs1.next())
			{
				frtRate = rs1.getDouble(1);
				frtType = checkNull(rs1.getString(2));
				frtAmtFixed = rs1.getDouble(3);
			}
			if (rs !=null)
			{
				rs.close();
				rs=null;
			}
			if (pstmt != null)
			{
		    	pstmt.close();
				pstmt=null;
			 }
			/*if(pstmt1 != null)
			{
				pstmt1.close();
				pstmt1 = null;
			}*/			
			if(frtType.equalsIgnoreCase("Q"))
			{
				frtAmtQty = frtRate * quantity;
			}
			else
			{
			frtAmtQty = 0;	
			}
			System.out.println("frtrate>>>>>>>>>>>>>>>>>>> " + frtRate);
			System.out.println("frtType!!!!!!!!!!!!!! " + frtType );
			System.out.println("frtAmtFixed@@@@@@@@@@@@@@@@@@@  " + frtAmtFixed);
			System.out.println("frtAmtQty@@@@@@@@@@@@@@@@@@@  " + frtAmtQty);
			
			frtAmt = frtAmtFixed + frtAmtQty;
			System.out.println("frtAmtQty@@@@@@@@@@@@@@@@@@@  " + frtAmt);
				
				/**
				 * Following if(count>0) is commented
				 * on 02/MAR/16
				 * to resolve the issue
				 * in EDIT mode when all details are deleted
				 * */
//			if(count > 0)
//			{
			//Changed By PriyankaC on 17July2019.
		//	sql = "update porder set tax_amt = ?,tot_amt = ?,ord_amt = ? where purc_order = ? ";
			sql = "update porder set  tax_amt = ?,tot_amt = ?,ord_amt = ? ,frt_amt = ? where purc_order = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setDouble(1, taxAmtHdr);
			pstmt.setDouble(2, totAmtHdr);
			pstmt.setDouble(3, ordAmtHdr);
			//pstmt.setDouble(4, frtAmtQty);
			pstmt.setDouble(4, frtAmt);
			pstmt.setString(5, tranId);
			count = pstmt.executeUpdate();
			//System.out.println("post count---->>["+count+"]");
			if (pstmt != null)
			{
		    	pstmt.close();
				pstmt=null;
			 }
			BaseLogger.log("9", null, null, "post count---->>["+count+"]");
			/*if(count >0 ){
				conn.commit();
			}*/
//			}
			
			//Added by sarita to provide validation on post save as total amount updated on save on 11 JUN 2018 11 JUN 2018 [START]
			//System.out.println("Post Save Values of ordAmt ["+ordAmtHdr+"] \t totAmt ["+totAmtHdr+"]");
			BaseLogger.log("9", null, null, "Post Save Values of ordAmt ["+ordAmtHdr+"] \t totAmt ["+totAmtHdr+"]");
			
			sql = "select type , amt_type , rel_amt from pord_pay_term where purc_order = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,tranId);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				type = rs.getString("type");
				amtType = rs.getString("amt_type");
				//Changes by sarita to get relAmt as double on 20 AUG 18 [START]
				//relAmt = rs.getString("rel_amt");
				relAmt = rs.getDouble("rel_amt");
				//Changes by sarita to get relAmt as double on 20 AUG 18 [END]
				//System.out.println("type ["+type+"] \t amtType ["+amtType+"] \t relAmt ["+relAmt+"]");
				BaseLogger.log("9", null, null, "type ["+type+"] \t amtType ["+amtType+"] \t relAmt ["+relAmt+"]");
				advAmt = 0;
				
				//Commented and added by sarita on 20 AUG 18 to remove parseDouble for relAmt [START]
				/*if ("01".equalsIgnoreCase(amtType)) {
					advAmt = ordAmtHdr
							* (Double.parseDouble(relAmt) / 100);
				} else if ("02".equalsIgnoreCase(amtType)) {
					advAmt = totAmtHdr
							* (Double.parseDouble(relAmt) / 100);
				} else if ("03".equalsIgnoreCase(amtType)) {
					advAmt = Double.parseDouble(relAmt);
				}*/
				if ("01".equalsIgnoreCase(amtType)) {
					advAmt = ordAmtHdr
							* ((relAmt) / 100);
				} else if ("02".equalsIgnoreCase(amtType)) {
					advAmt = totAmtHdr
							* ((relAmt) / 100);
				} else if ("03".equalsIgnoreCase(amtType)) {
					advAmt = relAmt;
				}
				//Commented and added by sarita on 20 AUG 18 to remove parseDouble for relAmt [END]
				lcAmount = lcAmount + advAmt;
				
				
				//System.out.println("@@@@@@@ lcAmount[" + lcAmount		+ "] > totAmt[" + totAmtHdr + "]");
				BaseLogger.log("9", null, null, "@@@@@@@ lcAmount[" + lcAmount		+ "] > totAmt[" + totAmtHdr + "]");
			}
			if (rs != null)
			{
		    	rs.close();
				rs=null;
			}
			if (pstmt != null)
			{
		    	pstmt.close();
				pstmt=null;
			 }			
					
			if (lcAmount > totAmtHdr)
			{
				errorString = itmDBAccessEJB.getErrorString("","POADVMIS","","",conn);
				return errorString;
			}
			
			//Added by sarita to provide validation on post save as total amount updated on save on 11 JUN 2018 11 JUN 2018 [END]
			
				
			/*else{
				conn.rollback();
			}*/
		}
			catch(Exception e){
				//System.out.println("Exception : POrderPostSave -->["+e.getMessage()+"]");
				BaseLogger.log("0", null, null, "Exception : POrderPostSave -->["+e.getMessage()+"]");
				e.printStackTrace();
				try{
				conn.rollback();
				}catch(Exception e1){
					//System.out.println("Exception while rollbacking transaction....");
					BaseLogger.log("0", null, null, "Exception while rollbacking transaction....");
					e1.printStackTrace();
				}
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
					if (rs1 != null)
					{
							rs1.close();
							rs1 = null;
					}
					if (pstmt1 != null)
					{
							pstmt1.close();
							pstmt1 = null;
					}
				}
				catch(Exception e){}
			}
		return errorString;
	}
	
	private String checkNull(String input)
	{
		if (input == null || "null".equalsIgnoreCase(input) || "undefined".equalsIgnoreCase(input))
		{
			input= "";
		}
		return input.trim();
	}
		
}
	


