package ibase.webitm.ejb.dis;

import ibase.utility.*;
import ibase.system.config.*;
import ibase.webitm.ejb.*;
import ibase.webitm.ejb.mfg.RequirementICRemote;
import ibase.webitm.utility.*;
import ibase.utility.E12GenericUtility;
import javax.servlet.http.*;
import java.io.*;
import javax.naming.InitialContext;
public class EmpFlightPopHelpBean  
{
	private ibase.utility.UserInfoBean userInfo = null;
	private HttpSession sessionCtx = null;
	ITMWizardStatefulRemote itmWizardRemote = null;
	private String objName = "";
	private String user_lang ="en"; 
	private String user_country = "US";
	
	public EmpFlightPopHelpBean ( String objName, HttpSession sessionCtx ) throws ITMException
	{
		try
		{
			this.objName = objName;
			this.sessionCtx = sessionCtx;
			this.userInfo = ( ibase.utility.UserInfoBean ) this.sessionCtx.getAttribute("USER_INFO");
			this.user_lang = this.userInfo.getUserLanguage();
			this.user_country = this.userInfo.getUserCountry();
			if( this.itmWizardRemote == null )
			{
				InitialContext ctx = new InitialContext( new AppConnectParm().getProperty() );
				this.itmWizardRemote = (ibase.webitm.ejb.ITMWizardStatefulRemote)ctx.lookup("ibase/ITMWizardStatefulEJB/remote");
				
				this.itmWizardRemote.setUserInfo( this.userInfo );
				
				this.itmWizardRemote.loadFormsMetaData( this.objName, "1", this.objName+"21", this.userInfo.getEmpCode(), this.userInfo.getProfileId() );
			}
		}
		catch (ITMException itme)
		{
			itmWizardRemote.remove();
			itmWizardRemote = null;
			throw itme;
		}
		catch (Exception e)
		{
			itmWizardRemote.remove();
			itmWizardRemote = null;
			throw new ITMException(e);
		}		
	}

	public EmpFlightPopHelpBean () throws ITMException
	{
	}
	//get flight code pop help 
	public String getFlightCodeList( String flightCode) throws ITMException
	{
		String flightCodeData = "";
		//EmpTravelInfoICRemote EmpTravelInfoICRemote = null;
		EmpTravelInfoIC EmpTravelInfoICRemote = null;
		E12GenericUtility genericUtility= new  E12GenericUtility();
		try
		{
			
			EmpTravelInfoICRemote = new EmpTravelInfoIC();
			flightCodeData = EmpTravelInfoICRemote.getFlightCodeList( flightCode);
			EmpTravelInfoICRemote = null;
			System.out.println("flightCodeData=="+flightCodeData);
			String xslFileName = getXSLFileName( "emp_travel_fligt_code_wiz_" + this.user_lang + "_" + this.user_country + ".xsl" );
			//String xslFileName = getXSLFileName( "requirement_item_code_wiz_en_US.xsl" );
			flightCodeData = genericUtility.transformToString( xslFileName, flightCodeData, CommonConstants.APPLICATION_CONTEXT + File.separator + "temp", "Output", ".html" );
			
			System.out.println("flightCodeData==="+flightCodeData);
		}
		catch ( Exception e  )
		{
			throw new ITMException(e);
		}
		finally
		{
			if ( EmpTravelInfoICRemote != null )
			{
				EmpTravelInfoICRemote = null;
			}						
		}
		return flightCodeData;
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
			File xslPath = new File( defaultPath + File.separator  + "xsl" + File.separator + CommonConstants.THEME + File.separator + "WIZARD");
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

