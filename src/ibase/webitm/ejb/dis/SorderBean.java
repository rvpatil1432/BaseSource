package ibase.webitm.ejb.dis;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
/*
 * Request Id:D15JSUN018
 * Developer : Mahendra Jadhav
 * date : 21-01-2015
 * */
import java.util.Date;

public class SorderBean implements Serializable {
	
	
	private String saleOrder = null;
	private String priceListClg = null;
	private String priceList = null;
	private String custCode=null;
	private String siteCodeShip=null;
	private String status=null;
	private String locGroup = null;
	private String confirmed=null;
	private String partQty = null;
	private String orderType = null;
	private Date dueDate=null;
	private String siteCode = null;
	private String priceListDisc = null;
	private Timestamp plDate = null;
	private Timestamp orderdate = null;
	private String stateCodeDlv=null;
	private String countCodeDlv=null;
	
	boolean isRejected=false;
	private ArrayList<SordItemBean> sorditemList=new ArrayList<SordItemBean>();
	
	public void setSorditemList(ArrayList sorditemList)
	{
		this.sorditemList=sorditemList;
	}
	public ArrayList<SordItemBean> getSorditemList()
	{
		return this.sorditemList;
	}
	
		
	public String getSaleOrder() {
		return saleOrder;
	}
	public void setSaleOrder(String saleOrder) {
		this.saleOrder = saleOrder;
	}
	public String getPriceListClg() {
		return priceListClg;
	}
	public void setPriceListClg(String priceListClg) {
		this.priceListClg = priceListClg;
	}
	public String getPriceList() {
		return priceList;
	}
	public void setPriceList(String priceList) {
		this.priceList = priceList;
	}
	public String getCustCode() {
		return custCode;
	}
	public void setCustCode(String custCode) {
		this.custCode = custCode;
	}
	public String getSiteCodeShip() {
		return siteCodeShip;
	}
	public void setSiteCodeShip(String siteCodeShip) {
		this.siteCodeShip = siteCodeShip;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getConfirmed() {
		return confirmed;
	}
	public void setConfirmed(String confirmed) {
		this.confirmed = confirmed;
	}
	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	
	public boolean isRejected() {
		return isRejected;
	}
	public void setRejected(boolean isRejected) {
		this.isRejected = isRejected;
	}
	public String getPartQty() {
		return partQty;
	}
	public void setPartQty(String partQty) {
		this.partQty = partQty;
	}
	public String getLocGroup() {
		return locGroup;
	}
	public void setLocGroup(String locGroup) {
		this.locGroup = locGroup;
	}
	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	public String getSiteCode() {
		return siteCode;
	}
	public void setSiteCode(String siteCode) {
		this.siteCode = siteCode;
	}
	public String getPriceListDisc() {
		return priceListDisc;
	}
	public void setPriceListDisc(String priceListDisc) {
		this.priceListDisc = priceListDisc;
	}
	public Timestamp getPlDate() {
		return plDate;
	}
	public void setPlDate(Timestamp plDate) {
		this.plDate = plDate;
	}
	public Timestamp getOrderdate() {
		return orderdate;
	}
	public void setOrderdate(Timestamp orderdate) {
		this.orderdate = orderdate;
	}
	public String getStateCodeDlv() {
		return stateCodeDlv;
	}
	public void setStateCodeDlv(String stateCodeDlv) {
		this.stateCodeDlv = stateCodeDlv;
	}
	public String getCountCodeDlv() {
		return countCodeDlv;
	}
	public void setCountCodeDlv(String countCodeDlv) {
		this.countCodeDlv = countCodeDlv;
	}
	
	
	
	

}
