package ibase.webitm.ejb.dis;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;
@javax.ejb.Stateless
public class SRLContainerPrs extends ValidatorEJB implements SRLContainerPrsLocal, SRLContainerPrsRemote
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	ITMDBAccessEJB itmDBAccessEJB = new ITMDBAccessEJB();

	public String preSave() throws RemoteException,ITMException
	{
		return "";
	}
	public String preSave(String xmlString, String editFlag, String xtraParams, Connection conn)throws RemoteException,ITMException
	{
		String errString = "";
		System.out.println("SRLContainerPrs EJB called");
		Document dom = null;
		try
		{
			System.out.println("xmlString in SRLContainerPrs : preSaveRec \n"+xmlString);			
			if (xmlString != null && xmlString.trim().length() > 0)
			{
				dom = genericUtility.parseString(xmlString);
				errString = executepreSave( dom, editFlag, xtraParams, conn );
				System.out.println("ReturnString in SRLContainerPrs ["+errString+"]");
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception :SRLContainerPrs :" + e.getMessage() + ":");
			e.printStackTrace();
			throw new ITMException(e);
		}
		return errString;
	}
	
	public String executepreSave(Document dom, String editFlag, String xtraParams, Connection conn ) throws RemoteException, ITMException, SQLException
	{
		System.out.println("executepreSave method called");
		String errString = "";
		NodeList parentNodeList = null;
		Node parentNode = null , headerNode = null;
		String updateFlag="";
		int parentNodeListLength=0;
		int detlCnt = 0 ;
		double noArt = 0;
		String  numberOfArticles ="" , itemCode = "" , inventoryType = "" , DetItemCode = "";
		List<String> DetItemCodeList = new ArrayList<String>();
		boolean isLocCon = false;
		boolean isError = false;
		try
		{			
			if (conn == null ||  conn.isClosed())
			{
				conn = getConnection();
				System.out.println("connection is not null :"+conn);
				isLocCon = true;
			}
			System.out.println("connection is :"+conn);
	     				
			headerNode = dom.getElementsByTagName("Detail1").item(0);
			numberOfArticles = checkNull(genericUtility.getColumnValueFromNode("no_art", headerNode));
			itemCode  = checkNull(genericUtility.getColumnValueFromNode("item_code", headerNode));
			inventoryType = checkNull(genericUtility.getColumnValueFromNode("inventory_type", headerNode));
			//changes by sarita on 17MAR2018
			noArt = Double.valueOf(numberOfArticles);
			System.out.println("Value of Number of Articles from Header ["+noArt+"] \n  [Inventory Type ["+inventoryType+"]]");
			
			parentNodeList = dom.getElementsByTagName("Detail2");			
			parentNodeListLength = parentNodeList.getLength();
			
			System.out.println("[parentNodeList ["+parentNodeList+"]] \n [parentNodeListLength ["+parentNodeListLength+"]]");
			
			if(parentNodeListLength == 0)
			{
				System.out.println("Detail1 not found");
				
				errString = itmDBAccessEJB.getErrorString("", "VTBLNKDTL", "","",conn);//itmDBAccessEJB.getErrorString("","VTBLNKDTL","","",conn);
				return errString;
			}
			else
			{
				detlCnt = getNumOfNonDelDetail(dom,2);
				System.out.println("Detail Count List Is =================> ["+detlCnt+"]  &&&  Number of Articles from Header ["+numberOfArticles+"]");
				
				if(detlCnt != noArt)
				{
					//String errCode = "VTINVDTLRD";
					errString = itmDBAccessEJB.getErrorString("", "VTINVDTLRD", "","",conn);
					return errString;
					/*errString = itmDBAccessEJB.getErrorString("", "VTINVDTLRD", "");//itmDBAccessEJB.getErrorString("","VTINVDTLRD","","",conn);
					return errString;*/
				}
			}
			
		}
		catch(Exception e)
		{
			System.out.println("The Exception occurs in SRLContainerPrs :"+e);
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try
			{
				System.out.println("Inside finally SRLContainerPrs isError["+isError+"] connStatus["+isLocCon+"]");
				
				if(isLocCon)
				{
					if(isError)
					{
						System.out.println("Inside rollbacking....");
						conn.rollback();
					}
					else
					{
						System.out.println("Inside committing....");
						conn.commit();
					}
					if (conn != null )
					{
						conn.close();
					}
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return errString;
	}
	private String checkNull( String input )	
	{
		if ( input == null )
		{
			input = "";
		}
		return input;
	}		
	
	public int getNumOfNonDelDetail(Document dom2,int detailNo) throws ITMException
	{
		Node childNode = null;
		NodeList updateList;
		String childNodeName = "";
		String updateFlag="";
		int cntr=0;
		System.out.println("Inside getXmlDocument method of SRLContainerPrs class");
		try
		{
			System.out.println("detailString value is =="+genericUtility.serializeDom(dom2));
			NodeList detailNoteList = dom2.getElementsByTagName("Detail"+detailNo);
			for(int cnt = 0;cnt<detailNoteList.getLength();cnt++)
			{
				Node pNode=detailNoteList.item(cnt);		
				childNodeName = pNode.getNodeName();
				updateFlag = getAttributeVal(pNode,"updateFlag");
				System.out.println("Before updateFlag counter is ===["+cntr+"]"+"\t"+"updateFlag [" + updateFlag + "]");
				
				if(!("D".equalsIgnoreCase(updateFlag)))
				{
					cntr++;
				}
				System.out.println("After updateFlag counter is ===["+cntr+"]"+"\t"+"updateFlag [" + updateFlag + "]");
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception : : getNumOfNonDelDetail :"+e); 
			e.printStackTrace();
			throw new ITMException(e);
		}
		return cntr;
	}
	public String getAttributeVal(Node dom, String attribName )throws ITMException
	{
		System.out.println("Inside getAttributeVal method is !!!!!!!!!!!!!!");
		String AttribValue = null;
		try
		{
			NodeList detailList = dom.getChildNodes();
			System.out.println("Details NodeList is ====["+detailList+"]" + "Length is ======"+detailList.getLength());
			int detListLength = detailList.getLength();
			for(int ctr = 0; ctr < detListLength; ctr++)
			{
				Node curDetail = detailList.item(ctr);
				if(curDetail.getNodeName().equals("attribute")) 
				{
					AttribValue = curDetail.getAttributes().getNamedItem(attribName).getNodeValue();
					System.out.println("Attribute Value is =====["+AttribValue+"]");
					break;
				}
				else
				{
					continue;
				}
			}		
		}
		catch (Exception e)
		{
			System.out.println("Exception : : searchNode :"+e); 
			throw new ITMException(e);
		}
		return AttribValue;
	}	
}
