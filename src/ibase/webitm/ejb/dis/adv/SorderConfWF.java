/********************************************************
	Title : SorderConfWF[D16EBAS004]
	Date  : 25/08/16
	Developer: Aniket
	
 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import ibase.utility.UserInfoBean;
import ibase.webitm.utility.ITMException;
import java.rmi.RemoteException;
import ibase.webitm.ejb.dis.adv.SorderConf;
import javax.ejb.Stateless;


@Stateless
public class SorderConfWF implements SorderConfWFRemote, SorderConfWFLocal {

	public String confirm(String saleOrder, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException, ITMException {
		System.out.println("SOrderConfWF confirm method called>>>>>>>>>>>>>>>>>>>");
		String retString = "N", errString = "";
		SorderConf sorderConf = null;
		
		try {
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
			sorderConf = new SorderConf();
			sorderConf.setUserInfo(userInfo);
			errString = sorderConf.confirm(saleOrder, xtraParams, forcedFlag);
			
			if(errString != null && errString.trim().length() > 0)
			{
				if(errString.indexOf("VTCNFSUCC") > -1)
				{
					retString = "Y";
					System.out.println("@@@@ SOrder Workflow Transaction Confirmed... ");
				}
				else
				{
					System.out.println("@@@@ SOrder Workflow Error Occured... ");
				}
			}
		} catch (Exception e) {
			System.out.println("Exception Occured in SOrderConfWF confirm");
			e.printStackTrace();
		}
		System.out.println("returnString>>>> "+retString);
		return retString;
	}
}
