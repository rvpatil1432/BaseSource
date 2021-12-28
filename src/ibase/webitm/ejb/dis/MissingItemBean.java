package ibase.webitm.ejb.dis;

import ibase.system.config.AppConnectParm;
import ibase.utility.UserInfoBean;

import ibase.webitm.utility.ITMException;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.InitialContext;

public class MissingItemBean implements Serializable{

	private String itemCode;
	private String itemDesc;
	private double itemQuantity;
	private String status;
	
	
	public double getItemQuantity() {
		return itemQuantity;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public void setItemQuantity(double totqty) {
		this.itemQuantity = totqty;
	}


	public String getItemCode() {
		return itemCode;
	}


	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}


	public String getItemDesc() {
		return itemDesc;
	}


	public void setItemDesc(String itemDesc) {
		this.itemDesc = itemDesc;
	}

}
