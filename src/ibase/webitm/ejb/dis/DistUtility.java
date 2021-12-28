package ibase.webitm.ejb.dis;

import ibase.utility.CommonConstants;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ValidatorEJB;
import ibase.webitm.utility.ITMException;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.imageio.ImageIO;

public class DistUtility extends ValidatorEJB
{
	//Added and replace by sarita on 2nd JAN 2018
	//public String getImagePath(String tranWindow, String tranId, String altColVal, String folderName, Connection conn) throws ITMException
	public String getImagePath(String tranWindow, String tranId, String altColVal, String folderName, Connection conn,UserInfoBean userInfo) throws ITMException
	{
		System.out.println("Common utility to get image path");
		String retImgPath = "", refSer = "", docId = "", docType = "", objName = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		//Connection conn = null;
		
		try
		{
			if (conn == null)
			{
				//Added by sarita on 2nd JAN 2018
				setUserInfo(userInfo);
				conn = getConnection();
			}
			
			objName = tranWindow.substring(tranWindow.indexOf("w_")+2, tranWindow.length());
						
			sql = " SELECT REF_SER FROM TRANSETUP WHERE TRAN_WINDOW = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, tranWindow);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				refSer = checkNullAndTrim(rs.getString("REF_SER"));
			}	
			if(rs != null)
			{
				rs.close();rs = null;
			}	
			if(pstmt != null)
			{	
				pstmt.close();pstmt = null;
			}
			
			sql = " SELECT DOC_CONTENTS.DOC_ID,DOC_CONTENTS.DOC_TYPE FROM DOC_CONTENTS, DOC_TRANSACTION_LINK, USERS "
				+ " WHERE DOC_TRANSACTION_LINK.DOC_ID = DOC_CONTENTS.DOC_ID AND USERS.CODE = DOC_CONTENTS.ADD_USER "
				+ " AND REF_SER = ? AND REF_ID = ? ORDER BY DOC_CONTENTS.DOC_ID ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, refSer);
			pstmt.setString(2, tranId);

			rs = pstmt.executeQuery();
			
			if(rs.next())
			{
				docId = checkNullAndTrim(rs.getString("DOC_ID"));
				docType= checkNullAndTrim(rs.getString("DOC_TYPE"));
				retImgPath = "/ibase/WebITMDocumentHandlerServlet?OBJ_NAME="+objName+"&REF_ID="+tranId.trim()+"&ACTION=GET_DOCUMENT&CLIENT=WEB1&DOC_ID="+docId+"&DOC_TYPE="+docType+"";
			}
			else
			{
				retImgPath = getCustomImagePath(tranId.trim(),altColVal,folderName);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e);
		}
		finally
		{
			try 
			{
				if (rs != null) 
				{					
					rs.close();rs = null;
				}
				if (pstmt != null ) 
				{					
					pstmt.close();pstmt = null;
				}
				if (conn != null && !conn.isClosed()) 
				{					
					conn.close();conn = null;
				}
			} 
			catch (Exception e) 
			{
				throw new ITMException(e);
			}
		}
		
		return retImgPath;
	}
	
	public String getCustomImagePath(String value,String altColImg, String object) throws ITMException
	{
		String imagePath = "";
		try
		{
			File objDir = new File (CommonConstants.RIALITE_PROFILE_PATH +File.separator+ object);
			if(!objDir.exists())
			{
				objDir.mkdir();
			}
			if( fileExist(value, object))
			{
				imagePath = File.separator+"ibase"+File.separator+"resource"+File.separator+ object+File.separator + value + ".png";
			}
			else
			{
				File f = new File(CommonConstants.RIALITE_PROFILE_PATH +File.separator+ object+File.separator + value + ".png");
				BufferedImage bi = createLabelImage(altColImg);
				ImageIO.write(bi,"PNG",f);
				imagePath = File.separator+"ibase"+File.separator+"resource"+File.separator+ object+File.separator + value + ".png";
			}
			
		}
		catch(Exception e)
		{
			System.out.println("PopUpDataAccessEJB.getCustomeImagePath()::getCustomImagePath");
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return imagePath;		
	}
	public boolean fileExist(String value, String object) throws ITMException
	{
		boolean flag = true;
		try
		{
			String path = CommonConstants.RIALITE_PROFILE_PATH +File.separator+ object+File.separator + value + ".png";
			File f = new File(path);
			if( !f.exists() )
			{
				flag = false;
			}
			else
			{
				flag = true;
			}
		}
		catch (Exception e) 
		{
			System.out.println("PopUpDataAccessEJB.fileExist()"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return flag;
	}
	public BufferedImage createLabelImage(String value) throws ITMException
	{
		int width = 400, height = 400;
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		try 
		{
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			File fontFile = new File(CommonConstants.APPLICATION_CONTEXT+"webitm"+File.separator+"css"+File.separator+"fonts"+File.separator+"Museo300-Regular.ttf");
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
			Graphics2D g = bi.createGraphics();
			String text = checkNull(dynamicMenuImage(value));
			int centerX = 200, centerY = 200;
			int ovalWidth = 400, ovalHeight = 400;
			Font font = new Font("Museo 300", Font.PLAIN, 200);
			g.setFont(font);
			Color c = Color.decode("#cfbebe");
			g.setColor(c);
			g.fillOval(centerX-ovalWidth/2, centerY-ovalHeight/2,ovalWidth, ovalHeight);
			FontMetrics fm = g.getFontMetrics();
			double textWidth = fm.getStringBounds(text,g).getWidth();
			g.setColor(Color.WHITE);
			g.drawString(text, (int) (centerX - textWidth/2), (int) ( (centerY + fm.getMaxAscent() / 2) ) - 2);
		}
		catch (Exception e) 
		{
			System.out.println("PopUpDataAccessEJB.createLabelImage()"+e.getMessage());
			throw new ITMException(e); //Added By Mukesh Chauhan on 07/08/19
		}
		return bi;
	}
	private String dynamicMenuImage(String objDescr)
	{
		StringBuffer mnIconBuffer = new StringBuffer();
		String[] refSr = null;
		
		objDescr = objDescr.toUpperCase();
		objDescr = checkNull(objDescr);
		objDescr = objDescr.trim();
		if( objDescr != "" )
		{
			if( objDescr.indexOf(" ") != -1 )
			{
				refSr = objDescr.split(" ");
			}
			else if( objDescr.indexOf("-") != -1 )
			{
				refSr = objDescr.split("-");
			}
			else if( objDescr.indexOf("_") != -1 )
			{
				refSr = objDescr.split("_");
			}
			else if( objDescr.indexOf(":") != -1 )
			{
				refSr = objDescr.split(":");
			}
			String menuStr = "";
			
			if( refSr != null )
			{
				for( int i = 0; i <= refSr.length; i++ )
				{
					if( mnIconBuffer.length() < 2 && checkNull( refSr[i] ) != "" )
					{
						menuStr = refSr[i];
						menuStr = menuStr.trim();
						if( menuStr != ":" && !"".equalsIgnoreCase(menuStr) )
						{
							mnIconBuffer.append( menuStr.charAt(0) );
						}
					}
				}
			}
			else
			{
				mnIconBuffer.append( objDescr.charAt(0) );
			}
		}
		
		String menuIconPath = mnIconBuffer.toString();
		
		return menuIconPath;
	}
	private static String checkNull(String input)
	{
		if (input==null)
		{
			input="";
		}
		return input;
	}
	private static String checkNullAndTrim(String input)
	{
		if (input==null)
		{
			input="";
		}
		return input.trim();
	}

}
