/**************************
 * VALLABH KADAM
 * AutoFileUploadSh.java
 * Request Id:- [D14KSUN003]
 * 9/MAY/15
 * *******************************/
package ibase.webitm.ejb.dis;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import ibase.scheduler.utility.interfaces.Schedule;
import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFCellUtil;
import org.apache.poi.ss.util.CellRangeAddress;

//import com.sun.org.apache.xerces.internal.util.URI.MalformedURIException;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;

import ibase.webitm.ejb.ITMUploadFileEJB;
import ibase.webitm.ejb.sys.UtilMethods;
import ibase.webitm.utility.ITMException;

import java.net.*;
import java.io.*;

public class AutoFileUploadSh implements Schedule
{
	private static String loadedFileLocation, unloadedFileLocation;
	public static File DUMP_DIR, LOG_DUMP_DIR;
	UserInfoBean userInfo = null;

	@Override
	public String schedule(HashMap arg0) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String schedule(String scheduleParamXML) throws Exception
	{
		// TODO Auto-generated method stub
		String retString = "";
		if (CommonConstants.UPLOAD_LOC == null && CommonConstants.J2EE_VERSION.equals("1"))
		{
			DUMP_DIR = new File((new File(CommonConstants.APPLICATION_CONTEXT)).getParentFile().getParent() + File.separator + "dump");
			LOG_DUMP_DIR = new File(DUMP_DIR.getParent() + File.separator + "logs");
		} else
		{
			DUMP_DIR = new File((new File(CommonConstants.UPLOAD_LOC)) + File.separator + "dump");
			if (!DUMP_DIR.exists())
			{
				DUMP_DIR.mkdir();
			}

			LOG_DUMP_DIR = new File((new File(CommonConstants.UPLOAD_LOC)) + File.separator + "logs");
			if (!LOG_DUMP_DIR.exists())
			{
				LOG_DUMP_DIR.mkdir();
			}
		}
		File[] files = DUMP_DIR.listFiles();
		userInfo = new ibase.utility.UserInfoBean(scheduleParamXML);

		for (File file : files)
		{
			processXLS(file);
		}

		return retString;
	}

	private void processXLS(File file) throws ITMException
	{
		String retString = "";
		// TODO Auto-generated method stub
		if (file != null)
		{
			try
			{
				FileInputStream fos = new FileInputStream(file);
				HSSFWorkbook workbook = new HSSFWorkbook(fos);
				HSSFSheet sheet = workbook.getSheetAt(0);

				HSSFRow tempRow;
				tempRow = sheet.getRow(1);
				Object objName;
				Object pkValue;
				objName = getCellValue(HSSFCellUtil.getCell(tempRow, 0));
				String fileObjName = objName == null ? "" : objName.toString();

				pkValue = getCellValue(HSSFCellUtil.getCell(tempRow, 1));
				String filePkValue = pkValue == null ? "" : pkValue.toString();

				retString = insertFileData(file, fileObjName, filePkValue);

				if ((retString != null || retString.trim().length() > 0) && retString.indexOf("Successfully Uploaded") >= 0)
				{
					loadedFileLocation = (new File(CommonConstants.UPLOAD_LOC)) + File.separator + "Loaded";
					if (file.exists() && !file.isDirectory())
					{
						moveFile(loadedFileLocation, file);
					} else
					{
						DUMP_DIR = new File((new File(CommonConstants.UPLOAD_LOC)) + File.separator + "dump_upd");
						File tempFile = new File(DUMP_DIR, file.getName());
						moveFile(loadedFileLocation, tempFile);
					}

				} else
				{
					unloadedFileLocation = (new File(CommonConstants.UPLOAD_LOC)) + File.separator + "Unloaded";
					if (file.exists() && !file.isDirectory())
					{
						moveFile(unloadedFileLocation, file);
					} else
					{
						DUMP_DIR = new File((new File(CommonConstants.UPLOAD_LOC)) + File.separator + "upload_log");
						File tempFile = new File(DUMP_DIR, file.getName());
						moveFile(unloadedFileLocation, tempFile);
					}
				}
			} 
			catch (IOException ioE)
			{
				ioE.printStackTrace();
			} 
			catch (Exception eX)
			{
				eX.printStackTrace();
				throw new ITMException(eX); //Added By Mukesh Chauhan on 02/08/19
			}
		}
	}

	private String insertFileData(File file, String fileObjName, String filePkValue) throws ITMException
	{
		String retString = "";
		String menuObjName = fileObjName.trim();
		boolean IS_INTERACTIVE = true;
		String OBJ_NAME = fileObjName.trim();
		boolean is_ExcelDriver = false;
		String oriFileName = file.toString().trim();
		String newFileName = file.getName().trim();

		// TODO Auto-generated method stub
		String[] fileInfoArr = new String[6];
		try
		{
			fileInfoArr[0] = String.valueOf(IS_INTERACTIVE);
			fileInfoArr[1] = newFileName;
			fileInfoArr[2] = OBJ_NAME;
			// fileInfoArr[3] = oriFileName;
			// fileInfoArr[4] = menuObjName;
			fileInfoArr[5] = String.valueOf(is_ExcelDriver);

			ITMUploadFileEJB itmUpEJB = new ITMUploadFileEJB();
			retString = itmUpEJB.insertFileData(fileInfoArr, userInfo, DUMP_DIR, CommonConstants.APPLICATION_CONTEXT, menuObjName, is_ExcelDriver);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			throw new ITMException(e); //Added By Mukesh Chauhan on 02/08/19
		}
		return retString;
	}

	Object getCellValue(HSSFCell cell)
	{
		if (cell.getCellType() == 1)
			return cell.getStringCellValue();
		if (cell.getCellType() == 0)
		{
			return Double.valueOf(cell.getNumericCellValue());
		}
		return null;
	}

	public void moveFile(String fileLocation, File file)
	{
		File destPath = new File(fileLocation);
		if (!destPath.exists())
		{
			destPath.mkdir();
		}
		file.renameTo(new File(destPath, file.getName()));
	}

	@Override
	public String schedulePriority(String arg0) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}
}
