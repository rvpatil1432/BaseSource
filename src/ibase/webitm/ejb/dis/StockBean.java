package ibase.webitm.ejb.dis;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public class StockBean implements Serializable {
	
	
	private String lotNo=null;
	private String lotSl=null;
	private String grade=null;
	private String siteCodeMfg=null;
	private String locCode=null;
	private String skipline = null;
	private String skiplot = null;
	private Timestamp expDate = null;
	private Timestamp mfgDate = null;
	private double stockQty =0.0;
	private double allocQty =0.0;
	private double holdQty =0.0;
	private double stockQtyTot = 0.0;
	private boolean isRejected=false;
	private SordItemBean sordItemBean;
	private int errorCnt=0;
	
	private double allocQtyUpd = 0.0;	
	public double getAllocQtyUpd() {
		return allocQtyUpd;
	}
	public void setAllocQtyUpd(double allocQtyUpd) {
		this.allocQtyUpd = allocQtyUpd;
	}
	
	public String getLotNo() {
		return lotNo;
	}
	public void setLotNo(String lotNo) {
		this.lotNo = lotNo;
	}
	public String getLotSl() {
		return lotSl;
	}
	public void setLotSl(String lotSl) {
		this.lotSl = lotSl;
	}
	public String getGrade() {
		return grade;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}
	public String getSiteCodeMfg() {
		return siteCodeMfg;
	}
	public void setSiteCodeMfg(String siteCodeMfg) {
		this.siteCodeMfg = siteCodeMfg;
	}
	public String getLocCode() {
		return locCode;
	}
	public void setLocCode(String locCode) {
		this.locCode = locCode;
	}
	
/*	public double getQuantity() {
		return quantity;
	}
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}*/
	
	
	public double getAllocQty() {
		return allocQty;
	}
	public void setAllocQty(double allocQty) {
		this.allocQty = allocQty;
	}
	public double getHoldQty() {
		return holdQty;
	}
	public void setHoldQty(double holdQty) {
		this.holdQty = holdQty;
	}
	
	public SordItemBean getSordItemBean() {
		return sordItemBean;
	}
	public void setSordItemBean(SordItemBean sordItemBean) {
		this.sordItemBean = sordItemBean;
	}
	
	public boolean isRejected() {
		return isRejected;
	}
	public void setRejected(boolean isRejected) {
		this.isRejected = isRejected;
	}
	public Timestamp getExpDate() {
		return expDate;
	}
	public void setExpDate(Timestamp expDate) {
		this.expDate = expDate;
	}
	public Timestamp getMfgDate() {
		return mfgDate;
	}
	public void setMfgDate(Timestamp mfgDate) {
		this.mfgDate = mfgDate;
	}
	public double getStockQtyTot() {
		return stockQtyTot;
	}
	public void setStockQtyTot(double stockQtyTot) {
		this.stockQtyTot = stockQtyTot;
	}
	public double getStockQty() {
		return stockQty;
	}
	public void setStockQty(double stockQty) {
		this.stockQty = stockQty;
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
	public int getErrorCnt() {
		return errorCnt;
	}
	public void setErrorCnt(int errorCnt) {
		this.errorCnt = errorCnt;
	}
	
	
	
	
	
	
	
	
	
	
	
	

}
