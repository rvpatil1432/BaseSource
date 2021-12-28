package ibase.webitm.ejb.dis;

import java.util.*;

class CustomerListBean
{
	
    
    private String custName;  
    private String custCode;  
    private String siteCode; 
       
    ArrayList custRecordList = new ArrayList();
   
    public void setCustName(String custName)
	{
		this.custName = custName;
	}
	public String getCustName()
	{
		return this.custName;
	}
	
	
	public void setCustCode(String custCode)
	{
		this.custCode = custCode;
	}
	public String getCustCode()
	{
		return this.custCode;
	}
	
	public void setSiteCode(String siteCode)
	{
		this.siteCode = siteCode;
	}
	public String getSiteCode()
	{
		return this.siteCode;
	}
	
	public void setCustRecordList(CustomerBean customerListBean) 
	{
			this.custRecordList.add(customerListBean);
	}

	public ArrayList getCustRecordList()  
	{
		return custRecordList;
	}
	
}