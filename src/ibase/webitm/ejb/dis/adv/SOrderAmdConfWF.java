/********************************************************
	Title : SOrderAmdConfWF[D16EBAS004]
	Date  : 25/08/16
	Developer: Aniket
	
 ********************************************************/
package ibase.webitm.ejb.dis.adv;

import ibase.webitm.utility.ITMException;
import ibase.utility.UserInfoBean;
import java.rmi.RemoteException;
import javax.ejb.Stateless;
import ibase.webitm.ejb.dis.adv.SOrderAmdConf;


@Stateless
public class SOrderAmdConfWF implements SOrderAmdConfWFRemote, SOrderAmdConfWFLocal {
   
	public String confirm(String saleOrderAmd, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException, ITMException {
		System.out.println("SOrderAmdConfWF confirm method called>>>>>>>>>>>>>>>>>>>");
		String retString = "N", errString = "";
		SOrderAmdConf sOrdAmdConf = null;
		
		try {
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
			sOrdAmdConf = new SOrderAmdConf();
			sOrdAmdConf.setUserInfo(userInfo);
			errString = sOrdAmdConf.confirm(saleOrderAmd, xtraParams, forcedFlag);
			if(errString != null && errString.trim().length() > 0)
			{
				if(errString.indexOf("VTCNFSUCC") > -1)
				{
					retString = "Y";
					System.out.println("@@@@ SOrderAmd Workflow Transaction Confirmed... ");
				}
				else
				{
					System.out.println("@@@@ SOrderAmd Workflow Error Occured... ");
				}
			}
		} catch (Exception e) {
			System.out.println("Exception Occured in SOrderAmdConfWF confirm");
			e.printStackTrace();
		}
		System.out.println("returnString>>>> "+retString);
		return retString;
	}
	}
