package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ValidatorEJB;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ITMDBAccessEJB;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.ejb.Stateless; // added for ejb3

@Stateless // added for ejb3
public class DissOrderDmgItmChg extends ValidatorEJB implements DissOrderDmgItmChgLocal,DissOrderDmgItmChgRemote //SessionBean
{
	/*public void ejbCreate() throws RemoteException, CreateException
	{
		//return "";
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
	public String wfValData() throws RemoteException,ITMException
	{
		return "";
	}

	public String itemChanged() throws RemoteException,ITMException
	{
		System.out.println("In item change ,.............");
		return "";
	}
	public String itemChanged(String xmlString, String xmlString1,String xmlString2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		Document dom = null;
		Document dom1 = null;
		Document dom2 = null;
		String valueXmlString = "";
		try
		{
			dom = parseString(xmlString);
			dom1 = parseString(xmlString1);
			if (xmlString2.trim().length() > 0 )
			{
				dom2 = parseString(xmlString2);
			}
			valueXmlString = itemChanged(dom,dom1,dom2,objContext,currentColumn,editFlag,xtraParams);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
			
		}
        return valueXmlString;
	}
	public String itemChanged(Document dom, Document dom1,Document dom2, String objContext, String currentColumn, String editFlag, String xtraParams) throws RemoteException,ITMException
	{
		StringBuffer valueXmlString = new StringBuffer();
		SimpleDateFormat sdf=null;
		NodeList parentNodeList = null;
		NodeList childNodeList = null;
		Node parentNode = null;
		Node childNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null ;
		int ctr = 0;
		String childNodeName = null;
		String columnValue = null,confirmed="";
		String Col_name = "";
		int currentFormNo = 0 ,cnt = 0;
		String  tranId="",deptCode = "", roleCodePrfmer = "",    siteCode = "", empCode = "";
		String sql = "",descr = "",empFName = "", empMName = "", empLName = "",roleCodeAprv="";
		ConnDriver connDriver = new ConnDriver();
		Timestamp tranDate =null;		
		String disptNo ="",currDate="";	
		Timestamp disptDate  =null;  	
		String InvcNo =""; 		 
		Timestamp InvcDate = null;  
		String exciseRef =""; 	
		Timestamp exciseRefDate =null; 		
		Timestamp exciseDateNew =null;
		String dateFlag ="";
		String siteCodeCurr ="",chgUser="",chgTerm="" ;		
		String addr1="" ,addr2="",city="",pin="",stateCode="";// genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
		String finEntity ="",currCode="",varValue="",locationDescr="",tranType="",siteCodeShip="",autoReceiptProtect="",autoReceipt="",avaliableYn="",avaliableYnProtect="",locCodeGitbf="",locCodeCons="",priceListClg="",dlvTerm="",itemCode="",priceListAcct="",siteCodeDlv="";
		String locConsProtected ="",sundryProtected="",chgSite="",locGitProtect="",locGitbfProtect="",locCodeGit="",priceList="",policyNo="",shDescr="",addr3="";
		double exchangeRateDec=0;
		String billSiteCode="";
		//java.sql.Timestamp toDate = new java.sql.Timestamp(System.currentTimeMillis());
		try
		{
		   // GenericUtility genericUtility = GenericUtility.getInstance();
			E12GenericUtility genericUtility= new  E12GenericUtility();
			DistCommon distCommon = new DistCommon();
			sdf = new SimpleDateFormat(genericUtility.getApplDateFormat());
			Timestamp currDateTs = new Timestamp(System.currentTimeMillis());
			siteCodeCurr = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"loginSiteCode");
			currDate = sdf.format(currDateTs).toString();
			//Changes and Commented By Bhushan on 09-06-2016 :START
			//conn = connDriver.getConnectDB("DriverITM");
			conn = getConnection();
			//Changes and Commented By Bhushan on 09-06-2016 :END 
			conn.setAutoCommit(false);
			connDriver=null;
			if(objContext != null && objContext.trim().length()>0)
			{
				currentFormNo = Integer.parseInt(objContext);
			}
			chgUser = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"userId");
			chgTerm = genericUtility.getValueFromXTRA_PARAMS(xtraParams,"termId");
			
			valueXmlString = new StringBuffer("<?xml version=\"1.0\"?><Root><header><editFlag>");
			valueXmlString.append(editFlag).append("</editFlag></header>");			
			switch(currentFormNo)
			{
			    case 1 :
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					ctr = 0;
					valueXmlString.append("<Detail1>");
					int childNodeListLength = childNodeList.getLength();
					do
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equals(currentColumn))
						{
							if (childNode.getFirstChild() != null)
							{
								columnValue = childNode.getFirstChild().getNodeValue().trim();
							}
						}
						ctr++;
					}while(ctr < childNodeListLength  && !childNodeName.equals(currentColumn));						

					if(currentColumn.trim().equalsIgnoreCase( "itm_default" ) )
					{
						
						// tran_id call by default 
						tranType ="IT";// genericUtility.getColumnValue("tran_type",dom);
						valueXmlString.append("<tran_type protect =\"0\">").append("<![CDATA["+tranType+"]]>").append("</tran_type>");
						valueXmlString.append("<order_type protect =\"0\">").append("<![CDATA[F]]>").append("</order_type>");
						
						valueXmlString.append("<trans_mode protect =\"0\">").append("<![CDATA[R]]>").append("</trans_mode>");
						siteCodeShip = genericUtility.getColumnValue("site_code__ship",dom);
						siteCodeShip = siteCodeShip == null ? "" : siteCodeShip.trim();
						siteCodeDlv = genericUtility.getColumnValue("site_code__dlv",dom);
						siteCodeDlv = siteCodeDlv !=null ?siteCodeDlv.trim() : "";
						if("CT".equalsIgnoreCase(tranType))
						{
							valueXmlString.append("<loc protect =\"1\">").append("<![CDATA[Consg Loc. :]]>").append("</loc>");

						}
						if("LT".equalsIgnoreCase(tranType))
						{
							valueXmlString.append("<loc protect =\"1\">").append("<![CDATA[Loan Loc. :]]>").append("</loc>");

						}
						else
						{
							valueXmlString.append("<loc protect =\"1\">").append("<![CDATA[Consg Loc. :]]>").append("</loc>");
						}
						sql="select loc_cons_protect,sundry_protect,chg_site,auto_receipt,auto_receipt_protect, "
							+"	loc_git_protect,loc_gitbf_protect,avaliable_yn,avaliable_yn_protect, "
							+"	loc_code__git,loc_code__gitbf,loc_code__cons,price_list__clg,dlv_term, "
							+"	price_list	"						 
							+"	from  distorder_type where tran_type = ? " ;
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,tranType.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							locConsProtected = rs.getString("loc_cons_protect")==null ? "":rs.getString("loc_cons_protect");
							sundryProtected = rs.getString("sundry_protect")==null ? "":rs.getString("sundry_protect");
							chgSite = rs.getString("chg_site")==null ? "":rs.getString("chg_site");
							autoReceipt = rs.getString("auto_receipt")==null ? "":rs.getString("auto_receipt");
							autoReceiptProtect = rs.getString("auto_receipt_protect")==null ? "":rs.getString("auto_receipt_protect");
							locGitProtect = rs.getString("loc_git_protect")==null ? "":rs.getString("loc_git_protect");
							locGitbfProtect = rs.getString("loc_gitbf_protect")==null ? "":rs.getString("loc_gitbf_protect");
							avaliableYn = rs.getString("avaliable_yn")==null ? "":rs.getString("avaliable_yn");
							avaliableYnProtect = rs.getString("sundry_protect")==null ? "":rs.getString("sundry_protect");
							locCodeGit = rs.getString("loc_code__git")==null ? "":rs.getString("loc_code__git");
							locCodeGitbf = rs.getString("loc_code__gitbf")==null ? "":rs.getString("loc_code__gitbf");
							locCodeCons = rs.getString("loc_code__cons")==null ? "":rs.getString("loc_code__cons");
							priceListClg = rs.getString("price_list__clg")==null ? "":rs.getString("price_list__clg");
							dlvTerm = rs.getString("dlv_term")==null ? "":rs.getString("dlv_term");
							priceList = rs.getString("price_list")==null ? "":rs.getString("price_list");							
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(dlvTerm !=null && dlvTerm.trim().length()>0)
						{
							sql="select policy_no from delivery_term where dlv_term = ?  " ;
							pstmt = conn.prepareStatement(sql);	
							pstmt.setString(1,dlvTerm.trim());
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								policyNo = rs.getString("policy_no")==null ? "":rs.getString("policy_no");						
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(policyNo!=null && policyNo.trim().length()>0)
							{
								valueXmlString.append("<policy_no protect =\"1\">").append("<![CDATA["+policyNo+"]]>").append("</policy_no>");
							}
						}
						priceListAcct = distCommon.setPlistTaxClassEnv(siteCodeShip,siteCodeDlv,itemCode,tranType,"","PRICE_LIST",conn);
						valueXmlString.append("<price_list protect =\"0\">").append("<![CDATA["+priceListAcct+"]]>").append("</price_list>");
						priceListAcct = distCommon.setPlistTaxClassEnv(siteCodeShip,siteCodeDlv,itemCode,tranType,"","PRICE_LIST__CLG",conn);
						valueXmlString.append("<price_list__clg protect =\"0\">").append("<![CDATA["+priceListAcct+"]]>").append("</price_list__clg>");	
						if("Y".equalsIgnoreCase(locConsProtected))
						{
							valueXmlString.append("<loc_code__cons protect =\"1\">").append("<![CDATA[]]>").append("</loc_code__cons>");
						}
						else
						{
							valueXmlString.append("<loc_code__cons protect =\"0\">").append("<![CDATA[]]>").append("</loc_code__cons>");
						}
						if("Y".equalsIgnoreCase(sundryProtected))
						{
							valueXmlString.append("<sundry_type protect =\"1\">").append("<![CDATA[]]>").append("</sundry_type>");
							valueXmlString.append("<sundry_code protect =\"1\">").append("<![CDATA[]]>").append("</sundry_code>");
						}
						else
						{
							valueXmlString.append("<sundry_type protect =\"0\">").append("<![CDATA[]]>").append("</sundry_type>");
							valueXmlString.append("<sundry_code protect =\"0\">").append("<![CDATA[]]>").append("</sundry_code>");
						}

						if("Y".equalsIgnoreCase(chgSite))
						{
							valueXmlString.append("<site_code__ship protect =\"0\">").append("<![CDATA["+siteCodeShip+"]]>").append("</site_code__ship>");
							valueXmlString.append("<site_code__dlv protect =\"0\">").append("<![CDATA["+siteCodeDlv+"]]>").append("</site_code__dlv>");
						}
						else
						{	
							valueXmlString.append("<site_code__ship protect =\"0\">").append("<![CDATA["+siteCodeShip+"]]>").append("</site_code__ship>");
							valueXmlString.append("<site_code__dlv protect =\"0\">").append("<![CDATA["+siteCodeDlv+"]]>").append("</site_code__dlv>");
						}

						valueXmlString.append("<auto_receipt protect =\"1\">").append("<![CDATA["+autoReceipt+"]]>").append("</auto_receipt>");
					
						if("Y".equalsIgnoreCase(autoReceiptProtect))
						{
							valueXmlString.append("<auto_receipt protect =\"1\">").append("<![CDATA["+autoReceipt+"]]>").append("</auto_receipt>");
						}
						else
						{
							valueXmlString.append("<auto_receipt protect =\"0\">").append("<![CDATA["+autoReceipt+"]]>").append("</auto_receipt>");
						}
						if("Y".equalsIgnoreCase(avaliableYnProtect))
						{
							valueXmlString.append("<avaliable_yn protect =\"1\">").append("<![CDATA["+avaliableYn+"]]>").append("</avaliable_yn>");
						}
						else
						{valueXmlString.append("<avaliable_yn protect =\"0\">").append("<![CDATA["+avaliableYn+"]]>").append("</avaliable_yn>");
						}
						if("Y".equalsIgnoreCase(locGitProtect))
						{
							valueXmlString.append("<loc_code__git protect =\"1\">").append("<![CDATA["+locCodeGit+"]]>").append("</loc_code__git>");
						}
						else
						{
							valueXmlString.append("<loc_code__git protect =\"0\">").append("<![CDATA["+locCodeGit+"]]>").append("</loc_code__git>");
						}
						if("Y".equalsIgnoreCase(locCodeGitbf))
						{
							valueXmlString.append("<loc_code__gitbf protect =\"1\">").append("<![CDATA["+locCodeGitbf+"]]>").append("</loc_code__gitbf>");
						}
						else
						{
							valueXmlString.append("<loc_code__gitbf protect =\"0\">").append("<![CDATA["+locCodeGitbf+"]]>").append("</loc_code__gitbf>");
						}
						valueXmlString.append("<loc_code__git protect =\"0\">").append("<![CDATA["+locCodeGit+"]]>").append("</loc_code__git>");
						sql="select sh_descr  from location where loc_code = ? " ;
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,locCodeGit.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							shDescr = rs.getString("sh_descr")==null ? "":rs.getString("sh_descr");						
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;							
						valueXmlString.append("<location_descr protect =\"1\">").append("<![CDATA["+shDescr+"]]>").append("</location_descr>");
						valueXmlString.append("<loc_code__gitbf protect =\"0\">").append("<![CDATA["+locCodeGitbf+"]]>").append("</loc_code__gitbf>");
						valueXmlString.append("<loc_code__cons protect =\"0\">").append("<![CDATA["+locCodeCons+"]]>").append("</loc_code__cons>");
						valueXmlString.append("<avaliable_yn protect =\"0\">").append("<![CDATA["+avaliableYn+"]]>").append("</avaliable_yn>");
						valueXmlString.append("<sundry_type>").append("<![CDATA[C]]>").append("</sundry_type>");
						// end of code tran_id dby default 

						sql= "select add1,add2,city,pin,state_code,descr from site where site_code= ? ";
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,siteCodeCurr.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString("descr")==null ? "":rs.getString("descr");
							addr1 = rs.getString("add1")==null ? "":rs.getString("add1");
							addr2 = rs.getString("add2")==null ? "":rs.getString("add2");
							city = rs.getString("city")==null ? "":rs.getString("city");
							pin = rs.getString("pin")==null ? "":rs.getString("pin");
							stateCode = rs.getString("state_code")==null ? "":rs.getString("state_code");						 
						}					
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;						
						sql= "select fin_entity  from site where site_code = ? " ;
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,siteCodeCurr.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							finEntity = rs.getString("fin_entity")==null ? "":rs.getString("fin_entity");						
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql= "select curr_code  from finent where  fin_entity = ?" ;
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,finEntity.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							currCode = rs.getString("curr_code")==null ? "":rs.getString("curr_code");						
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql= "select curr_code  from finent where  fin_entity = ? " ;
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,finEntity.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							currCode = rs.getString("curr_code")==null ? "":rs.getString("curr_code");						
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql="select var_value  from disparm "
							+"	where prd_code = '999999' and "
							+"	var_name = 'TRANSIT_LOC' " ;
						pstmt = conn.prepareStatement(sql);						
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							varValue = rs.getString("var_value")==null ? "":rs.getString("var_value");						
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;

						sql="select descr  from location where loc_code = ? " ;
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,varValue.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							locationDescr = rs.getString("descr")==null ? "":rs.getString("descr");						
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						exchangeRateDec = getDailyExchRateSellBuy(currCode,"",siteCodeCurr, currDateTs,"S",conn );
						valueXmlString.append("<loc protect =\"1\">").append("<![CDATA[Consg Loc. :]]>").append("</loc>"); // initial 
						valueXmlString.append("<site_code protect =\"1\">").append("<![CDATA["+siteCodeCurr+"]]>").append("</site_code>");
						valueXmlString.append("<order_date protect =\"1\" >").append("<![CDATA["+currDate+"]]>").append("</order_date>");
						valueXmlString.append("<conf_date protect =\"1\">").append("<![CDATA["+currDate+"]]>").append("</conf_date>");
						valueXmlString.append("<ship_date protect =\"0\">").append("<![CDATA["+currDate+"]]>").append("</ship_date>");
						valueXmlString.append("<due_date protect =\"0\">").append("<![CDATA["+currDate+"]]>").append("</due_date>");
						valueXmlString.append("<chg_date protect =\"1\" >").append("<![CDATA["+currDate+"]]>").append("</chg_date>");
						valueXmlString.append("<site_descr_b protect =\"1\">").append("<![CDATA["+descr+"]]>").append("</site_descr_b>");
						valueXmlString.append("<site_code__ship  protect =\"0\">").append("<![CDATA["+siteCodeCurr.trim()+"]]>").append("</site_code__ship>");
						valueXmlString.append("<site_descr protect =\"1\">").append("<![CDATA["+descr.trim()+"]]>").append("</site_descr>");
						valueXmlString.append("<site_addr1 protect =\"1\">").append("<![CDATA["+addr1.trim()+"]]>").append("</site_addr1>");
						valueXmlString.append("<site_addr2 protect =\"1\">").append("<![CDATA["+addr2.trim()+"]]>").append("</site_addr2>");
						valueXmlString.append("<site_city protect =\"1\">").append("<![CDATA["+city.trim()+"]]>").append("</site_city>");
						valueXmlString.append("<site_pin protect =\"1\">").append("<![CDATA["+pin.trim()+"]]>").append("</site_pin>");
						valueXmlString.append("<site_state_code protect =\"1\">").append("<![CDATA["+stateCode.trim()+"]]>").append("</site_state_code>");
						valueXmlString.append("<confirmed protect =\"1\">").append("<![CDATA[N]]>").append("</confirmed>");
						valueXmlString.append("<status protect =\"1\">").append("<![CDATA[P]]>").append("</status>");
						//valueXmlString.append("<order_type protect =\"1\">").append("<![CDATA[F]]>").append("</order_type>");
						//valueXmlString.append("<sundry_type protect =\"1\">").append("<![CDATA[C]]>").append("</sundry_type>");
						//valueXmlString.append("<auto_receipt protect =\"1\">").append("<![CDATA[N]]>").append("</auto_receipt>");
						//valueXmlString.append("<tran_type protect =\"1\">").append("<![CDATA[IT]]>").append("</tran_type>");
						//valueXmlString.append("<avaliable_yn protect =\"0\">").append("<![CDATA[Y]]>").append("</avaliable_yn>");						
						valueXmlString.append("<exch_rate protect =\"0\">").append("<![CDATA["+exchangeRateDec+"]]>").append("</exch_rate>");
						//valueXmlString.append("<tot_amt protect =\"0\">").append("<![CDATA[0.0]]>").append("</tot_amt>");
						//valueXmlString.append("<tax_amt protect =\"0\">").append("<![CDATA[0.0]]>").append("</tax_amt>");
						//valueXmlString.append("<net_amt protect =\"0\">").append("<![CDATA[0.0]]>").append("</net_amt>");
						//valueXmlString.append("<trans_mode protect =\"0\">").append("<![CDATA[D]]>").append("</trans_mode>");
						//valueXmlString.append("<qty_order protect =\"0\">").append("<![CDATA[0.0]]>").append("</qty_order>");
						//valueXmlString.append("<qty_confirm protect =\"0\">").append("<![CDATA[0.0]]>").append("</qty_confirm>");
						//valueXmlString.append("<qty_received protect =\"0\">").append("<![CDATA[0.0]]>").append("</qty_received>");
						//valueXmlString.append("<qty_shipped protect =\"0\">").append("<![CDATA[0.0]]>").append("</qty_shipped>");
						//valueXmlString.append("<rate protect =\"0\">").append("<![CDATA[0.0]]>").append("</rate>");
						//valueXmlString.append("<qty_return protect =\"0\">").append("<![CDATA[0.0]]>").append("</qty_return>");
					 	//valueXmlString.append("<rate__clg protect =\"0\">").append("<![CDATA[0.0]]>").append("</rate__clg>");
						//valueXmlString.append("<discount protect =\"0\">").append("<![CDATA[0.0]]>").append("</discount>");
						valueXmlString.append("<chg_term protect =\"1\">").append("<![CDATA["+chgTerm+"]]>").append("</chg_term>");
						valueXmlString.append("<chg_user protect =\"1\">").append("<![CDATA["+chgUser+"]]>").append("</chg_user>");
						
						valueXmlString.append("<loc_code__git protect =\"0\">").append("<![CDATA["+varValue+"]]>").append("</loc_code__git>");
						valueXmlString.append("<location_descr protect =\"0\">").append("<![CDATA["+locationDescr+"]]>").append("</location_descr>");
						//valueXmlString.append("<loc_code__gitbf protect =\"0\">").append("<![CDATA["+varValue+"]]>").append("</loc_code__gitbf>");
						valueXmlString.append("<curr_code protect =\"0\">").append("<![CDATA["+currCode+"]]>").append("</curr_code>");
						valueXmlString.append("<bill_site_descr protect =\"1\">").append("<![CDATA[]]>").append("</bill_site_descr>");	
						
				    }//end of itm default	
					if(currentColumn.trim().equalsIgnoreCase( "tran_type" ) )
					{						
						tranType = genericUtility.getColumnValue("tran_type",dom);
						siteCodeShip = genericUtility.getColumnValue("site_code__ship",dom);
						siteCodeDlv = genericUtility.getColumnValue("site_code__dlv",dom);
						siteCodeDlv = siteCodeDlv !=null ?siteCodeDlv.trim() : "";
						if("CT".equalsIgnoreCase(tranType))
						{
							valueXmlString.append("<loc protect =\"1\">").append("<![CDATA[Consg Loc. :]]>").append("</loc>");

						}
						if("LT".equalsIgnoreCase(tranType))
						{
							valueXmlString.append("<loc protect =\"1\">").append("<![CDATA[Loan Loc. :]]>").append("</loc>");

						}
						else
						{
							valueXmlString.append("<loc protect =\"1\">").append("<![CDATA[Consg Loc. :]]>").append("</loc>");
						}
						sql="select loc_cons_protect,sundry_protect,chg_site,auto_receipt,auto_receipt_protect, "
							+"	loc_git_protect,loc_gitbf_protect,avaliable_yn,avaliable_yn_protect, "
							+"	loc_code__git,loc_code__gitbf,loc_code__cons,price_list__clg,dlv_term, "
							+"	price_list	"						 
							+"	from  distorder_type where tran_type = ? " ;
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,tranType.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							locConsProtected = rs.getString("loc_cons_protect")==null ? "":rs.getString("loc_cons_protect");
							sundryProtected = rs.getString("sundry_protect")==null ? "":rs.getString("sundry_protect");
							chgSite = rs.getString("chg_site")==null ? "":rs.getString("chg_site");
							autoReceipt = rs.getString("auto_receipt")==null ? "":rs.getString("auto_receipt");
							autoReceiptProtect = rs.getString("auto_receipt_protect")==null ? "":rs.getString("auto_receipt_protect");
							locGitProtect = rs.getString("loc_git_protect")==null ? "":rs.getString("loc_git_protect");
							locGitbfProtect = rs.getString("loc_gitbf_protect")==null ? "":rs.getString("loc_gitbf_protect");
							avaliableYn = rs.getString("avaliable_yn")==null ? "":rs.getString("avaliable_yn");
							avaliableYnProtect = rs.getString("sundry_protect")==null ? "":rs.getString("sundry_protect");
							locCodeGit = rs.getString("loc_code__git")==null ? "":rs.getString("loc_code__git");
							locCodeGitbf = rs.getString("loc_code__gitbf")==null ? "":rs.getString("loc_code__gitbf");
							locCodeCons = rs.getString("loc_code__cons")==null ? "":rs.getString("loc_code__cons");
							priceListClg = rs.getString("price_list__clg")==null ? "":rs.getString("price_list__clg");
							dlvTerm = rs.getString("dlv_term")==null ? "":rs.getString("dlv_term");
							priceList = rs.getString("price_list")==null ? "":rs.getString("price_list");							
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						if(dlvTerm !=null && dlvTerm.trim().length()>0)
						{
							sql="select policy_no from delivery_term where dlv_term = ?  " ;
							pstmt = conn.prepareStatement(sql);	
							pstmt.setString(1,dlvTerm.trim());
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								policyNo = rs.getString("policy_no")==null ? "":rs.getString("policy_no");						
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
							if(policyNo!=null && policyNo.trim().length()>0)
							{
								valueXmlString.append("<policy_no protect =\"1\">").append("<![CDATA["+policyNo+"]]>").append("</policy_no>");
							}
							rs.close();
							rs = null;
							pstmt.close();
							pstmt = null;
						}
						priceListAcct = distCommon.setPlistTaxClassEnv(siteCodeShip,siteCodeDlv,itemCode,tranType,"","PRICE_LIST",conn);
						valueXmlString.append("<price_list protect =\"0\">").append("<![CDATA["+priceListAcct+"]]>").append("</price_list>");
						priceListAcct = distCommon.setPlistTaxClassEnv(siteCodeShip,siteCodeDlv,itemCode,tranType,"","PRICE_LIST__CLG",conn);
						valueXmlString.append("<price_list__clg protect =\"0\">").append("<![CDATA["+priceListAcct+"]]>").append("</price_list__clg>");	
						if("Y".equalsIgnoreCase(locConsProtected))
						{
							valueXmlString.append("<loc_code__cons protect =\"1\">").append("<![CDATA[]]>").append("</loc_code__cons>");
						}
						else
						{
							valueXmlString.append("<loc_code__cons protect =\"0\">").append("<![CDATA[]]>").append("</loc_code__cons>");
						}
						if("Y".equalsIgnoreCase(sundryProtected))
						{
							valueXmlString.append("<sundry_type protect =\"1\">").append("<![CDATA[]]>").append("</sundry_type>");
							valueXmlString.append("<sundry_code protect =\"1\">").append("<![CDATA[]]>").append("</sundry_code>");
						}
						else
						{
							valueXmlString.append("<sundry_type protect =\"0\">").append("<![CDATA[C]]>").append("</sundry_type>");
							valueXmlString.append("<sundry_code protect =\"0\">").append("<![CDATA[]]>").append("</sundry_code>");
						}

						if("Y".equalsIgnoreCase(chgSite))
						{
							valueXmlString.append("<site_code__ship protect =\"0\">").append("<![CDATA["+siteCodeShip+"]]>").append("</site_code__ship>");
							valueXmlString.append("<site_code__dlv protect =\"0\">").append("<![CDATA["+siteCodeDlv+"]]>").append("</site_code__dlv>");
						}
						else
						{	
							valueXmlString.append("<site_code__ship protect =\"0\">").append("<![CDATA["+siteCodeShip+"]]>").append("</site_code__ship>");
							valueXmlString.append("<site_code__dlv protect =\"0\">").append("<![CDATA["+siteCodeDlv+"]]>").append("</site_code__dlv>");
						}

						valueXmlString.append("<auto_receipt protect =\"1\">").append("<![CDATA["+autoReceipt+"]]>").append("</auto_receipt>");
					
						if("Y".equalsIgnoreCase(autoReceiptProtect))
						{
							valueXmlString.append("<auto_receipt protect =\"1\">").append("<![CDATA["+autoReceipt+"]]>").append("</auto_receipt>");
						}
						else
						{
							valueXmlString.append("<auto_receipt protect =\"0\">").append("<![CDATA["+autoReceipt+"]]>").append("</auto_receipt>");
						}
						if("Y".equalsIgnoreCase(avaliableYnProtect))
						{
							valueXmlString.append("<avaliable_yn protect =\"1\">").append("<![CDATA["+avaliableYn+"]]>").append("</avaliable_yn>");
						}
						else
						{valueXmlString.append("<avaliable_yn protect =\"0\">").append("<![CDATA["+avaliableYn+"]]>").append("</avaliable_yn>");
						}
						if("Y".equalsIgnoreCase(locGitProtect))
						{
							valueXmlString.append("<loc_code__git protect =\"1\">").append("<![CDATA["+locCodeGit+"]]>").append("</loc_code__git>");
						}
						else
						{
							valueXmlString.append("<loc_code__git protect =\"0\">").append("<![CDATA["+locCodeGit+"]]>").append("</loc_code__git>");
						}
						if("Y".equalsIgnoreCase(locCodeGitbf))
						{
							valueXmlString.append("<loc_code__gitbf protect =\"1\">").append("<![CDATA["+locCodeGitbf+"]]>").append("</loc_code__gitbf>");
						}
						else
						{
							valueXmlString.append("<loc_code__gitbf protect =\"0\">").append("<![CDATA["+locCodeGitbf+"]]>").append("</loc_code__gitbf>");
						}
						valueXmlString.append("<loc_code__git protect =\"0\">").append("<![CDATA["+locCodeGit+"]]>").append("</loc_code__git>");
						sql="select sh_descr  from location where loc_code = ? " ;
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,locCodeGit.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							shDescr = rs.getString("sh_descr")==null ? "":rs.getString("sh_descr");						
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;							
						valueXmlString.append("<location_descr protect =\"1\">").append("<![CDATA["+shDescr+"]]>").append("</location_descr>");
						valueXmlString.append("<loc_code__gitbf protect =\"0\">").append("<![CDATA["+locCodeGitbf+"]]>").append("</loc_code__gitbf>");
						valueXmlString.append("<loc_code__cons protect =\"0\">").append("<![CDATA["+locCodeCons+"]]>").append("</loc_code__cons>");
						valueXmlString.append("<avaliable_yn protect =\"0\">").append("<![CDATA["+avaliableYn+"]]>").append("</avaliable_yn>");
					}// end of itemchange tran_type	
					if(currentColumn.trim().equalsIgnoreCase( "site_code__dlv" ) )
					{
						tranType = genericUtility.getColumnValue("tran_type",dom);
						tranType = tranType !=null?tranType:"";
						siteCodeDlv = genericUtility.getColumnValue("site_code__dlv",dom);
						siteCodeDlv = siteCodeDlv !=null ?siteCodeDlv.trim():"";
						siteCodeShip = genericUtility.getColumnValue("site_code__ship",dom);			
						
						sql="select price_list__clg,price_list "							
							+"	from  distorder_type where tran_type = ? ";
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,tranType.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							priceListClg = rs.getString("price_list__clg")==null ? "":rs.getString("price_list__clg");
							priceList = rs.getString("price_list")==null ? "":rs.getString("price_list");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						priceListAcct = distCommon.setPlistTaxClassEnv(siteCodeShip,siteCodeDlv,itemCode,tranType,"","PRICE_LIST",conn);
						if(priceListAcct.trim().length()==0||priceListAcct ==null )
						{
							priceListAcct = priceList;
						}
						valueXmlString.append("<price_list>").append("<![CDATA["+priceListAcct+"]]>").append("</price_list>");
						priceListAcct = distCommon.setPlistTaxClassEnv(siteCodeShip,siteCodeDlv,itemCode,tranType,"","PRICE_LIST__CLG",conn);
						if(priceListAcct.trim().length()==0||priceListAcct ==null )
						{
							priceListAcct = priceListClg;
						}
						valueXmlString.append("<price_list__clg>").append("<![CDATA["+priceListAcct+"]]>").append("</price_list__clg>");
						sql= "select add1,add2,city,pin,state_code,descr from site where site_code= ? ";
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,siteCodeDlv.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString("descr")==null ? "":rs.getString("descr");
							addr1 = rs.getString("add1")==null ? "":rs.getString("add1");
							addr2 = rs.getString("add2")==null ? "":rs.getString("add2");
							city = rs.getString("city")==null ? "":rs.getString("city");
							pin = rs.getString("pin")==null ? "":rs.getString("pin");
							stateCode = rs.getString("state_code")==null ? "":rs.getString("state_code");						 
						}					
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<site_descr_dlv protect =\"1\">").append("<![CDATA["+descr+"]]>").append("</site_descr_dlv>");
						valueXmlString.append("<site_addr1_dlv protect =\"1\">").append("<![CDATA["+addr1+"]]>").append("</site_addr1_dlv>");
						valueXmlString.append("<site_addr2_dlv protect =\"1\">").append("<![CDATA["+addr2+"]]>").append("</site_addr2_dlv>");
						valueXmlString.append("<site_addr3_dlv protect =\"1\">").append("<![CDATA["+addr3+"]]>").append("</site_addr3_dlv>");
						valueXmlString.append("<site_city_dlv protect =\"1\">").append("<![CDATA["+city+"]]>").append("</site_city_dlv>");
						valueXmlString.append("<site_pin_dlv protect =\"1\">").append("<![CDATA["+pin+"]]>").append("</site_pin_dlv>");
						valueXmlString.append("<dlv_site_state_code protect =\"1\">").append("<![CDATA["+stateCode+"]]>").append("</dlv_site_state_code>");
						
						
					}//end of site_code_ship
					if(currentColumn.trim().equalsIgnoreCase( "site_code__ship" ) )
					{
						tranType = genericUtility.getColumnValue("tran_type",dom);
						tranType = tranType !=null? tranType.trim() : "";
						siteCodeShip = genericUtility.getColumnValue("site_code__ship",dom);
						siteCodeShip = siteCodeShip !=null ? siteCodeShip.trim() :"";
						siteCodeDlv = genericUtility.getColumnValue("site_code__dlv",dom);
						sql= "select add1,add2,city,pin,state_code,descr from site where site_code= ? ";
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,siteCodeShip.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							descr = rs.getString("descr")==null ? "":rs.getString("descr");
							addr1 = rs.getString("add1")==null ? "":rs.getString("add1");
							addr2 = rs.getString("add2")==null ? "":rs.getString("add2");
							city = rs.getString("city")==null ? "":rs.getString("city");
							pin = rs.getString("pin")==null ? "":rs.getString("pin");
							stateCode = rs.getString("state_code")==null ? "":rs.getString("state_code");						 
						}					
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						sql="select price_list__clg,price_list "							
							+"	from  distorder_type where tran_type = ? ";
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,tranType.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							priceListClg = rs.getString("price_list__clg")==null ? "":rs.getString("price_list__clg");
							priceList = rs.getString("price_list")==null ? "":rs.getString("price_list");
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						priceListAcct = distCommon.setPlistTaxClassEnv(siteCodeShip,siteCodeDlv,itemCode,tranType,"","PRICE_LIST",conn);
						if(priceListAcct.trim().length()==0||priceListAcct ==null )
						{
							priceListAcct = priceList;
						}
						valueXmlString.append("<price_list>").append("<![CDATA["+priceListAcct+"]]>").append("</price_list>");
						priceListAcct = distCommon.setPlistTaxClassEnv(siteCodeShip,siteCodeDlv,itemCode,tranType,"","PRICE_LIST__CLG",conn);
						if(priceListAcct.trim().length()==0||priceListAcct ==null )
						{
							priceListAcct = priceListClg;
						}
						valueXmlString.append("<price_list__clg>").append("<![CDATA["+priceListAcct+"]]>").append("</price_list__clg>");
						valueXmlString.append("<site_descr protect =\"1\">").append("<![CDATA["+descr+"]]>").append("</site_descr>");
						valueXmlString.append("<site_addr1 protect =\"1\">").append("<![CDATA["+addr1+"]]>").append("</site_addr1>");
						valueXmlString.append("<site_addr2 protect =\"1\">").append("<![CDATA["+addr2+"]]>").append("</site_addr2>");
						valueXmlString.append("<site_addr3 protect =\"1\">").append("<![CDATA["+addr3+"]]>").append("</site_addr3>");
						valueXmlString.append("<site_city protect =\"1\">").append("<![CDATA["+city+"]]>").append("</site_city>");
						valueXmlString.append("<site_pin protect =\"1\">").append("<![CDATA["+pin+"]]>").append("</site_pin>");
						valueXmlString.append("<site_state_code protect =\"1\">").append("<![CDATA["+stateCode+"]]>").append("</site_state_code>");
		
					}//end of site_code_dlv
					if(currentColumn.trim().equalsIgnoreCase( "loc_code__git" ))
					{
						locCodeGit = genericUtility.getColumnValue("loc_code__git",dom);
						sql="select descr  from location where loc_code = ? " ;
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,locCodeGit.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							shDescr = rs.getString("descr")==null ? "":rs.getString("descr");						
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
						valueXmlString.append("<location_descr protect =\"1\">").append("<![CDATA["+shDescr+"]]>").append("</location_descr>");					
					}
					if(currentColumn.trim().equalsIgnoreCase( "site_code__bil" ))
					{
						billSiteCode = genericUtility.getColumnValue("site_code__bil",dom);
						billSiteCode = billSiteCode == null ? "" :billSiteCode.trim();						
						sql= "select descr from site where site_code= ? ";
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,billSiteCode.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							shDescr = rs.getString("descr")==null ? "":rs.getString("descr");		
							
						}
						valueXmlString.append("<bill_site_descr protect =\"1\">").append("<![CDATA["+shDescr+"]]>").append("</bill_site_descr>");	
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
					}
			}  				
			valueXmlString.append("</Detail1>");
		    valueXmlString.append("</Root>");	
			
		}//END OF TRY
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception ::"+e.getMessage());
			throw new ITMException(e);
		}
		finally
		{
			try{
				if( rs != null )
				{
					rs.close();
					rs = null;
				}
				if( pstmt != null )
				{
					pstmt.close();
					pstmt = null;
				}
				if(conn!=null)
				{
					conn.close();
					conn=null;
				}
			}catch( Exception ex ){ex.printStackTrace();}
		}
		return valueXmlString.toString();
	 }//END OF ITEMCHANGE
	private double  getDailyExchRateSellBuy(String currCode,String currCodeTo,String siteCodeCurr,Timestamp orderDate,String tranType, Connection conn ) throws ITMException
	{
		PreparedStatement pstmt = null;
		ResultSet rs= null;
		String finEntity = "",sql="",currCodeToFien="",varValue="";
		double exchangeRateDec =0;		
		if(currCode.trim().equalsIgnoreCase(currCodeTo.trim()))
		{
			return 1;
		}
		try
		{
			 if(currCodeTo.trim().length()==0||currCodeTo==null)
			{	 
				sql= "select fin_entity  from site where site_code = ? " ;
				pstmt = conn.prepareStatement(sql);	
				pstmt.setString(1,siteCodeCurr.trim());
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					finEntity = rs.getString("fin_entity")==null ? "":rs.getString("fin_entity");						
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				sql= "select curr_code  from finent where  fin_entity = ?" ;
				pstmt = conn.prepareStatement(sql);	
				pstmt.setString(1,finEntity.trim());
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					currCodeToFien = rs.getString("curr_code")==null ? "":rs.getString("curr_code");						
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
			}
			else
			{
				currCodeToFien	=currCodeTo;
			}
			if(tranType.equalsIgnoreCase("B"))
			{
				sql= "select exch_rate__sell "	
					+"	from 	 daily_exch_rate_sell_buy "
					+"	where	 curr_code = ? and "
					+"	 curr_code__to = ?  and "
					+"	? between from_date and to_date " ;
				pstmt = conn.prepareStatement(sql);	
				pstmt.setString(1,currCode.trim());
				pstmt.setString(2,currCodeToFien.trim());
				pstmt.setTimestamp(3,orderDate);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					exchangeRateDec = rs.getDouble("exch_rate__sell");						
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(exchangeRateDec==0 )
				{
					sql= "select exch_rate "	
						+"	from 	 daily_exch_rate_sell_buy "
						+"	where	 curr_code = ? and "
						+" curr_code__to = ?  and "
						+"	? between from_date and to_date " ;
					pstmt = conn.prepareStatement(sql);	
					pstmt.setString(1,currCode.trim());
					pstmt.setString(2,currCodeToFien.trim());
					pstmt.setTimestamp(3,orderDate);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						exchangeRateDec = rs.getDouble("exch_rate");						
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if( exchangeRateDec!=0)
					{
						exchangeRateDec = 1/exchangeRateDec;
					}
				}
			}// end of code 
			if(tranType.equalsIgnoreCase("S"))
			{
				sql= "	select exch_rate__sell "	
					+"	from 	 daily_exch_rate_sell_buy "
					+"	where	 curr_code = ? and "
					+"	 curr_code__to = ?  and "
					+"	? between from_date and to_date " ;
				pstmt = conn.prepareStatement(sql);	
				pstmt.setString(1,currCode.trim());
				pstmt.setString(2,currCodeToFien.trim());
				pstmt.setTimestamp(3,orderDate);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					exchangeRateDec = rs.getDouble("exch_rate__sell");						
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(exchangeRateDec==0)
				{
					sql= "select exch_rate 	"	
						+"	from 	 daily_exch_rate_sell_buy "
						+"	where	 curr_code = ? and "
						+"	 curr_code__to = ?  and "
						+"	? between from_date and to_date " ;
					pstmt = conn.prepareStatement(sql);	
					pstmt.setString(1,currCode.trim());
					pstmt.setString(2,currCodeToFien.trim());
					pstmt.setTimestamp(3,orderDate);
					rs = pstmt.executeQuery();
					if(rs.next())
					{
						exchangeRateDec = rs.getDouble("exch_rate");						
					}
					rs.close();
					rs = null;
					pstmt.close();
					pstmt = null;
					if(exchangeRateDec!=0)
					{
						exchangeRateDec = 1/exchangeRateDec;
					}
				}
			}// end of code
			if(exchangeRateDec==0)
			{
				sql= "select exch_rate__sell "	
					+"	from 	 daily_exch_rate_sell_buy "
					+"	where	 curr_code = ? and "
					+"	 curr_code__to = ?  and "
					+"	? between from_date and to_date " ;
				pstmt = conn.prepareStatement(sql);	
				pstmt.setString(1,currCode.trim());
				pstmt.setString(2,currCodeToFien.trim());
				pstmt.setTimestamp(3,orderDate);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					exchangeRateDec = rs.getDouble("exch_rate__sell");						
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if(exchangeRateDec==0)
				{
					sql= "select exch_rate "	
						+"	from 	 daily_exch_rate_sell_buy "
						+"	where	 curr_code = ? and "
						+"	 curr_code__to = ?  and "
						+"	? between from_date and to_date " ;
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,currCode.trim());
						pstmt.setString(2,currCodeToFien.trim());
						pstmt.setTimestamp(3,orderDate);
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							exchangeRateDec = rs.getDouble("exch_rate");						
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
				}			
			}
			if(exchangeRateDec==0)
			{
				
				sql="select case when var_value is null then 'Y' else var_value end from finparm "
					+"	where prd_code = '999999' "
					+"	and var_name = 'EXCRT_CURR' " ;
				pstmt = conn.prepareStatement(sql);						
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					varValue  = rs.getString(1)!=null? rs.getString(1) :"";						
				}
				rs.close();
				rs = null;
				pstmt.close();
				pstmt = null;
				if("Y".equalsIgnoreCase(varValue))
				{
					sql="select std_exrt from currency "
						+"	where	 curr_code = ? ";
						pstmt = conn.prepareStatement(sql);	
						pstmt.setString(1,currCode.trim());
						rs = pstmt.executeQuery();
						if(rs.next())
						{
							exchangeRateDec = rs.getDouble("std_exrt");						
						}
						rs.close();
						rs = null;
						pstmt.close();
						pstmt = null;
				}
				else
				{
					return 0;
				}
			}

		}//end of try 
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return  exchangeRateDec;
	}
 }// END OF MAIN CLASS