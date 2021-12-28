/**
 * 
 */
package ibase.webitm.ejb.dis;

import java.util.Date;

/**
 * @author base
 *
 */
public class ForecastBean 
{
	private String period, nextperiod, currperiod;
	private int cnt;
	private float percent;
	private Date fr_date;

	/**
	 * @return the period
	 */
	public String getPeriod() {
		return period;
	}

	/**
	 * @param period the period to set
	 */
	public void setPeriod(String period) {
		this.period = period;
	}

	/**
	 * @return the fr_date
	 */
	public Date getFr_date() {
		return fr_date;
	}

	/**
	 * @param fr_date the fr_date to set
	 */
	public void setFr_date(Date fr_date) {
		this.fr_date = fr_date;
	}

	/**
	 * @return the cnt
	 */
	public int getCnt() {
		return cnt;
	}

	/**
	 * @param cnt the cnt to set
	 */
	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	/**
	 * @return the nextperiod
	 */
	public String getNextperiod() {
		return nextperiod;
	}

	/**
	 * @param nextperiod the nextperiod to set
	 */
	public void setNextperiod(String nextperiod) {
		this.nextperiod = nextperiod;
	}

	/**
	 * @return the currperiod
	 */
	public String getCurrperiod() {
		return currperiod;
	}

	/**
	 * @param currperiod the currperiod to set
	 */
	public void setCurrperiod(String currperiod) {
		this.currperiod = currperiod;
	}

	/**
	 * @return the percent
	 */
	public float getPercent() {
		return percent;
	}

	/**
	 * @param percent the percent to set
	 */
	public void setPercent(float percent) {
		this.percent = percent;
	}
}
