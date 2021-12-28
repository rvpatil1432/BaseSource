/*
Name :- BaseInfo Pvt Ltd.
Modification:-
		Reason						Date[Like 05052007 all modified code should contain this so that search easier]

1-

2-

3-					
*/

package ibase.webitm.ejb.dis;
import java.sql.Timestamp;
import java.util.ArrayList;



class StockItem
{
	
	private String itemCode;
	private double totAmt;
	private int distLineno;
	private String  itemSer;
	private String unit;
	private String locCode ;
	private String siteCode ;
	
	ArrayList lotWiseList  = new ArrayList();
	
	public void setLotWiseList(LotDet lotDet) 
	{
			this.lotWiseList.add(lotDet);
	}

	public ArrayList getLotWiseList()  
	{ 
		return lotWiseList;
	}
	public void setSiteCode(String  siteCode)
	{
		this.siteCode = siteCode;
	}
	public String getSiteCode()
	{
		return this.siteCode;
	}
   
   
   	public void setLocCode(String  locCode)
	{
		this.locCode = locCode;
	}
	public String getLocCode()
	{
		return this.locCode;
	}
	

	public void setItemCode(String  itemCode)
	{
		this.itemCode = itemCode;
	}
	public String getItemCode()
	{
		return this.itemCode;
	}
	public void setTotAmt(double  totAmt)
	{
		this.totAmt = totAmt;
	}
	public double getTotAmt()
	{
		return this.totAmt;
	}
	public void setDistLineno(int  distLineno)
	{
		this.distLineno = distLineno;
	}
	public int getDistLineno()
	{
		return this.distLineno;
	}

	public void setItemSer(String  itemSer)
	{
		this.itemSer = itemSer;
	}
	public String getItemSer()
	{
		return this.itemSer;
	}
	public void setUnit(String  unit)
	{
		this.unit = unit;
	}
	public String  getUnit()
	{
		return this.unit;
	}
	
	
	
}