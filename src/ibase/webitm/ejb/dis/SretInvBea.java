package ibase.webitm.ejb.dis;

import java.util.*;

class SretInvBean
{
	
    
    private String refSer;  
    private String refNo;   
    private double adjAmt;   
    private double refBalAmt;  
    
    public void setRefSer(String refSer)
	{
		this.refSer = refSer;
	}
	public String getRefSer()
	{
		return this.refSer;
	}
	public void setRefNo(String refNo)
	{
		this.refNo = refNo;
	}
	public String getRefNo()
	{
		return this.refNo;
	}
	
	public void setAdjAmt(double adjAmt)
	{
		this.adjAmt = adjAmt;
	}
	public double getAdjAmt()
	{
		return this.adjAmt;
	}
	public void setRefBalAmt(double refBalAmt)
	{
		this.refBalAmt = refBalAmt;
	}
	public double getRefBalAmt()
	{
		return this.refBalAmt;
	}
   	
	
}