package ibase.webitm.ejb.dis;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ProcessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.ejb.fin.MiscDrCrRcpConf;
//import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import ibase.webitm.ejb.ValidatorEJB;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class ProofOfDeliveryProcess {
	E12GenericUtility genericUtility= new  E12GenericUtility();
	String siteCodeG="",invTypeG="",xtraParamsG="",schemeCodeG="",multipleScheme="";
	Map<String,Double> partialInvoiceMapG = new HashMap <String,Double>(); 	  
	Map <String,Double>partialInvMap=new HashMap<String,Double>();
	int invdoneCnt=0;
	public String process(String xmlString, String xmlString2, String windowName, String xtraParams) throws RemoteException,ITMException{
		System.out.println("enter in process(212....................");
		String retStr = "";
		Document detailDom = null;
		Document headerDom = null;
			
		//GenericUtility genericUtility = GenericUtility.getInstance();			
		try{	
		if(xmlString != null && xmlString.trim().length()!=0){
		  headerDom = genericUtility.parseString(xmlString); 
		  System.out.println("xmlString--->>" + xmlString);
		}
		if(xmlString2 != null && xmlString2.trim().length()!=0){
		  detailDom = genericUtility.parseString(xmlString2); 
		  System.out.println("xmlString2 --->>" + xmlString2);
		}
		xtraParamsG=xtraParams;
		retStr = process(headerDom, detailDom, windowName, xtraParams);

			}
			catch (Exception e)
			{
				System.out.println("Exception :PODProcess :process(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
				e.printStackTrace();
				/*retStr = e.getMessage();*/ //Commented By Mukesh Chauhan on 05/08/19
				throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
			}
			return retStr;

  }
	
	public String process(Document dom, Document dom2, String windowName, String xtraParams) throws RemoteException,ITMException
	{
		System.out.println("enter in process(dom) "); 
		ArrayList <String>inv45dayList=new ArrayList<String>();
		ArrayList<String> podProcessInvIDList=new ArrayList<String>(); 
		ArrayList<String> podNotConfList=new ArrayList<String>();
		ArrayList<String> podTraceInvList=new ArrayList<String>();
		ArrayList<String> miscDrCrInvList=new ArrayList<String>();
		ArrayList<String> PodHdrInvList=new ArrayList<String>();
		Connection conn = null;
		ConnDriver connDriver = null;
		//GenericUtility genericUtility = null;
		ProofOfDelivery podObject=null;
		ITMDBAccessEJB itmdbAccess = null;
		ValidatorEJB vdt=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;		
		int cnt=0;
		
		double ExchRate=0;
		String siteCode="",custCodeFrm="",custCodeTo="",invType="",errorString="",errCode="",userId="",custCode="";
		String sql="",invoiceId="",priceList="",currCode="";
		
		try{			
			//genericUtility = GenericUtility.getInstance();
			itmdbAccess=new ITMDBAccessEJB();
			podObject=ProofOfDelivery.getInstance();
		    vdt=new ValidatorEJB();
			connDriver = new ConnDriver();
			conn = connDriver.getConnectDB("DriverITM");
			//conn = getConnection();
			connDriver = null;
			userId = vdt.getValueFromXTRA_PARAMS(xtraParams,"loginCode");
		   siteCode=genericUtility.getColumnValue("site_code", dom);
		   custCodeFrm=genericUtility.getColumnValue("cust_code_from", dom);
		   custCodeTo=genericUtility.getColumnValue("cust_code_to", dom);
		   invType=genericUtility.getColumnValue("inv_type", dom);
		
		   siteCode=siteCode==null ? "" :siteCode.trim();
		   custCodeFrm=custCodeFrm==null ? "" :custCodeFrm.trim();
		   custCodeTo=custCodeTo==null ? "" :custCodeTo.trim();
		   invType=invType==null ? "" :invType.trim();
		   System.out.println("SiteCode-133-->>["+siteCode+"]");
		   System.out.println("custCodeFrm--->>["+custCodeFrm+"]");
		   System.out.println("custCodeTo--->>["+custCodeTo+"]");
		   System.out.println("invType--->>["+invType+"]");
		
		//Validation for Site code
		if (siteCode.length() == 0 ){							
			errCode = "VTSITECNE";
			errorString = vdt.getErrorString("site_code",errCode,userId);
			return errorString;
			
		}else{							
			cnt=podObject.getDBRowCount(conn,"site","site_code",siteCode);
			if(cnt == 0){
			System.out.println("site_code not exist validation fire");
			errCode = "VTSITENEX";
			errorString = vdt.getErrorString("site_code",errCode,userId);	
			return errorString;
			}		
		  }
		//Validation for Customer Code From
		if (custCodeFrm.length() == 0 ){							
			errCode = "VTCUSTCNE";
			errorString = vdt.getErrorString("cust_code_from",errCode,userId);
			return errorString;
		
		}else{
			System.out.println("custCodeFrm--->>["+custCodeFrm+"]");
			if(!("00".equalsIgnoreCase(custCodeFrm))){
				cnt=podObject.getDBRowCount(conn,"customer","cust_code",custCodeFrm);
				if(cnt == 0){
				System.out.println("custCodeFrom not exist validation fire");
				errCode = "VTCUSTNEX";
				errorString = vdt.getErrorString("cust_code_from",errCode,userId);	
				return errorString;
				}		
			}			
		}
		
		//Validation for Customer Code To 
		if (custCodeTo.length() == 0 ){							
			errCode = "VTCUSTCNE";
			errorString = vdt.getErrorString("cust_code_to",errCode,userId);
			return errorString;		
		}else{							
			System.out.println("custCodeTo --->>["+custCodeTo+"]");
			if(!("ZZ".equalsIgnoreCase(custCodeTo))){
				cnt=podObject.getDBRowCount(conn,"customer","cust_code",custCodeTo);
				if(cnt == 0){
				System.out.println("custCodeFrom not exist validation fire");
				errCode = "VTCUSTNEX";
				errorString = vdt.getErrorString("cust_code_to",errCode,userId);	
				return errorString;	
			   }			
			}		
		 }
		//Validation for invoice type. only DM or IS invoice type allowed.		
		if(invType.length() == 0){
			errCode = "VTINVTNN";
			errorString = vdt.getErrorString("inv_type",errCode,userId);
			return errorString;			
		}else{
			if(!("DM".equalsIgnoreCase(invType) || "IS".equalsIgnoreCase(invType)) ){
			errCode = "VTINVTI";
			errorString = vdt.getErrorString("inv_type",errCode,userId);
			return errorString;
			}			
		}
		
		if(custCodeFrm.length() > 0 && custCodeTo.length() > 0 && siteCode.length() > 0){
			
			siteCodeG=siteCode;
			invTypeG=invType;			
			if("00".equalsIgnoreCase(custCodeFrm) && "ZZ".equalsIgnoreCase(custCodeTo))
			{
				sql="select invoice_id from invoice where site_code = ? and confirmed = ?"
				+ " and sysdate - conf_date > 45 and inv_type = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, siteCode);
				pstmt.setString(2, "Y");
				pstmt.setString(3,invType);
			}else{
				sql="select invoice_id from invoice where cust_code__bil between ? and  ? and site_code = ? and confirmed = ?"
						+ " and sysdate - conf_date > 45 and inv_type = ?";
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, custCodeFrm);
				pstmt.setString(2, custCodeTo);
				pstmt.setString(3, siteCode);
				pstmt.setString(4, "Y");
				pstmt.setString(5,invType);
			}		
		
		
		rs=pstmt.executeQuery();
		while(rs.next()){			
		invoiceId=rs.getString(1);
		invoiceId=invoiceId==null ? "" :invoiceId.trim();			
		inv45dayList.add(invoiceId);			
		}
	    rs.close();
	    rs=null;
	    pstmt.close();
	    pstmt=null;
	    System.out.println("Size of arrayList inv45dayList----->>["+inv45dayList.size()+"]");
	    System.out.println("Actual inv45dayList-->>["+inv45dayList+"]");
	    
	    PodHdrInvList=getDoneInvoiceIDFromPODHdr(conn,inv45dayList);
		System.out.println("POD done arrayList---->>["+PodHdrInvList+"]");
	    
		  miscDrCrInvList=getDoneInvoiceIDFromMiscDrCrRcp(conn, PodHdrInvList);
			System.out.println("miscDrCrInvList11121---->>["+miscDrCrInvList+"]");
			if(miscDrCrInvList.contains("Error")){
				errCode="VTPRCNCP"; 
				errorString = itmdbAccess.getErrorString("", errCode, "", "", conn);
				return errorString;
			}
		
			if(miscDrCrInvList.size() > 0){
				podProcessInvIDList.addAll(miscDrCrInvList);
			}
			
			if(podProcessInvIDList.size()<=0 && partialInvoiceMapG.size() <= 0){
				errCode="VTDNFIN"; //Data not found 
				errorString = itmdbAccess.getErrorString("", errCode, "", "", conn);
				return errorString;
			}
	    
	    
		}
		
		
		
		
		
		
		} //end try
		catch(Exception e)
		{
			System.out.println("Exception : POD Process : "+e.getMessage());
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		return "";
	}
	
	
	private ArrayList<String> getDoneInvoiceIDFromPODHdr(Connection conn,ArrayList<String>inv45dayListL) throws ITMException
	{
		System.out.println("------in getDoneInvoiceIDFromPODHdr------");
		PreparedStatement pstmt=null,pstmt1=null;
		ResultSet rs=null,rs1=null;
		String sql="",remarks="",subInvId="",TranId="",sql1="",itemCode="",itemCodeM="",confirmed="",
				lotNo="",lotSl="",lotNoM="",lotSlM="",traceLineNoS="";
		int lineNoTrace=0,lineNoTraceM=0,deleteId=0,traceLineNo=0;
		double sumQuantityMisc=0,sumQuantityInv=0,pendQty=0,qtyM=0;
		HashSet<String> unqInvID=new HashSet <String>();
		ArrayList<String> tranIDList=new ArrayList <String>();
		ArrayList<String> InvIDList=new ArrayList <String>();
		ArrayList<String> InvIDTemp=new ArrayList <String>();
		ProofOfDelivery podL=ProofOfDelivery.getInstance();
		try{		
			Iterator<String> it=inv45dayListL.iterator();		
			while(it.hasNext())
			{	
				deleteId=1;
				String id=it.next();
				confirmed=podL.getColumnDescr(conn,"confirmed","spl_sales_por_hdr","invoice_id",id);
				confirmed=confirmed==null ? "N" :confirmed.trim();
				System.out.println("confirmed-->["+confirmed+"] invID-->["+id+"]");
				if("Y".equalsIgnoreCase(confirmed)){
					sql1="select d.quantity__resale,d.item_code,d.lot_no,d.lot_sl,d.pend_qty, "
							+ "line_no__trace,d.loc_code from spl_sales_por_hdr h,spl_sales_por_det d "
							+ "where h.tran_id = d.tran_id and h.invoice_id = ? "
							+ " and d.pend_qty > 0";
					
					pstmt1=conn.prepareStatement(sql1);
					pstmt1.setString(1, id);				
					rs1=pstmt1.executeQuery();
					while(rs1.next()){
						//lineNoTraceM=rs1.getInt(1);
						qtyM=rs1.getDouble(1);
						itemCodeM=rs1.getString(2);
						lotNoM=rs1.getString(3);
						lotSlM=rs1.getString(4);
						pendQty=rs1.getDouble(5);
						traceLineNo=rs1.getInt(6);
						
						traceLineNoS=String.valueOf(traceLineNo);
						itemCodeM=itemCodeM==null ? "" :itemCodeM.trim();
						lotNoM=lotNoM==null ? "" :lotNoM.trim();
						lotSlM=lotSlM==null ? "" :lotSlM.trim();
						InvIDTemp.add(id);
						partialInvoiceMapG.put(traceLineNoS+":"+id+":"+itemCode+":"+lotNoM+":"+lotSlM, pendQty);
					}
					
					
				}	//confirmed id cond. end	
				
			} // end while 1
			System.out.println("unqInvID addAll--->>["+unqInvID+"]");	
			System.out.println("InvIDTemp --->>["+InvIDTemp+"]");	
			if(InvIDTemp.size() > 0){
				unqInvID.addAll(InvIDTemp);
				InvIDList.clear();
				InvIDList.addAll(unqInvID);
				System.out.println("InvIDList addAll--->>["+InvIDList+"]");
				//partialInvoice.addAll(InvIDList);
				
			}
			System.out.println("partialInvoiceMapG --->>["+partialInvoiceMapG+"]");
			if(inv45dayListL.size() > 0){
				Iterator <String>it1=InvIDList.iterator();
				while (it1.hasNext()) {
					String podPrcId=it1.next();
					if(inv45dayListL.contains(podPrcId))
						System.out.println("---in misc arrayList------"+podPrcId);
						it1.remove();
						inv45dayListL.remove(podPrcId);
						
					}
					
				}
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			inv45dayListL.add("Error");
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println("inv45dayListL addAll--->>["+inv45dayListL+"]");
		return inv45dayListL;
	}
	private ArrayList<String> getDoneInvoiceIDFromMiscDrCrRcp(Connection conn,ArrayList<String>inv45dayListL) throws ITMException
	{
		System.out.println("------in getDoneInvoiceIDFromMiscDrCrRcp1234------");
		PreparedStatement pstmt=null,pstmt1=null;
		ResultSet rs=null,rs1=null;
		String sql="",remarks="",subInvId="",TranId="",sql1="",itemCode="",itemCodeM="",confirmed="",
				lotNo="",lotSl="",lotNoM="",lotSlM="",traceNo="";
		int lineNoTrace=0,lineNoTraceM=0;
		double sumQuantityMisc=0,sumQuantityInv=0,qty=0,qtyM=0;
		HashSet<String> unqInvID=new HashSet <String>();
		ArrayList<String> tranIDList=new ArrayList <String>();
		ArrayList<String> InvIDList=new ArrayList <String>();
		ArrayList<String> InvIDTemp=new ArrayList <String>();
		ProofOfDelivery podL=ProofOfDelivery.getInstance();
		boolean isDuplicate=false,isDuplicate1=false;
		try{		
			Iterator<String> it=inv45dayListL.iterator();		
			while(it.hasNext())
			{	
				String id=it.next();
				/*confirmed=podL.getColumnDescr(conn,"confirmed","spl_sales_por_hdr","invoice_id",id);
				confirmed=confirmed==null ? "N" :confirmed.trim();
				System.out.println("confirmed-->["+confirmed+" invID-->["+id+"]");*/
				 
				sql="select quantity,item_code,lot_no,lot_sl,line_no__trace from invoice_trace where invoice_id = ? "
						+ "group by item_code,lot_no, lot_sl,line_no__trace";
						
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, id);
				rs=pstmt.executeQuery();
				while(rs.next()){
					//lineNoTrace=rs.getInt(1);
					isDuplicate=false;
					qty=rs.getDouble(1);
					itemCode=rs.getString(2);
					
					lotNo=rs.getString(3);
					lotSl=rs.getString(4);
					lineNoTrace=rs.getInt(5);
					
					itemCode=itemCode==null ? "" :itemCode.trim();
					lotNo=lotNo==null ? "" :lotNo.trim();
					lotSl=lotSl==null ? "" :lotSl.trim();
					
					sql1="select sum(nvl(d.quantity,0)) as Quantity,d.item_code,d.lot_sl, d.lot_no,d.line_no__invtrace" 
							 +" from misc_drcr_rcp h,misc_drcr_rdet d where h.tran_id=d.tran_id and h.remarks" 
							 +" like ? and Quantity <> 0 group by d.item_code,d.lot_sl,d.lot_no,d.line_no__invtrace";
					/*sql1="select sum(nvl(d.quantity__resale,0)),d.item_code from spl_sales_por_hdr h,spl_sales_por_det d "
							+ "where h.tran_id = d.tran_id and h.invoice_id = ? group by d.item_code";*/
					pstmt1=conn.prepareStatement(sql1);
					pstmt1.setString(1, "%POD%"+id);				
					rs1=pstmt1.executeQuery();
					while(rs1.next()){
						//lineNoTraceM=rs1.getInt(1);
						qtyM=rs1.getDouble(1);
						itemCodeM=rs1.getString(2);
						lotSlM=rs1.getString(3);
						lotNoM=rs1.getString(4);
						lineNoTraceM=rs.getInt(5);
						traceNo=String.valueOf(lineNoTraceM);
						itemCodeM=itemCodeM==null ? "" :itemCodeM.trim();
						lotNoM=lotNoM==null ? "" :lotNoM.trim();
						lotSlM=lotSlM==null ? "" :lotSlM.trim();
						
						System.out.println("itemCodeM-->>["+itemCodeM+" itemCodeM-->["+itemCodeM+"]");
						System.out.println("qtyMisc-->>["+qtyM+" qtyInvoice-->["+qty+"]");
						if(qtyM < qty && itemCode.equalsIgnoreCase(itemCodeM)){
							if(lotNoM.equalsIgnoreCase(lotNo) && lotSlM.equalsIgnoreCase(lotSl)){
								InvIDTemp.add(id); 
								partialInvoiceMapG.put(traceNo+":"+id+":"+itemCode+":"+lotNoM+":"+lotSlM, qty- qtyM);
							}
						}else{
							it.remove();
							inv45dayListL.remove(id);
							isDuplicate=true;
							break;
						}
					}
					if(isDuplicate){
						System.out.println("isDuplicate continue......isDuplicate true");
						isDuplicate1=true;
						break;
					}
						
					if(rs1!=null){
						rs1.close();
						rs1=null;
					}
					
				}
				System.out.println("InvTemp --->>["+InvIDTemp+"]");			
				if(isDuplicate1){
					System.out.println("isDuplicate1 continue......isDuplicate true");			
					continue;
				}
				
			}
			System.out.println("unqInvID addAll--->>["+unqInvID+"]");	
			System.out.println("InvIDTemp --->>["+InvIDTemp+"]");	
			if(InvIDTemp.size() > 0){
				unqInvID.addAll(InvIDTemp);
				InvIDList.clear();
				InvIDList.addAll(unqInvID);
				System.out.println("InvIDList addAll--->>["+InvIDList+"]");
				//partialInvoice.addAll(InvIDList);
				
			}
			System.out.println("partialInvoiceMapG --->>["+partialInvoiceMapG+"]");
			if(inv45dayListL.size() > 0){
				Iterator <String>it1=InvIDList.iterator();
				while (it1.hasNext()) {
					String podPrcId=it1.next();
					if(inv45dayListL.contains(podPrcId))
						System.out.println("---in misc arrayList------"+podPrcId);
						it1.remove();
						inv45dayListL.remove(podPrcId);
						
					}
					
				}
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			inv45dayListL.clear();
			inv45dayListL.add("Error");
			throw new ITMException(e); //Added By Mukesh Chauhan on 05/08/19
		}
		System.out.println("inv45dayListL addAll--->>["+inv45dayListL+"]");
		return inv45dayListL;
	}
	


}
