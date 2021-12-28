package ibase.webitm.ejb.dis;


class CustomerBean
{
	DistCommon distCommonObj = new DistCommon();
	java.io.File filePtr = new java.io.File("C:\\pb10\\log\\pbnitrace.log");
    
	private String stockQuantity ;
	private String 	saleOrder;
	private String 	lineNo;
	private String 	itemCode;
	private String 	expLev ;
	private String frsBrowFlag;
	
	private String taxClass;
	private String taxChap;
	private String taxEnv;
	private String itemSer;
	
	
	private double 	allocQty =0;
	
    public void setStockQuantity(String stockQuantity)
	{
		this.stockQuantity = stockQuantity;
	}
	public String getStockQuantity()
	{
		return this.stockQuantity;
	}
	
	public void setSaleOrder(String saleOrder)
	{
		this.saleOrder = saleOrder;
	}
	public String getSaleOrder()
	{
		return this.saleOrder;
	}
	
	public void setLineNo(String lineNo)
	{
		this.lineNo = lineNo;
	}
	public String getLineNo()
	{
		return this.lineNo;
	}
	
	public void setItemCode(String itemCode)
	{
		this.itemCode = itemCode;
	}
	public String getItemCode()
	{
		return this.itemCode;
	}
	
	public void setExpLev(String expLev)
	{
		this.expLev = expLev;
	}
	public String getExpLev()
	{ 
		return this.expLev;
	}
	public void setFrsBrowFlag(String frsBrowFlag)
	{
		this.frsBrowFlag = frsBrowFlag;
	}
	public String getFrsBrowFlag()
	{
		return this.frsBrowFlag;
	}
	 public void setAllocQty(double allocQty)
	{
		this.allocQty = allocQty; 
	}
	public double getAllocQty()
	{
		return this.allocQty;
	}
	public void setTaxClass(String siteCode,String tranType,java.sql.Connection conn)
	{
		try
		{
				FrsDbtCreationPrc.writeLog(filePtr,"C a l c u l a t i n g Tax Class(Parameter) ",true);
				FrsDbtCreationPrc.writeLog(filePtr,"____________________________________________",true);
				FrsDbtCreationPrc.writeLog(filePtr,"siteCodeFrom:-["+siteCode+"]",true);
				FrsDbtCreationPrc.writeLog(filePtr,"siteCodeTo:-["+siteCode+"]",true);
				FrsDbtCreationPrc.writeLog(filePtr,"ItemCode:-["+siteCode+"]",true);
				FrsDbtCreationPrc.writeLog(filePtr,"tranType:-["+tranType+"]",true);
				FrsDbtCreationPrc.writeLog(filePtr,"itemSer:-["+itemSer+"]",true);
				this.taxClass = distCommonObj.setPlistTaxClassEnv(siteCode,siteCode,this.itemCode,tranType,this.itemSer,"TAX_CLASS",conn);
				if(this.taxClass==null)this.taxClass="";
				FrsDbtCreationPrc.writeLog(filePtr,"TaxEClass[After Calculating]:-["+taxClass+"]",true);
				FrsDbtCreationPrc.writeLog(filePtr,"____________________________________________",true);
		}	
		catch(Exception e)
		{
			FrsDbtCreationPrc.writeLog(filePtr,e,true);
		}
		
	}
	public String getTaxClass()
	{
		return this.taxClass;
	}
	
	public void setTaxChap(String siteCode,String tranType,java.sql.Connection conn)
	{
		try
		{
			
			this.taxChap = distCommonObj.setPlistTaxClassEnv(siteCode,siteCode,this.itemCode,tranType,this.itemSer,"",conn);
		}
		catch(Exception e){}
		
	}
	public String getTaxChap()
	{
		return this.taxChap; 
	}
	public void setTaxEnv(String siteCode,String tranType,java.sql.Connection conn)
	{
		try
		{
				FrsDbtCreationPrc.writeLog(filePtr,"C a l c u l a t i n g Tax Enviroment(Parameter) ",true);
				FrsDbtCreationPrc.writeLog(filePtr,"____________________________________________",true);
				FrsDbtCreationPrc.writeLog(filePtr,"siteCodeFrom:-["+siteCode+"]",true);
				FrsDbtCreationPrc.writeLog(filePtr,"siteCodeTo:-["+siteCode+"]",true);
				FrsDbtCreationPrc.writeLog(filePtr,"ItemCode:-["+siteCode+"]",true);
				FrsDbtCreationPrc.writeLog(filePtr,"tranType:-["+tranType+"]",true);
				FrsDbtCreationPrc.writeLog(filePtr,"itemSer:-["+itemSer+"]",true);
				
				this.taxEnv = distCommonObj.setPlistTaxClassEnv(siteCode,siteCode,this.itemCode,tranType,this.itemSer,"TAX_ENV",conn);
				if(this.taxEnv==null)this.taxEnv="";
				FrsDbtCreationPrc.writeLog(filePtr,"TaxEnviroment[After Calculating]:-["+taxEnv+"]",true);
				FrsDbtCreationPrc.writeLog(filePtr,"____________________________________________",true);
		}
		catch(Exception e)
		{
			FrsDbtCreationPrc.writeLog(filePtr,e,true);
		}
		
	}
	public String getTaxEnv()
	{
		return this.taxEnv;
	}
	public void setItemSer(java.sql.Connection conn)
	{
		String sql=null;
		try
		{
			java.sql.ResultSet rs = null;
			java.sql.PreparedStatement pstmt = null;
			sql = "SELECT ITEM_SER FROM ITEM WHERE ITEM_CODE='"+this.itemCode+"'";
			FrsDbtCreationPrc.writeLog(filePtr,"Item Series Sql:-["+sql+"]",true);
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				this.itemSer = (rs.getString(1)==null?"":rs.getString(1));
			}
			FrsDbtCreationPrc.writeLog(filePtr,"Item Series:-["+itemSer+"]",true);
			
		}
		catch(Exception e)
		{
			FrsDbtCreationPrc.writeLog(filePtr,e,true);
			e.printStackTrace();
		}
	}
	public String getItemSer()
	{
		return this.itemSer;
	}
	 
	 
	 
	
	
	
	
}