package ibase.webitm.ejb.dis;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * Request Id:D15JSUN018
 * Developer : Mahendra Jadhav
 * date : 21-01-2015
 * */
public class SordItemBean implements Serializable
{
	
	
	
	private String saleOrder = null;
	private String lineNo = null;
	private String itemCode = null;
	private String itemCodeOrd = null;
	private String siteCode = null;
	private String unit = null;
	private String status = null;
	private String expLev = null;
	private String itemRef = null;
	//String minShelfLife = null;
	private String unitRef = null;
	private String itemFlag = null;
	//String qtyAlloc = null;
	private String nature = null;
	private String unitStd=null;
	private String itemCodeParent=null;
	private String skipline=null;
	private String skiplot = null;
	
//	private String rateClg = null;
	//private String stockOpt=null;
	private boolean isRejected=false;
	private double quantityStduom=0.0;
	private double convQtyStduom=0.0;
	private double minShelfLife = 0.0;
	private double maxShelfLife = 0.0;
	private double qtyAlloc=0.0;
	private double qtyDesp = 0.0;
	private double quantity = 0.0;
	private double siteItmQty = 0.0;
	private double sordItmQty = 0.0;
	private double rate  = 0.0;
	private double rateClg = 0.0;
	
	private int stockOpt=0;
	private ArrayList<StockBean> stockList=new ArrayList<StockBean>();
	
	private SorderBean sorderBean;
	
	
	
	
	public void setStockList(ArrayList stockList)
	{
		this.stockList=stockList;
	}
	public ArrayList<StockBean> getStockList()
	{
		return this.stockList;
	}
	
	public String getUnitStd() {
		return unitStd;
	}
	public void setUnitStd(String unitStd) {
		this.unitStd = unitStd;
	}
	public double getQuantityStduom() {
		return quantityStduom;
	}
	public void setQuantityStduom(double quantityStduom) {
		this.quantityStduom = quantityStduom;
	}
	public double getConvQtyStduom() {
		return convQtyStduom;
	}
	public void setConvQtyStduom(double convQtyStduom) {
		this.convQtyStduom = convQtyStduom;
	}
	public boolean isRejected() {
		return isRejected;
	}
	public void setRejected(boolean isRejected) {
		this.isRejected = isRejected;
	}
	public String getLineNo() {
		return lineNo;
	}
	public void setLineNo(String lineNo) {
		this.lineNo = lineNo;
	}
	
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getExpLev() {
		return expLev;
	}
	public void setExpLev(String expLev) {
		this.expLev = expLev;
	}
	public String getItemRef() {
		return itemRef;
	}
	public void setItemRef(String itemRef) {
		this.itemRef = itemRef;
	}
	/*public String getMinShelfLife() {
		return minShelfLife;
	}
	public void setMinShelfLife(String minShelfLife) {
		this.minShelfLife = minShelfLife;
	}*/
	public String getUnitRef() {
		return unitRef;
	}
	public void setUnitRef(String unitRef) {
		this.unitRef = unitRef;
	}
	public String getItemFlag() {
		return itemFlag;
	}
	public void setItemFlag(String itemFlag) {
		this.itemFlag = itemFlag;
	}
	/*public String getQtyAlloc() {
		return qtyAlloc;
	}
	public void setQtyAlloc(String qtyAlloc) {
		this.qtyAlloc = qtyAlloc;
	}*/
	public String getNature() {
		return nature;
	}
	public void setNature(String nature) {
		this.nature = nature;
	}
	public String getItemCode() {
		return itemCode;
	}
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}
	public String getItemCodeOrd() {
		return itemCodeOrd;
	}
	public void setItemCodeOrd(String itemCodeOrd) {
		this.itemCodeOrd = itemCodeOrd;
	}
	public String getSiteCode() {
		return siteCode;
	}
	public void setSiteCode(String siteCode) {
		this.siteCode = siteCode;
	}
	public double getMinShelfLife() {
		return minShelfLife;
	}
	public void setMinShelfLife(double minShelfLife) {
		this.minShelfLife = minShelfLife;
	}
	public double getQtyAlloc() {
		return qtyAlloc;
	}
	public void setQtyAlloc(double qtyAlloc) {
		this.qtyAlloc = qtyAlloc;
	}
	public double getQtyDesp() {
		return qtyDesp;
	}
	public void setQtyDesp(double qtyDesp) {
		this.qtyDesp = qtyDesp;
	}
	public double getQuantity() {
		return quantity;
	}
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}
	public double getSiteItmQty() {
		return siteItmQty;
	}
	public void setSiteItmQty(double siteItmQty) {
		this.siteItmQty = siteItmQty;
	}
	/*public String getStockOpt() {
		return stockOpt;
	}
	public void setStockOpt(String stockOpt) {
		this.stockOpt = stockOpt;
	}*/
	public SorderBean getSorderBean() {
		return sorderBean;
	}
	public void setSorderBean(SorderBean sorderBean) {
		this.sorderBean = sorderBean;
	}
	public String getSaleOrder() {
		return saleOrder;
	}
	public void setSaleOrder(String saleOrder) {
		this.saleOrder = saleOrder;
	}
	public int getStockOpt() {
		return stockOpt;
	}
	public void setStockOpt(int stockOpt) {
		this.stockOpt = stockOpt;
	}
	public double getSordItmQty() {
		return sordItmQty;
	}
	public void setSordItmQty(double sordItmQty) {
		this.sordItmQty = sordItmQty;
	}
	public double getMaxShelfLife() {
		return maxShelfLife;
	}
	public void setMaxShelfLife(double maxShelfLife) {
		this.maxShelfLife = maxShelfLife;
	}
	public double getRate() {
		return rate;
	}
	public void setRate(double rate) {
		this.rate = rate;
	}
	public double getRateClg() {
		return rateClg;
	}
	public void setRateClg(double rateClg) {
		this.rateClg = rateClg;
	}
	public String getItemCodeParent() {
		return itemCodeParent;
	}
	public void setItemCodeParent(String itemCodeParent) {
		this.itemCodeParent = itemCodeParent;
	}
	public String getSkipline() {
		return skipline;
	}
	public void setSkipline(String skipline) {
		this.skipline = skipline;
	}
	public String getSkiplot() {
		return skiplot;
	}
	public void setSkiplot(String skiplot) {
		this.skiplot = skiplot;
	}
	
	
	
	
	

}
