package ibase.webitm.ejb.dis.adv;


public class ItemDetails{
	private int counter=1;
	private int itemNo=0;
	private String productName;
	private String productDesc;
	private int hsnCode;
	private int quantity;
	private String qtyUnit;
	private double taxableAmount;
	private double sgstRate;
	private double cgstRate;
	private double igstRate;
	private double cessRate;
	private double cessNonAdvol;
	public ItemDetails(int counter,String productName, String productDesc, int hsnCode, int quantity,
			String qtyUnit, double taxableAmount, double sgstRate, double cgstRate, double igstRate,
			double cessRate, double cessNonAdvol) {
		super();
		this.itemNo = counter;
		this.productName = productName;
		this.productDesc = productDesc;
		this.hsnCode = hsnCode;
		this.quantity = quantity;
		this.qtyUnit = qtyUnit;
		this.taxableAmount = taxableAmount;
		this.sgstRate = sgstRate;
		this.cgstRate = cgstRate;
		this.igstRate = igstRate;
		this.cessRate = cessRate;
		this.cessNonAdvol = cessNonAdvol;
	}
	public int getItemNo() {
		return itemNo;
	}
	public void setItemNo(int itemNo) {
		this.itemNo = itemNo;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getProductDesc() {
		return productDesc;
	}
	public void setProductDesc(String productDesc) {
		this.productDesc = productDesc;
	}
	public int getHsnCode() {
		return hsnCode;
	}
	public void setHsnCode(int hsnCode) {
		this.hsnCode = hsnCode;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public String getQtyUnit() {
		return qtyUnit;
	}
	public void setQtyUnit(String qtyUnit) {
		this.qtyUnit = qtyUnit;
	}
	public double getTaxableAmount() {
		return taxableAmount;
	}
	public void setTaxableAmount(double taxableAmount) {
		this.taxableAmount = taxableAmount;
	}
	public double getSgstRate() {
		return sgstRate;
	}
	public void setSgstRate(double sgstRate) {
		this.sgstRate = sgstRate;
	}
	public double getCgstRate() {
		return cgstRate;
	}
	public void setCgstRate(double cgstRate) {
		this.cgstRate = cgstRate;
	}
	public double getIgstRate() {
		return igstRate;
	}
	public void setIgstRate(double igstRate) {
		this.igstRate = igstRate;
	}
	public double getCessRate() {
		return cessRate;
	}
	public void setCessRate(double cessRate) {
		this.cessRate = cessRate;
	}
	public double getCessNonAdvol() {
		return cessNonAdvol;
	}
	public void setCessNonAdvol(double cessNonAdvol) {
		this.cessNonAdvol = cessNonAdvol;
	}		
}
