/********************************************************
Title : DistOrderClubBean
Date  : 27/03/12
Author: Chandni Shah

********************************************************/

package ibase.webitm.ejb.dis;

import java.io.Serializable;

public class DistOrderClubBean{
//private static final long serialVersionUID = 42L;

private String siteCode;
private String tranType;
private String availableYn;
private String deptCode;
private int lineNo;
private String unit;
private String unitAlt;
private String itemCode;
private double qtyConfirm;
private double qtyShipped;
private double discount;
private String distOrdNo;
private String rateFmDistOrd;
private String rateClgFmDistOrd;

//private Class javaType;

public String getSiteCode() {
	return siteCode;
}
public void setSiteCode(String siteCode) {
	this.siteCode = siteCode;
}
public String getTranType() {
	return tranType;
}
public void setTranType(String tranType) {
	this.tranType = tranType;
}
public String getAvailableYn() {
	return availableYn;
}
public void setAvailableYn(String availableYn) {
	this.availableYn = availableYn;
}
public String getDeptCode() {
	return deptCode;
}
public void setDeptCode(String deptCode) {
	this.deptCode = deptCode;
}

public int getLineNo() {
	return lineNo;
}
public void setLineNo(int lineNo) {
	this.lineNo = lineNo;
}
public String getUnit() {
	return unit;
}
public void setUnit(String unit) {
	this.unit = unit;
}
public String getUnitAlt() {
	return unitAlt;
}
public void setUnitAlt(String unitAlt) {
	this.unitAlt = unitAlt;
}
public String getItemCode() {
	return itemCode;
}
public void setItemCode(String itemCode) {
	this.itemCode = itemCode;
}
public double getQtyConfirm() {
	return qtyConfirm;
}
public void setQtyConfirm(double qtyConfirm) {
	this.qtyConfirm = qtyConfirm;
}
public double getQtyShipped() {
	return qtyShipped;
}
public void setQtyShipped(double qtyShipped) {
	this.qtyShipped = qtyShipped;
}
public double getDiscount() {
	return discount;
}
public void setDiscount(double discount) {
	this.discount = discount;
}
public String getDistOrdNo() {
	return distOrdNo;
}
public void setDistOrdNo(String distOrdNo) {
	this.distOrdNo = distOrdNo;
}
public String getRateFmDistOrd() {
	return rateFmDistOrd;
}
public void setRateFmDistOrd(String rateFmDistOrd) {
	this.rateFmDistOrd = rateFmDistOrd;
}
//private String rateClgFmDistOrd;

public String getRateClgFmDistOrd() {
	return rateClgFmDistOrd;
}
public void setRateClgFmDistOrd(String rateClgFmDistOrd) {
	this.rateClgFmDistOrd = rateClgFmDistOrd;
}

}