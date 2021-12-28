package ibase.webitm.ejb.dis;


import ibase.system.config.AppConnectParm;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
//import ibase.webitm.ejb.wms.WavegenWizPosRemote;
import ibase.webitm.utility.ITMException;

import java.io.File;

import javax.naming.InitialContext;

public class PlistgenWizBean {
	
	E12GenericUtility genericUtility = new E12GenericUtility();
	private String objName = "";
	private String user_lang ="en"; 
	private String user_country = "US";
	
	
	public PlistgenWizBean(String objName) throws ITMException 
	{
		try
		{
			this.objName = objName;
			
		}
		catch (Exception e)
		{
			throw new ITMException(e);
		}
	}
	
	public PlistgenWizBean() {
	}


	public String previousForm( String formNo, String xmlData ) throws ITMException 
	{
		String retHtmlData = null;
		
		try
		{
			System.out.println("In Method : [previousForm]");
			
			System.out.println("xmlString : ["+ xmlData +"]");
			
			String xslFileName = getXSLFileName( this.objName + formNo + "_wiz_" + this.user_lang + "_" + this.user_country + "_" + "A" + ".xsl" );
			
			retHtmlData = (genericUtility).transformToString( xslFileName, xmlData, CommonConstants.APPLICATION_CONTEXT + File.separator + "temp", "Output", ".html" );

		}
		catch (Exception e)
		{
			throw new ITMException(e);
		}
		
		return retHtmlData;
	}
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
			File xslPath = new File( defaultPath + File.separator  + "xsl" + File.separator + CommonConstants.THEME + File.separator + "WIZARD"+ File.separator + "Galaxy");
			if ( !xslPath.exists() )
			{
				xslPath.mkdir();
			}
			System.out.println( " xslPath [" + xslPath +"] xslFileName ["+xslFileName +"]");
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
	}

}
