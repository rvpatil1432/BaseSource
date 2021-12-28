package ibase.webitm.servlet.dis;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ibase.bi.utility.Messages;
import ibase.utility.CommonConstants;
import ibase.webitm.ejb.dis.PricelistGenWizEJB;
import ibase.webitm.utility.ITMException;


public class PlistgenWizServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	//InitialContext ctx = null;

    public PlistgenWizServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doPost( request, response );
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		System.out.println("XXXXXXXXXXXXXXXXXXXXXX[ PlistgenWizServlet START]XXXXXXXXXXXXXXXXXXXX");
		request.setCharacterEncoding(CommonConstants.ENCODING);
		String action = "", responseXML = "", htmlData = "";
		
		PricelistGenWizEJB pricelistGenWizRemote = null;
		
		try 
		{	
			action = request.getParameter("action");
			System.out.println("PlistgenWizServlet ACTION ["+action+"]");
			
			if("previous".equalsIgnoreCase(action))
			{
				ibase.utility.UserInfoBean userInfo = ( ibase.utility.UserInfoBean )request.getSession().getAttribute( "USER_INFO" );
				if(userInfo != null)
				{
					String objName 	= request.getParameter("OBJ_NAME");
					String formNo 	= request.getParameter("FORM_NO");
					String PRE_FORM_VAL = request.getParameter("PRE_FORM_VAL");
					
					System.out.println("OBJ_NAME["+objName+"]");
					System.out.println("FORMNO["+formNo+"]");
					System.out.println("PRE_FORM_VAL["+PRE_FORM_VAL+"]");
					
					String editorId = (String) request.getSession().getAttribute( "WIZARD_BEAN_ID_" + objName );
		            System.out.println( " PlistgenWizServlet :: editorId["+editorId+"]" );
		            
		            if ( request.getSession().getAttribute(  "WIZARD_BEAN_" + editorId ) != null )
					{
						System.out.println(" wizard object found....");
						
						request.getSession().removeAttribute( "WIZARD_BEAN_" + objName );
						//Added by Santosh on 31/03/2017 to remove editorID from session						
						request.getSession().removeAttribute( "WIZARD_BEAN_ID_" + objName );
						System.out.println("WIZARD_BEAN_ and WIZARD_BEAN_ID_ removed from  session");
						request.getRequestDispatcher("/webitm/jsp/WavegenWiz.jsp").forward(request, response);
					}
				}
				else
				{
					htmlData = Messages.getString("ITMWizardHandlerServlet_notLoggedIn")+"\n"+Messages.getString("ITMWizardHandlerServlet_pleaseReLogin");
					System.out.println("htmlData ["+htmlData+"]");
				}
			}
			else if("SAVE_DETAIL_DATA".equalsIgnoreCase(action))
			{
				String prevHTMLData = request.getParameter("HTML_DATA");
				request.getSession().setAttribute( "WAVEGEN_WIZ_DETAIL",prevHTMLData);
			}
			else if("DISPLAY_DETAIL_DATA".equalsIgnoreCase(action))
			{
				String htmlDataErr = (String) request.getSession().getAttribute( "WAVEGEN_WIZ_DETAIL");
				request.getSession().removeAttribute( "WAVEGEN_WIZ_DETAIL");
				
				response.setContentType("text/html");
		         
		        if(CommonConstants.CONTENT_ENCODING != null && CommonConstants.CONTENT_ENCODING.equalsIgnoreCase("gzip"))
				{
					response.setHeader("Content-Encoding", "gzip");
					GZIPOutputStream gzOutStream = new GZIPOutputStream(response.getOutputStream());
					gzOutStream.write(htmlDataErr.getBytes());
					gzOutStream.flush();
					gzOutStream.close();
				}
				else
				{
					response.setHeader("Content-Encoding", CommonConstants.CONTENT_ENCODING);
					OutputStream outStream = response.getOutputStream();
					outStream.write(htmlDataErr.getBytes());
					outStream.flush();
					outStream.close();
				}
			}
			else
			{
				ibase.utility.UserInfoBean userInfoBean = ( ibase.utility.UserInfoBean )request.getSession().getAttribute( "USER_INFO" );
				
				String loginCode=userInfoBean.getLoginCode();
				
				HashMap<String,String> requestParamMap = new HashMap<String,String>();
				String paramName = "", paramValue = "";
				
				@SuppressWarnings("unchecked")
				Enumeration<String> reqParams = request.getParameterNames();
				while(reqParams.hasMoreElements())
				{
					Object paramObj = reqParams.nextElement();
					paramName = (String) paramObj;
					paramValue = (String)request.getParameter(paramName);
					if(!paramValue.equalsIgnoreCase(""))
						requestParamMap.put(paramName,paramValue);
				}
				requestParamMap.put("loginUser", loginCode);
				System.out.println("Request Parameter map :"+requestParamMap);
				
				pricelistGenWizRemote = new PricelistGenWizEJB();
				responseXML = pricelistGenWizRemote.handleRequest(requestParamMap);
				
				response.setContentType("text/xml");
		         
		         if(CommonConstants.CONTENT_ENCODING != null && CommonConstants.CONTENT_ENCODING.equalsIgnoreCase("gzip"))
				{
					response.setHeader("Content-Encoding", "gzip");
					GZIPOutputStream gzOutStream = new GZIPOutputStream(response.getOutputStream());
					gzOutStream.write(responseXML.getBytes());
					gzOutStream.flush();
					gzOutStream.close();
				}
				else
				{
					response.setHeader("Content-Encoding", "");
					OutputStream outStream = response.getOutputStream();
					outStream.write(responseXML.getBytes());
					outStream.flush();
					outStream.close();
				}
			}
		} 
		catch (Exception e) 
		{
			System.out.println("PlistgenWizServlet.doPost():doPost"+e.getMessage());
			e.printStackTrace();
		}
		
         System.out.println("XXXXXXXXXXXXXXXXXXXXXX[ PlistgenWizServlet END]XXXXXXXXXXXXXXXXXXXX");
	}
	
}