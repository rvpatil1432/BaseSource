package ibase.webitm.ejb.dis.adv;

import java.util.List;

public class EwayBillJson {

	String version;
	List<EwayBillValue> billLists;
	public EwayBillJson(String version, List<EwayBillValue> billLists) {
		super();
		this.version = version;
		this.billLists = billLists;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public List<EwayBillValue> getBillLists() {
		return billLists;
	}
	public void setBillLists(List<EwayBillValue> billLists) {
		this.billLists = billLists;
	}
}
