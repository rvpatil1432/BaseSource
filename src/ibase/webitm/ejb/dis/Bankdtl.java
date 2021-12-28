package ibase.webitm.ejb.dis;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement(name = "BankDtl")
public class Bankdtl implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String bankcode="";
	private String bankname="";
	
	public Bankdtl(){}
	
	public Bankdtl ( String bankCode, String bankName)
	{
		this.bankcode = bankCode;
		this.bankname = bankName;
		
	}
	public String getBankCode() {
		return bankcode;
	}
	@XmlElement
	
	public void setBankCode(String bankCode) {
		this.bankcode = bankCode;
	}
	public String getBankName() {
		return bankname;
	}
	@XmlElement
	
	public void setBankName(String bankName) {
		this.bankname = bankName;
	}
}
