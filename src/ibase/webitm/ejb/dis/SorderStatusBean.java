package ibase.webitm.ejb.dis;
import ibase.planner.utility.ITMException;
import ibase.system.config.AppConnectParm;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;

import java.io.File;
import javax.naming.InitialContext;


public class SorderStatusBean
{
	E12GenericUtility genericUtility = new E12GenericUtility();
	private String user_lang ="en"; 
	private String user_country = "US";
	private String user_status = "V";
	
	//Added by sarita to add userInfo on 8JAN2018
	//public String SorderStatusInfo(String saleOrder,String ref_series) throws ITMException
	public String SorderStatusInfo(String saleOrder,String ref_series,UserInfoBean userInfo) throws ITMException
	{
		//Modified by Anjali R. on[04/10/2018][Start]
		//SorderStatusEJBLocal sorderStatusEJB = null;
		SorderStatusEJB sorderStatusEJB = null;
		//Modified by Anjali R. on[04/10/2018][End]
		String getXmlData = "";
		String xslFileName = "",finalString = "";
		try
		{
			System.out.println("Inside SorderStatusBean class");
			System.out.println("Value of Sale order is 1234556::"+saleOrder +"ref_series ::"+ref_series);
			
			//InitialContext ctx = null;
	        AppConnectParm appConnect = new AppConnectParm();
	        InitialContext ctx = new InitialContext(appConnect.getProperty()); 
	        
	        //Modified by Anjali R. on[04/10/2018][Start]
			//sorderStatusEJB = (SorderStatusEJBLocal)ctx.lookup("ibase/SorderStatusEJB/local");
	        sorderStatusEJB = new SorderStatusEJB();
	        //Modified by Anjali R. on[04/10/2018][End]
	        
			//Added by sarita to add userInfo on 8JAN2018
			//getXmlData = sorderStatusEJB.getSorderStatusXML(saleOrder,ref_series);
			
			//Modified by Anjali R. on[04/10/2018][Start]
			getXmlData = sorderStatusEJB.getSorderStatusXML(saleOrder,ref_series,userInfo);
			//Modified by Anjali R. on[04/10/2018][End]
			System.out.println("Returned XML is ::::"+getXmlData);
			
			xslFileName = getXSLFileName( "sorder_status11_" + this.user_lang + "_" + this.user_country + "_" + this.user_status +".xsl" );	
			finalString = (genericUtility).transformToString(xslFileName, getXmlData, CommonConstants.APPLICATION_CONTEXT + File.separator + "temp", "Output", ".html");
		}
		catch(Exception e)
		{
			System.out.println("Exception Inside SorderStatusBean SorderStatusInfo() method ::"+e.getMessage());
			throw new ITMException(e);
		}
		System.out.println("finalString is >>>>>>>>>>>"+finalString);
		return finalString;
	}//end of method SorderStatusInfo
	
	private String getXSLFileName( String xslFileName )throws ITMException
	{   
		String retFileName = null;
		try
		{
			String defaultPath = null;
			if( CommonConstants.APPLICATION_CONTEXT != null )
			{
				defaultPath = CommonConstants.APPLICATION_CONTEXT + CommonConstants.ITM_CONTEXT + File.separator;
			}
			else
			{
				defaultPath = ".." + File.separator + "webapps" + File.separator + "ibase" + File.separator + CommonConstants.ITM_CONTEXT + File.separator;
			}
			//File xslPath = new File( defaultPath + File.separator  + "xsl" + File.separator + CommonConstants.THEME + File.separator + "WIZARD");
			//File xslPath = new File( defaultPath + File.separator  + "xsl" + File.separator + CommonConstants.THEME + File.separator + "WIZARD" + File.separator + "Galaxy");
			File xslPath = new File( defaultPath + File.separator  + "xsl" + File.separator + CommonConstants.THEME);
			if ( !xslPath.exists() )
			{
				xslPath.mkdir();
			}
			System.out.println( " 1xslPath [" + xslPath +"] xslFileName ["+xslFileName +"]");
			File xslFile = new File(xslPath , xslFileName);
			if( xslFile.exists() )
			{
				retFileName = xslFile.getAbsolutePath();
			}
			else
			{
				throw new ITMException( new Exception( retFileName + " Wizard XSL file Not Found") );	
			}
		}
		catch (Exception e)
		{
			throw new ITMException(e);
		}
		return retFileName;
	}//end of method getXSLFileName

}