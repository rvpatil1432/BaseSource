/********************************************************
	Title : PorderConfWF[D16EBAS004]
	Date  : 25/08/16
	Developer: Aniket
	
 ********************************************************/

package ibase.webitm.ejb.dis.adv;

import ibase.utility.UserInfoBean;
import ibase.webitm.utility.ITMException;

import java.rmi.RemoteException;

import javax.ejb.Stateless;

@Stateless
public class PorderConfWF implements PorderConfWFRemote, PorderConfWFLocal {

	public String confirm(String purcOrder, String xtraParams, String forcedFlag, String userInfoStr) throws RemoteException, ITMException 
	{
		System.out.println("PorderConfWF confirm method called>>>>>>>>>>>>>>>>>>>");
		String retString = "N", errString = "";
		PorderConf porderConf = null;
		
		try {
			UserInfoBean userInfo = new UserInfoBean(userInfoStr);
			porderConf = new PorderConf();
			porderConf.setUserInfo(userInfo);
			errString = porderConf.confirm(purcOrder, xtraParams, forcedFlag);
			
			if(errString != null && errString.trim().length() > 0)
			{
				if(errString.indexOf("VTCNFSUCC") > -1)
				{
					retString = "Y";
					System.out.println("@@@@ POrder Workflow Transaction Confirmed... ");
				}
				else
				{
					System.out.println("@@@@ POrder Workflow Error Occured... ");
				}
			}
		} catch (Exception e) {
			System.out.println("Exception Occured in PorderConfWF confirm");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 09/08/19
		}
		System.out.println("returnString>>>> "+retString);
		return retString;
	}
	
}

