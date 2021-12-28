
package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.*;
import org.w3c.dom.*;

import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.TransIDGenerator;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.dis.DistCommon;

import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class ConsIssuePostSave extends ValidatorEJB implements ConsIssuePostSaveLocal, ConsIssuePostSaveRemote
{
	static
	{
		System.out.println(" ConsIssuePostSave called ");
	}
	public String postSave()throws RemoteException,ITMException
	{
		return "";
	}
	public String postSave(String domString, String tranId,String editFlag, String xtraParams, Connection conn ) throws RemoteException,ITMException
	{

		NodeList detail2List =    null;
		String errString = "";
		Document dom = null;
		PreparedStatement pstmt = null,pstmt1 = null;		
		ResultSet rs = null;		
		String sql = "";
		int updCissDet=0;
		System.out.println(" Tran id = "+tranId);
        String  chgTerm = "",chgUser  ="",lineNo="";
		double amt=0d,taxamt=0d,netamt=0d;
		//GenericUtility genericUtility = null;
		E12GenericUtility genericUtility= null;
		double noArt=0;
		try
		{
			 genericUtility= new  E12GenericUtility();			
			//genericUtility = GenericUtility.getInstance();
			dom = genericUtility.parseString(domString);
			chgTerm =  genericUtility.getValueFromXTRA_PARAMS( xtraParams, "CHG_TERM" );
			chgUser =  genericUtility.getValueFromXTRA_PARAMS( xtraParams, "CHG_USER" );
			detail2List = dom.getElementsByTagName("Detail2");	
			tranId = genericUtility.getColumnValueFromNode("cons_issue", dom.getElementsByTagName("Detail1").item(0));			
		
			System.out.println("@@@@@ postSave called :["+tranId+"]");
			System.out.println("@@@@@@@@ detail2List [[[[[[[["+detail2List.getLength()+"]]]]]]]]]]]]]");
		
				sql =  " SELECT LINE_NO FROM CONSUME_ISS_DET WHERE CONS_ISSUE = ? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, tranId);
				rs=pstmt.executeQuery();
				while(rs.next())
				{
					System.out.println("lineNo::"+rs.getInt(1));
					sql = " UPDATE CONSUME_ISS_DET SET AMOUNT = (CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END) * (CASE WHEN RATE IS NULL THEN 0 ELSE RATE END) ,"+
							" NET_AMT = ((CASE WHEN QUANTITY IS NULL THEN 0 ELSE QUANTITY END)*(CASE WHEN RATE IS NULL THEN 0 ELSE RATE END))+ (CASE WHEN TAX_AMT IS NULL THEN 0 ELSE TAX_AMT END) "+
							" WHERE CONS_ISSUE= ? AND LINE_NO= ? ";
					pstmt1=conn.prepareStatement(sql);
					pstmt1.setString(1, tranId);
					pstmt1.setInt(2, rs.getInt(1));
					updCissDet=updCissDet+pstmt1.executeUpdate();
					pstmt1.close();
					pstmt1=null;
		
				  }
				if(rs !=null)
					rs.close();
				rs = null;
				if(pstmt !=null)
					pstmt.close();
				pstmt = null;
				System.out.println("Total updated record in detail  :: "+updCissDet);

				pstmt=conn.prepareStatement("SELECT SUM(TAX_AMT),SUM(AMOUNT),SUM(NET_AMT),SUM(CASE WHEN NO_ART IS NULL THEN 0 ELSE NO_ART END) FROM CONSUME_ISS_DET WHERE CONS_ISSUE=?");
				pstmt.setString(1, tranId);
				rs=pstmt.executeQuery();
				if(rs.next())
				{
					taxamt = rs.getDouble(1);System.out.println("taxamt:;"+taxamt);
					amt = rs.getDouble(2);System.out.println("amt:;"+amt);
					netamt = rs.getDouble(3);System.out.println("netamt:;"+netamt);
					noArt= rs.getDouble(4);System.out.println("noArt:;"+netamt);
				}
				if(rs !=null)
					rs.close();
				rs = null;
				if(pstmt !=null)
					pstmt.close();
				pstmt = null;
				
				sql= " UPDATE CONSUME_ISS SET TAX_AMT=?,AMOUNT=?,NET_AMT=?+?,NET_AMT__BC=(?+?)*EXCH_RATE,NO_ART=? WHERE CONS_ISSUE=? ";
				pstmt=conn.prepareStatement(sql);
				pstmt.setDouble(1, taxamt);
				pstmt.setDouble(2, amt);
				pstmt.setDouble(3, taxamt);
				pstmt.setDouble(4, amt);
				pstmt.setDouble(5, taxamt);
				pstmt.setDouble(6, amt);
				pstmt.setDouble(7, noArt);
				pstmt.setString(8, tranId);
				int updCisshdr=pstmt.executeUpdate();
				pstmt.close();
				pstmt=null;
				System.out.println(" updated record in header  :: "+updCisshdr);
				
		}
		catch(Exception e)
		{
				System.out.println("Exception.. "+e.getMessage());
				e.printStackTrace();	
				errString=e.getMessage();
				throw new ITMException(e);
		}
		finally
		{
			try
			{	
				if( errString != null && errString.trim().length() >  0 )
				{
					conn.rollback();
					System.out.println("Transaction rollback... ");
				}
				if(pstmt != null)
				{
					pstmt.close();
					pstmt = null;
				}
				if(pstmt1 != null)
				{
					pstmt1.close();
					pstmt1 = null;
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
		return errString;
	}
}

