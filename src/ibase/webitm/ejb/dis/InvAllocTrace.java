package ibase.webitm.ejb.dis;

import java.util.*;

public class InvAllocTrace   
{
 private String tranId=null;
 private java.sql.Date tranDate = new java.sql.Date(System.currentTimeMillis());
 private String refSer=null;
 private String refId=null;
 private String refLine=null;
 private String siteCode=null;
 private String itemCode=null;
 private String locCode=null;
 private String lotNo=null;
 private String lotSl=null;
 private double allocQty = 0.0;
 private String chgUser = null;
 private String chgTerm = null;
 private String chgWin = null;
 private java.sql.Date chgDate = null;

	public void setTranId(String tranId)
	{
		this.tranId=tranId;
	}
	public String getTranId()
	{
		return this.tranId;
	}

	public java.sql.Date getTranDate()
	{
		return this.tranDate;
	}
	public void setRefSer(String refSer)
	{
		this.refSer=refSer;
	}
	public String getRefSer()
	{
		return this.refSer;
	}
	public void setRefId(String refId)
	{
		this.refId=refId;
	}
	public String getRefId()
	{
		return this.refId;
	}
	public void setRefLine(String refLine)
	{
		this.refLine=refLine;
	}
	public String getRefLine()
	{
		return this.refLine;
	}
	public void setSiteCode(String siteCode)
	{
		this.siteCode=siteCode;
	}
	public String getSiteCode()
	{
		return this.siteCode;
	}
	public void setItemCode(String itemCode)
	{
		this.itemCode=itemCode;
	}
	public String getItemCode()
	{
		return this.itemCode;
	}
	public void setLocCode(String locCode)
	{
		this.locCode=locCode;
	}
	public String getLocCode()
	{
		return this.locCode;
	}
	public void setLotNo(String lotNo)
	{
		this.lotNo=lotNo;
	}
	public String getLotNo()
	{
		return this.lotNo;
	}
	public void setLotSl(String lotSl)
	{
		this.lotSl=lotSl;
	}
	public String getLotSl()
	{
		return this.lotSl;
	}
	public void setAllocQty(double allocQty)
	{
		this.allocQty=allocQty;
	}
	public double getAllocQty()
	{
		return this.allocQty;
	}
	public void setChgUser(String chgUser)
	{
		this.chgUser=chgUser;
	}
	public String getChgUser()
	{
		return this.chgUser;
	}
	public void setChgTerm(String chgTerm)
	{
		this.chgTerm=chgTerm;
	}
	public String getChgTerm()
	{
		return this.chgTerm;
	}
	public void setChgWin(String chgWin)
	{
		this.chgWin=chgWin;
	}
	public String getChgWin()
	{
		return this.chgWin;
	}
	public void setChgDate(java.sql.Date chgDate)
	{
		this.chgDate=chgDate;
	}
	public java.sql.Date getChgDate()
	{
		return this.chgDate;
	}
}