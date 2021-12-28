package ibase.webitm.ejb.dis;


import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import ibase.system.config.ConnDriver;
import ibase.utility.E12GenericUtility;
import ibase.webitm.ejb.ITMDBAccessEJB;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.GenericUtility;
import ibase.webitm.utility.ITMException;
import javax.ejb.Stateless;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Stateless

public class DistributionOrderTypeIC extends ValidatorEJB implements DistributionOrderTypeICRemote, DistributionOrderTypeICLocal {


	//Comment By Nasruddin 07-10-16
	 //GenericUtility genericUtility = GenericUtility.getInstance();
	 E12GenericUtility genericUtility = new E12GenericUtility();
	  ITMDBAccessEJB itmdbAccessEJB = new ITMDBAccessEJB();
	  
	  public String wfValData(String xmlString, String xmlString1, String xmlString2, String objContext, String editFlag, String xtraParams)
				throws RemoteException, ITMException {

			String rtStr = "";
			Document dom = null;
			Document dom1 = null;
			Document dom2 = null;
			try {
				System.out.println("wfValdata string :::::");
				System.out.println("::: xmlString" + xmlString);
				System.out.println("::: xmlString1" + xmlString1);
				System.out.println("::: xmlString2" + xmlString2);

				if (xmlString != null && xmlString.trim().length() > 0) {
					dom = genericUtility.parseString(xmlString);
				}
				if (xmlString1 != null && xmlString1.trim().length() > 0) {
					dom1 = genericUtility.parseString(xmlString1);
				}
				if (xmlString2 != null && xmlString2.trim().length() > 0) {
					dom2 = genericUtility.parseString(xmlString2);
				}
				rtStr = wfValData(dom, dom1, dom2, objContext, editFlag, xtraParams);
			} catch (Exception e) {
				System.out.println(":::" + this.getClass().getSimpleName() + "::" + e.getMessage());
				e.getMessage();
			}
			return rtStr;
		}
	  
	  @Override
		public String wfValData(Document dom, Document dom1, Document dom2, String objContext, String editFlag, String xtraParams)
				throws RemoteException, ITMException {

			String errString = "";
			String sql = "", locCodeGit = "", tranType = "", locCodeGitBf = "", locCodeCons = "", keyFlag = "", tranTypeParent = "";
			Connection conn = null;
			String userId = "", deliveryTerm = "", locGroupCons = "", locCode = "", locGroup = "", taxClass = "", taxEnv = "";
			String modName =  "w_distorder_type_mst";
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			NodeList parentNodeList = null, childNodeList = null;
			Node parentNode = null, childNode = null;
			int ctr = 0, childNodeLength = 0, currentFormNo = 0;
			String childNodeName = "";
			try {
				ConnDriver con = new ConnDriver();
				//Changes and Commented By Bhushan on 13-06-2016 :START
				//conn = con.getConnectDB("DriverITM");
				conn = getConnection();
				//Changes and Commented By Bhushan on 13-06-2016 :END
				conn.setAutoCommit(false);
				userId = genericUtility.getValueFromXTRA_PARAMS(xtraParams, userId);
				if (objContext != null && objContext.trim().length() > 0) {
					currentFormNo = Integer.parseInt(objContext);
				}
				System.out.println("in wfValdata doc :::::");
				switch (currentFormNo) {
				case 1:
					parentNodeList = dom.getElementsByTagName("Detail1");
					parentNode = parentNodeList.item(0);
					childNodeList = parentNode.getChildNodes();
					childNodeLength = childNodeList.getLength();
					for (ctr = 0; ctr < childNodeLength; ctr++)
					{
						childNode = childNodeList.item(ctr);
						childNodeName = childNode.getNodeName();
						if(childNodeName.equalsIgnoreCase("tran_type"))
						{
							tranType = genericUtility.getColumnValue("tran_type", dom);
							tranType = tranType == null ? "" : tranType.trim();
							System.out.println(":::: tran type" + tranType);
							sql = " select key_flag from TRANSETUP where TRAN_WINDOW = ? " ;
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, modName);
							rs = pstmt.executeQuery();
							if(rs.next())
							{
								keyFlag = rs.getString("key_flag");
							}
							pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;
							
							keyFlag = keyFlag == null ? "M" : keyFlag.trim();
							
							if(keyFlag.equalsIgnoreCase("M") && tranType.isEmpty() )
							{
								errString = itmdbAccessEJB.getErrorString("tran_type", "VMCODNULL", userId);
								return errString;
							}
							else if(editFlag.equalsIgnoreCase("A"))
							{
								int count = 0;
								sql = "select count(*) as count from distorder_type where tran_type = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, tranType);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count = rs.getInt("count");
								}
								
								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;
								
								if(count > 0)
								{
									errString = itmdbAccessEJB.getErrorString("tran_type", "VMDUPL1", userId);
									return errString;
								}
							}
						}
						
						if(childNodeName.equalsIgnoreCase("loc_code__git"))
						{
							//if(childNode.getFirstChild() != null)
								
							locCodeGit = genericUtility.getColumnValue("loc_code__git", dom);
							
							System.out.println("::: loc code git" + locCodeGit);
							
							int count = 0;
							// Changed By Nasruddin [16-SEP-16]
							if(locCodeGit != null)
							{
								sql = "select count(*) as count from location where loc_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, locCodeGit.trim());
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count = rs.getInt("count");
								}

								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;

								if(count == 0)
								{
									errString = itmdbAccessEJB.getErrorString("loc_code__git","VMLOC6", userId);
									return errString;
								}
							}
						}
						
						if(childNodeName.equalsIgnoreCase("loc_code__gitbf"))
						{
							//if(childNode.getFirstChild() != null)
							locCodeGitBf = genericUtility.getColumnValue("loc_code__gitbf", dom);
							
							System.out.println(":::: loc code gitbf" + locCodeGitBf);
							
							int count = 0;
							
							// Changed By Nasruddin [16-SEP-16]
							if(locCodeGitBf != null)
							{
								sql = "select count(*) as count from location where loc_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, locCodeGitBf.trim());
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count = rs.getInt("count");
								}

								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;

								if(count == 0)
								{
									errString = itmdbAccessEJB.getErrorString("loc_code__gitbf","VMLOC6", userId);
									return errString;
								}
							}
						}
						
