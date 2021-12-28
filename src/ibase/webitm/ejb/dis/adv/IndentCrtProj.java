/* 
		Developed by : Niraja
		Company : Base Information Management Pvt. Ltd
		Version : 1.0
		Date : 26/10/2005
*/
package ibase.webitm.ejb.dis.adv;

import java.rmi.RemoteException;
import java.sql.*;

import javax.ejb.*;

import ibase.webitm.ejb.*;
import ibase.system.config.*;
import ibase.webitm.utility.TransIDGenerator;
import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class IndentCrtProj extends ActionHandlerEJB implements IndentCrtProjLocal,IndentCrtProjRemote
{
	/*public void ejbCreate() throws RemoteException, CreateException 
	{
	}
	public void ejbRemove()
	{
	}
	public void ejbActivate() 
	{
	}
	public void ejbPassivate() 
	{
	}*/
	public String actionHandler(String tranID, String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		System.out.println("xtraParams:"+xtraParams+":");
		String  retString = null;
		try
		{
			retString = actionCreateProj(tranID,xtraParams);
		}
		catch(Exception e)
		{
			System.out.println("Exception :IndentCreateProj :actionHandler:" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		System.out.println("returning from IndentCreateProj actionHandler"+retString);
		return retString;
	}
	private String actionCreateProj(String indNo,  String xtraParams)throws ITMException
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pstmt =null;
		ResultSet rs = null;
		String sql = "";
		String updSql = "";
		String insSql = "";
		String errCode="";
		String errString="";
		String siteCode	="";
		String projCode	="";
		String itemCode="";
		String itemDescr ="";
		String keyString="";
		String userId="";
		String projNo="";
		int rows=0;
		ITMDBAccessEJB itmDBAccess = new ITMDBAccessEJB();
		ibase.utility.E12GenericUtility genericUtility= new  ibase.utility.E12GenericUtility();
		ConnDriver connDriver = new ConnDriver();
		try
		{	
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			stmt = conn.createStatement();
			sql="SELECT SITE_CODE__DLV,PROJ_CODE FROM INDENT WHERE IND_NO = '"+indNo+"'";
			System.out.println("IndentCreateProj:actionCreateProj:INDENT:sql:"+sql);
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				siteCode=rs.getString("SITE_CODE__DLV");
				projCode=rs.getString("PROJ_CODE");
			}
			System.out.println("siteCode:"+siteCode+"\t:projCode:"+projCode+":");
			sql="SELECT ITEM.ITEM_CODE,DESCR FROM ITEM,INDENT "+
				" WHERE INDENT.ITEM_CODE = ITEM.ITEM_CODE "+
				" AND INDENT.IND_NO ='"+indNo+"'";
			System.out.println("IndentCreateProj:actionCreateProj:ITEM:sql:"+sql);
			rs = stmt.executeQuery(sql);
			if(rs.next())
			{
				itemCode=rs.getString("ITEM_CODE");
				itemDescr=rs.getString("DESCR");
			}
			System.out.println("itemCode:"+itemCode+"\t:itemDescr:"+itemDescr+":");
			if(itemCode != null && itemCode.trim().length() > 0)
			{
				if(projCode != null && projCode.trim().length() > 0)
				{
					sql="SELECT COUNT(*)CNT FROM PROJECT WHERE PROJ_CODE='"+projCode+"'";
					System.out.println("IndentCreateProj:actionCreateProj:PROJECT:sql:"+sql);
					rs = stmt.executeQuery(sql);
					if(rs.next())
					{
						if(rs.getInt(1) > 0)
						{
							errCode="VTDUPPJCD";
						}
					}
				}
    			System.out.println("*********  errCode 1******:"+errCode+":");
				if(errCode.equals(""))
				{
					sql="SELECT KEY_STRING FROM TRANSETUP WHERE TRAN_WINDOW = 'w_project' ";
				    System.out.println("IndentCreateProj:actionCreateProj:TRANSETUP:sql:"+sql);
					rs = stmt.executeQuery(sql);
					if(rs.next())
					{
						keyString =rs.getString("KEY_STRING");
					}
					else
					{
						sql="SELECT KEY_STRING FROM TRANSETUP WHERE TRAN_WINDOW = 'GENERAL'";
						System.out.println("IndentCreateProj:actionCreateProj:else TRANSETUP:sql:"+sql);
						rs = stmt.executeQuery(sql);
						if(rs.next())
						{
							keyString =rs.getString("KEY_STRING");
						}						
					}
					System.out.println("IndentCreateProj:actionCreateProj:keyString:"+keyString);
					userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"user_id");
					String XMLString = "<?xml version=\"1.0\"?>\r\n<Root>\r\n<header>"+
								   "\r\n</header><Detail1><desp_date>"+new java.sql.Date(System.currentTimeMillis())+"</desp_date><site_code>"+siteCode+"</site_code>"+
								   " </Detail1></Root>";
					TransIDGenerator tg = new TransIDGenerator(XMLString, userId, "");

					projNo = tg.generateTranSeqID("M-PROJ","proj_code",keyString,conn);
					System.out.println("IndentCreateProj:actionCreateProj:projNo:"+projNo);
				}
				System.out.println("*********  errCode 2******:"+errCode+":");
			    if(errCode.equals(""))
				{
					insSql ="INSERT INTO PROJECT(PROJ_CODE,ITEM_CODE,DESCR,SITE_CODE,IND_NO,START_DATE,PROJ_STATUS,CHG_DATE,CHG_USER,CHG_TERM) "+ 
						" VALUES(?,?,?,?,?,?,?,?,?,?)";
					System.out.println(new java.sql.Date(System.currentTimeMillis()));
					System.out.println("IndentCreateProj:actionCreateProj:insSql:"+insSql);
					pstmt = conn.prepareStatement(insSql);
					pstmt.setString(1,projNo);
					pstmt.setString(2,itemCode);
					pstmt.setString(3,itemDescr);
					pstmt.setString(4,siteCode);
					pstmt.setString(5,indNo.trim());
					pstmt.setDate(6,new java.sql.Date(System.currentTimeMillis()));
					pstmt.setString(7,"A");
					pstmt.setDate(8,new java.sql.Date(System.currentTimeMillis()));
					pstmt.setString(9,userId);
					pstmt.setString(10,"Base");	 
					rows = pstmt.executeUpdate();
					if(rows == 0)
					{
						updSql="UPDATE INDENT SET PROJ_CODE ='"+projNo+"'"+" WHERE IND_NO = '"+indNo+"'";
						System.out.println("IndentCreateProj:actionCreateProj:updSql:"+updSql);
						rows = stmt.executeUpdate(updSql) ;
						System.out.println("Rows Updated :Table:"+rows);
					}
				}			    
				System.out.println("*********  errCode 3******:"+errCode+":");
				if(errCode.equals(""))
				{
					conn.commit();
					errCode = "VTSUCC";
					System.out.println("\n <==== INDENT Updated Successfully ====>");
				}
			}
			if(errCode != null  && errCode.trim().length() > 0)
			{
				System.out.println("IndentCreateProj:errCode:"+errCode);
				errString = itmDBAccess.getErrorString("",errCode,"","",conn);
				System.out.println("errString:"+errString+":");
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :IndentCreateProj :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Connection Closed......");
				conn.close();
			}catch(Exception e){}
		}
		return errString;
	}
}

