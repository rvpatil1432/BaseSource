package ibase.webitm.servlet.dis;


import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;

import ibase.ejb.CommonDBAccessEJB;
import ibase.servlet.Messages;
import ibase.utility.BaseException;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.dis.SCMWebServiceDao;
import ibase.webitm.utility.ITMException;

@Path("/scm")
public class SCMWebService 
{
	SCMWebServiceDao scmWSDao = new SCMWebServiceDao();
	//PriyankaC on [START..]
	private UserInfoBean userInfo = null;
	private E12GenericUtility genericUtility = new E12GenericUtility();
	//PriyankaC [END].
	
	@POST
	@Path("/addTransaction")
	@Produces({"application/xml"})
	public Response addTransaction(@Context HttpServletRequest request, @FormParam("DATA_XML") String dataXML, @FormParam("USERNAME") String userCode, @FormParam("PASSWORD") String password, @FormParam("SITECODE") String siteCode)
	{
		String retString = "";
		ArrayList<String> langAndCountryList = new ArrayList<String>(); 

		CommonDBAccessEJB commonDBAccessLocal = null;
		try
		{
			if(dataXML == null || dataXML.trim().length() == 0 || userCode == null || userCode.trim().length() == 0 || password == null || password.trim().length() == 0)
			{
				System.out.println("IN missing parameters");
				retString = "<Root><tran_id><![CDATA[]]></tran_id><msg_code><![CDATA[]]></msg_code><msg_descr><![CDATA[Missing required paramters]]></msg_descr><result><![CDATA[FAILED]]></result></Root>";
				return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(retString).build();
			}
			commonDBAccessLocal = new CommonDBAccessEJB();
			System.out.println("SCMWebService.addTransaction() userCode : [" +userCode+ "]");
			System.out.println("SCMWebService.addTransaction() siteCode : [" +siteCode+ "]");
			//Changed By Pankaj T. on 25-07-19 for authentication pass site_code through API, if site_code passed from api is null then use login user site_code - start
			if( "".equals(E12GenericUtility.checkNull(siteCode)) )
			{
				siteCode = commonDBAccessLocal.getDBColumnValue( "USERS", "SITE_CODE__DEF","WHERE CODE = "+"'" +userCode+"'" );
				System.out.println("SCMWebService.addTransaction() siteCode against user: [" +siteCode+ "]");
			}
			//Changed By Pankaj T. on 25-07-19 for authentication pass site_code through API, if site_code passed from api is null then use login user site_code - end
			//String xmlInfoStr = commonDBAccessLocal.authenticate(userCode, password, siteCode, "I", "true"); // 24-dec-2020 manoharan as suggested by Prasad/Pankaj this is for old proteus-framework jar
			String xmlInfoStr = commonDBAccessLocal.authenticate(userCode, password, siteCode, "I", request);
			System.out.println("authRetString["+xmlInfoStr+"]");
			
			String userLanguage = commonDBAccessLocal.getDBColumnValue( "USERS", "USER_LANG","WHERE CODE = "+"'" +userCode+"'" );
			String userCountry = commonDBAccessLocal.getDBColumnValue( "USERS", "USER_COUNTRY", "WHERE CODE = "+"'" +userCode+"'" );
			String passWD = commonDBAccessLocal.getDBColumnValue( "USERS", "PASS_WD","WHERE CODE = "+"'" +userCode+"'" );
			if( userLanguage != null && userCountry != null )
			{
				langAndCountryList.add( userLanguage );
				langAndCountryList.add( userCountry );
			}
			
			retString = generateResponse(xmlInfoStr, langAndCountryList);
			
			if(retString != null && retString.trim().length()>0)
			{
				return Response.status(HttpServletResponse.SC_UNAUTHORIZED).entity(retString).build();
			}
			
			retString = scmWSDao.addTransaction(dataXML, userCode, passWD, xmlInfoStr);
		}
		catch(Exception e)
		{
			System.out.println("SCMWebService.addTransaction()["+e.getMessage()+"]");
			e.printStackTrace();
		}
		return Response.status(HttpServletResponse.SC_OK).entity( retString ).build();
	}
	
	
	private String generateResponse( String xmlInfo, ArrayList<String> langAndCountryList )throws BaseException
	{
		StringBuffer responseString = new StringBuffer();
		String errorMessage = "";
		try
		{
			if ( E12GenericUtility.checkNull( xmlInfo ).length() > 0 )
			{
				Document document = new E12GenericUtility().parseString( xmlInfo );
				if ( document.getElementsByTagName( "STATUS_CODE" ).item(0) != null && document.getElementsByTagName("STATUS_CODE").item(0).getFirstChild() != null )
				{
					String statusCode = document.getElementsByTagName("STATUS_CODE").item(0).getFirstChild().getNodeValue();
					if ( statusCode.equalsIgnoreCase("RESIGNED") )
					{
						errorMessage = Messages.getString( "AuthenticateServlet_employeeHasResigned", langAndCountryList );
					}
					else if ( statusCode.equalsIgnoreCase("LOCKED") )
					{
						errorMessage = Messages.getString( "AuthenticateServlet_accountIsLockedContactAdmin", langAndCountryList );
					}
					else if ( statusCode.indexOf("INVALID") != -1 )
					{
						if ( statusCode.equalsIgnoreCase("INVALID_USR") )
						{
							errorMessage = Messages.getString( "AuthenticateServlet_pleaseEnterValidUserNameAndPassword", langAndCountryList );
						}
						else if ( statusCode.equalsIgnoreCase("INVALID_PWD") )
						{
							errorMessage = Messages.getString( "AuthenticateServlet_pleaseEnterValidUserNameAndPassword", langAndCountryList );
						}
					}
					else if( statusCode.indexOf("PASS_EXP") != -1 ) 
					{
						if ( statusCode.equalsIgnoreCase("PASS_EXP_NOGRACE") )
						{
							errorMessage = Messages.getString( "AuthenticateServlet_passwordExpiredChangeIt", langAndCountryList );
						}
						else if (statusCode.equalsIgnoreCase("PASS_EXP_GRACE")) // 07-jan-2021 Manoharan
						{
							errorMessage = Messages.getString( "AuthenticateServlet_passwordExpiredChangeIt", langAndCountryList );
						}
					}
					
					if( !"VALID".equalsIgnoreCase(statusCode) )
					{
						responseString.append("<Root>");
						responseString.append("<tran_id><![CDATA[]]></tran_id>");
						responseString.append("<msg_code><![CDATA[]]></msg_code>");
						responseString.append("<msg_descr><![CDATA["+errorMessage+"]]></msg_descr>");
						responseString.append("<result><![CDATA[FAILED]]></result>");
						responseString.append("</Root>");
					}
				}
			}
			
			
			System.out.println("Final auth retString["+responseString.toString()+"]");
		}
		catch( BaseException be )
		{
			System.out.println("Exception: generateResponse:==>\n"+be);
			throw be;
		}
		catch( Exception e )
		{
			System.out.println("Exception: generateResponse:==>\n"+e);
			throw new BaseException( e );
		}
		return responseString.toString();
	}
	