						if(childNodeName.equalsIgnoreCase("loc_code__cons"))
						{
							//if(childNode.getFirstChild() != null)
							locCodeCons = genericUtility.getColumnValue("loc_code__cons", dom);
							
							System.out.println(":::: loc code cons" + locCodeCons);
							
							int count = 0;
							// Changed By Nasruddin [16-SEP-16]
							if(locCodeCons != null)
							{
								sql = "select count(*) as count from location where loc_code = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, locCodeCons.trim());
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count = rs.getInt("count");
								}

								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;

								if(count == 0)
								{
									errString = itmdbAccessEJB.getErrorString("loc_code__cons","VMLOC6", userId);
									return errString;
								}
							}
						}
						
						if(childNodeName.equalsIgnoreCase("tran_type__parent"))
						{
							tranTypeParent = genericUtility.getColumnValue("tran_type__parent", dom);
							tranTypeParent = tranTypeParent == null ? "" : tranTypeParent.trim();

							System.out.println(":::: tran type parent" + tranTypeParent);

							if(tranTypeParent.isEmpty())
							{
								//errString = itmdbAccessEJB.getErrorString("tran_type__parent", "VTBLTRATYP", userId); Comment By Nasruddin [16-sep-16]
								errString = itmdbAccessEJB.getErrorString("tran_type__parent", "VMIPTT", userId);
								return errString;
							}
							else
							{

								int count = 0;
								sql = "select count(*) as count from distorder_type where tran_type = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, tranTypeParent);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count = rs.getInt("count");
								}

								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;
								if(tranType != tranTypeParent)
								{
									if(count == 0)
									{
										
									//	errString = itmdbAccessEJB.getErrorString("tran_type__parent", "VTINVTRTYD", userId); Comment By Nasruddin [16-sep-16]
										errString = itmdbAccessEJB.getErrorString("tran_type__parent", "VMPTTND", userId);
										return errString;
									}	
								}
							}
						}
						
						if(childNodeName.equalsIgnoreCase("dlv_term"))
						{
							
							//if(childNode.getFirstChild() != null)
							//{
								int count = 0;
								deliveryTerm = genericUtility.getColumnValue("dlv_term", dom);

								// Changed By Nasruddin [16-Sep-16]
								if(deliveryTerm != null && deliveryTerm.trim().length() > 0)
								{
									System.out.println(":::: delivery term" + deliveryTerm);

									sql = "select count(*) as count from DELIVERY_TERM where dlv_term = ? ";
									pstmt = conn.prepareStatement(sql);
									pstmt.setString(1, deliveryTerm.trim());
									rs = pstmt.executeQuery();
									if(rs.next()){
										count = rs.getInt("count");
									}

									pstmt.close();
									pstmt = null;
									rs.close();
									rs = null;

									if(count == 0)
									{
										errString = itmdbAccessEJB.getErrorString("dlv_term", "VMDLVTERM1", userId);
										return errString;
									}
								}
							//}
						}
						
						if(childNodeName.equalsIgnoreCase("loc_group__cons"))
						{
							locGroupCons = genericUtility.getColumnValue("loc_group__cons", dom);
							locGroupCons = locGroupCons == null ? "" : locGroupCons.trim();

							System.out.println(":::: loc group cons" + locGroupCons);

							locCode = genericUtility.getColumnValue("loc_code__cons", dom);
							locCode = locCode == null ? "" : locCode.trim();

							System.out.println(":::: loc code" + locCode);

							sql = "select loc_group from location where loc_code = ? ";
							pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, locCode);
                            rs = pstmt.executeQuery();
                            
							if(rs.next())
							{
								locGroup = rs.getString("loc_group"); 
							}
							locGroup = locGroup == null ? "" : locGroup.trim();
							System.out.println(":::loc group" + locGroup);


							/*pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;*/


							if(!locGroup.equalsIgnoreCase(locGroupCons))
							{
								errString = itmdbAccessEJB.getErrorString("loc_group__cons", "VMLOCGRCON", userId);  
								return errString;
                            }
                           
                            //added by monika salla on 31 dec 20--to remove dirty connection
                            pstmt.close();
							pstmt = null;
							rs.close();
							rs = null;

						}
						
						if(childNodeName.equalsIgnoreCase("tax_class"))
						{
							
							taxClass = genericUtility.getColumnValue("tax_class", dom);
							taxClass = taxClass == null ? "" : taxClass.trim();
							
							
							System.out.println(":::tax class" + taxClass);
							int count = 0;
							// Changed By Nasruddin [16-sep-16]
							if( taxClass != null && taxClass.trim().length() > 0)
							{
								sql = "select count(*) as count from taxclass where tax_class = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, taxClass);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count = rs.getInt("count");
								}

								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;
								if(count == 0)
								{
									errString = itmdbAccessEJB.getErrorString("tax_class", "VTTCLASS1", userId);
									return errString;
								}
							}
						}
						
						if(childNodeName.equalsIgnoreCase("tax_env"))
						{

							taxEnv = genericUtility.getColumnValue("tax_env", dom);
							taxEnv = taxEnv == null ? "" : taxEnv.trim();

							System.out.println(":::tax env" + taxEnv);

							int count = 0;
							
							//Changed By Nasruddin [16-sep-16]
							if( taxEnv != null && taxEnv.trim().length() > 0)
							{
								sql = "select count(*) as count from taxenv where tax_env = ? ";
								pstmt = conn.prepareStatement(sql);
								pstmt.setString(1, taxEnv);
								rs = pstmt.executeQuery();
								if(rs.next())
								{
									count = rs.getInt("count");
								}

								pstmt.close();
								pstmt = null;
								rs.close();
								rs = null;
								if(count == 0)
								{
									errString = itmdbAccessEJB.getErrorString("tax_env", "VTTAXENV1", userId);
									return errString;
								}
							}
						}
					}
				}
			}catch(Exception e){
				System.out.println(":::" + this.getClass().getSimpleName() + ":::" + e.getMessage());
				e.printStackTrace();
            }
            
            //added by monika salla 0n 31 dec 2020 to remove dirty connection----
            finally
		{
			try
			{
				if (conn != null)
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

					conn.close();
				}
				conn = null;
			}
			catch (Exception d)
			{
				d.printStackTrace();
				throw new ITMException(d);
			}
		}
			return errString;
	  }
}


