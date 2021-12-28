package ibase.webitm.ejb.dis;

import java.util.*;

public class MaterialtransferBean implements java.io.Serializable
{
		private String itemCode ;
		private double  demandQty;
		private double  suppQty ;
		private double  balQty ;
		private HashMap siteQtyMap = new HashMap();
		//private ArrayList stockList = new ArrayList();

		public MaterialtransferBean()
		{
		}
		public String getItemCode()
		{
			return this.itemCode;
		}
		public void setItemCode(String itemCode)
		{
			this.itemCode = itemCode;
		}
		public double getBalQty()
		{
			return this.balQty;
		}
		public void setBalQty(double balQty)
		{
			this.balQty =  balQty;
		}
		public double getDemandQty()
		{
			return this.demandQty;
		}
		public void setDemandQty(double demandQty)
		{
			this.demandQty +=  demandQty;
			setBalQty(this.demandQty - this.suppQty);
		}
		public double getSuppQty()
		{
			return this.suppQty;
		}
		public void setSuppQty(double suppQty)
		{
			this.suppQty = suppQty;
		}

		public HashMap getSiteQtyMap()
		{
			return this.siteQtyMap;
		}
		public void setSiteQtyMap(HashMap siteQtyMap)
		{
			this.siteQtyMap = siteQtyMap;
		}
		/*public void setStockList(HashMap stockMap)
		{
			String itemCode = null;
			String locCode = null;
			String lotNo = null;
			String lotSl = null;
			double quantity = 0;
			double oldQuantity = 0;
			if (this.stockList.size() > 0)
			{
				int index = 0;
				for (index = 0; index < this.stockList.size(); index++ )
				{
				}

			}
			else
			{
				HashMap stockMap = new HashMap();

			}
		}
		public HashMap getStockList()
		{
			return this.stockList ;
		}*/
}