	//Added By PriyankaC To getBank DATA on 27FEB2018...[START]
	@GET
	@Path("/getBankDetails")
	@Produces({"application/xml", "application/json"})
	public Response getBankDetails( @Context UriInfo siteCode , @QueryParam("USERCODE") String userCode,@QueryParam("PASSWORD") String password ,@QueryParam("MASTER_REQ") String masterReq ) throws BaseException, Exception
	{
		String retString = "";
		List<String> sitecodeList = siteCode.getQueryParameters().get("SITECODE");
		System.out.println("Received List: "+sitecodeList +"PASSWARD" +password +" MASTER_REQ " +masterReq);
		if( (sitecodeList==null || sitecodeList.isEmpty() == true) || (userCode == null || userCode.trim().length() == 0) || (password == null || password.trim().length() ==0) || (masterReq == null || masterReq.trim().length()==0))
		{
			System.out.println("IN missing parameters");
			retString = "<Root><msg_code><![CDATA[]]></msg_code><msg_descr><![CDATA[Missing required paramters]]></msg_descr></Root>";
			JSONObject jsonObj = null;
			jsonObj = XML.toJSONObject(retString); 
			return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(jsonObj.toString()).build();
		}
		retString = scmWSDao.getResult(sitecodeList,userCode,password,masterReq);
		return Response.status(HttpServletResponse.SC_OK).entity( retString ).build();
	}
	//Added By PriyankaC To getBank DATA on 27FEB2018..[END]
}
