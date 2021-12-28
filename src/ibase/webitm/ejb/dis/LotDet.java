
/*win_name=w_dist_order(d_dist_order_edit/d_distorderdet_brow)
Name :- BaseInfo Pvt Ltd.
Modification:-
		Reason						Date[Like 05052007 all modified code should contain this so that search easier]

1-

2-

3-					
*/

package ibase.webitm.ejb.dis;
import java.sql.Timestamp;



class LotDet
{
	
	private String lotNo;
	private String lotSl;
	private double qty ;
	private double allocQty;
	private int art ;

	public void setLotNo(String  lotNo)
	{
		this.lotNo = lotNo;
	}
	public String getLotNo()
	{
		return this.lotNo;
	}
	public void setLotSl(String  lotSl)
	{
		this.lotSl = lotSl;
	}
	public String getLotSl()
	{
		return this.lotSl;
	}
	
	public void setQty(double  qty)
	{
		this.qty = qty;
	}
	public double getQty()
	{
		return this.qty;
	}
	public void setAllocQty(double  allocQty)
	{
		this.allocQty = allocQty;
	}
	public double getAllocQty()
	{
		return this.allocQty;
	}
	
	public void setNoArt(int  art)
	{
		this.art = art;
	}
	public int getNoArt()
	{
		return this.art;
	}


	
}