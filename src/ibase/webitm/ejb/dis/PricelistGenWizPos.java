package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ejb.Stateless;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;

@Stateless
public class PricelistGenWizPos extends ValidatorEJB implements PricelistGenWizPosLocal,PricelistGenWizPosRemote {
	E12GenericUtility genericUtility = new  E12GenericUtility();
	public String postSave(String xmlString,String tranId,String editFlag, String xtraParams,Connection conn) throws RemoteException,ITMException
	{

		System.out.println("------------ postSave method called-----------------PricelistGenWizPos : ");
		System.out.println("tranId111--->>["+tranId+"]");
		System.out.println("xml String--->>["+xmlString+"]");
		Document dom = null;
		String errString="";

		try
		{
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = parseString(xmlString);				
				errString = postSave(dom,tranId,xtraParams,conn);
			}
			System.out.println("errString["+errString+"]");
		}
		catch(Exception e)
		{
			System.out.println("Exception : PricelistGenWizPos.java : postSave : ==>\n"+e.getMessage());
			throw new ITMException(e);
		}		
		return errString;
	}	
	public String postSave(Document dom,String tranId,String xtraParams,Connection conn) throws ITMException
	{
		String retString = "";
		String nodeName = "", productCode = "", effFrom = "", validUpto = "", mstPricelist = "", priceListTar = "", priceListParent = "", calcMethod = "",grpCode="",
			   freeQty = "", freeOnQty = "", discPerc = "", listType = "", loginUser = "";
		ArrayList<HashMap<String, String>> inserPlist = new ArrayList<HashMap<String, String>>();
		boolean isError = false;
		DistCommon distCommon = new DistCommon();
		ITMDBAccessEJB ITMDBAccessEJB=new ITMDBAccessEJB();
		PreparedStatement pstmt = null, pstmt1 = null;
		ResultSet rs = null;
		
		int hdelcnt = 0, detdelcnt = 0;
		
		try
		{
			loginUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
			mstPricelist = distCommon.getDisparams("999999", "MRP", conn);
			NodeList detail1NodeList = dom.getElementsByTagName("Detail1");
			int detail1NodeListLen = detail1NodeList.getLength();
			
			for(int i=0; i<detail1NodeListLen; i++)
			{
				NodeList eachDetail1NodeList = detail1NodeList.item(0).getChildNodes();

				for(int j=0; j<eachDetail1NodeList.getLength();j++)
				{
					Node eachDetail1Element = eachDetail1NodeList.item(j);
					nodeName = eachDetail1Element.getNodeName();
					
					if(!"#text".equalsIgnoreCase(nodeName) && !"attribute".equals(nodeName))
					{
						if("product_code".equalsIgnoreCase(nodeName))
						{
							if( eachDetail1Element.getFirstChild() != null)
							{
								productCode = eachDetail1Element.getFirstChild().getNodeValue();
							}
						}
						else if("eff_from".equalsIgnoreCase(nodeName))
						{
							if( eachDetail1Element.getFirstChild() != null)
							{
								effFrom = eachDetail1Element.getFirstChild().getNodeValue();
							}
						}
						else if("valid_upto".equalsIgnoreCase(nodeName))
						{
							if( eachDetail1Element.getFirstChild() != null)
							{
								validUpto = eachDetail1Element.getFirstChild().getNodeValue();
							}
						}
						else if("grp_code".equalsIgnoreCase(nodeName))
						{
							if (eachDetail1Element.getFirstChild() != null)
							{
								grpCode = eachDetail1Element.getFirstChild().getNodeValue();
							}
						}
						
					}
				}
			}
			System.out.println("productCode["+productCode+"]effFrom["+effFrom+"]validUpto["+validUpto+"]");
			
			NodeList detail2NodeList = dom.getElementsByTagName("Detail2");
			int detail2NodeListLen = detail2NodeList.getLength();
			System.out.println("detail2NodeListLen["+detail2NodeListLen+"]");
			for(int i=0; i<detail2NodeListLen; i++)
			{
				Node eachDetail2 = detail2NodeList.item(i);
				
				NodeList eachDetail2NodeList = eachDetail2.getChildNodes();
				for(int j=0; j<eachDetail2NodeList.getLength();j++)
				{
					Node eachDetail2Element = eachDetail2NodeList.item(j);
					nodeName = eachDetail2Element.getNodeName();

					if(!"#text".equalsIgnoreCase(nodeName) && !"attribute".equals(nodeName))
					{
						if("offer_free_qty".equalsIgnoreCase(nodeName))
						{
							if (eachDetail2Element.getFirstChild() != null)
							{
								freeQty = eachDetail2Element.getFirstChild().getNodeValue();
							}
						}
						else if("offer_free_on".equalsIgnoreCase(nodeName))
						{
							if (eachDetail2Element.getFirstChild() != null)
							{
								freeOnQty = eachDetail2Element.getFirstChild().getNodeValue();
							}
						}
						else if("disc_perc".equalsIgnoreCase(nodeName))
						{
							if (eachDetail2Element.getFirstChild() != null)
							{
								discPerc = eachDetail2Element.getFirstChild().getNodeValue();
							}
						}
						else if("price_list".equalsIgnoreCase(nodeName))
						{
							if (eachDetail2Element.getFirstChild() != null)
							{
								priceListTar = eachDetail2Element.getFirstChild().getNodeValue();
							}
						}
						else if("price_list__parent".equalsIgnoreCase(nodeName))
						{
							if (eachDetail2Element.getFirstChild() != null)
							{
								priceListParent = eachDetail2Element.getFirstChild().getNodeValue();
							}
						}
						else if("list_type".equalsIgnoreCase(nodeName))
						{
							if (eachDetail2Element.getFirstChild() != null)
							{
								listType = eachDetail2Element.getFirstChild().getNodeValue();
							}
						}
						else if("calc_method".equalsIgnoreCase(nodeName))
						{
							if (eachDetail2Element.getFirstChild() != null)
							{
								calcMethod = eachDetail2Element.getFirstChild().getNodeValue();
							}
						}
						
					}
				}
				
				HashMap<String,String> eachPlist=new HashMap<String, String>();
				
				eachPlist.put("price_list_mrp", mstPricelist);
				eachPlist.put("price_list_tar", priceListTar);
				eachPlist.put("price_list_parent", priceListParent);
				eachPlist.put("calc_method", calcMethod);
				eachPlist.put("free_qty", freeQty);
				eachPlist.put("free_on_qty", freeOnQty);
				eachPlist.put("disc_perc", discPerc);
				eachPlist.put("list_type", listType);
				inserPlist.add(eachPlist);
			}
			
			System.out.println("final insert plist in pos save["+inserPlist+"]");
			
			PricelistGenEJB ins = new PricelistGenEJB();
			Object retObj = ins.generateAndInsertPriceList(productCode,grpCode, inserPlist, effFrom, validUpto, xtraParams, false, conn);
			if(retObj instanceof String)
			{
				System.out.println("retObj["+(String) retObj+"]");
				retString = (String) retObj;
				
				if("SUCCESS".equalsIgnoreCase(retString))
				{
					String sql = "SELECT TRAN_ID FROM PRICING_MANAGE WHERE CHG_USER = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, loginUser);
					
					rs = pstmt.executeQuery();
					
					if(rs.next())
					{
						String tranIdDel = rs.getString("TRAN_ID");
						String sqldel = "DELETE FROM PRICING_MANAGE WHERE TRAN_ID = ? ";
						pstmt1=conn.prepareStatement(sqldel);
						pstmt1.setString(1,tranIdDel);
						hdelcnt=pstmt1.executeUpdate();
						pstmt1.close();
						pstmt1=null;
						
						String sqldeldet = "DELETE FROM PRICING_MANAGE_LIST WHERE TRAN_ID = ? ";
						pstmt1=conn.prepareStatement(sqldeldet);
						pstmt1.setString(1,tranIdDel);
						detdelcnt=pstmt1.executeUpdate();
						pstmt1.close();
						pstmt1=null;
					}
					System.out.println("hdelcnt["+hdelcnt+"]detdelcnt["+detdelcnt+"]");
					retString = "";
				}
				else
				{
					retString = ITMDBAccessEJB.getErrorString("", "PLISTWIZER", "", "", conn);
					
				}
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Exception : PricelistGenWizPos -->["+e.getMessage()+"]");
			throw new ITMException(e);
		}
		
		return retString;
	}
}