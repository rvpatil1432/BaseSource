package ibase.webitm.ejb.dis;

import java.sql.*;

import ibase.utility.CommonConstants;

import ibase.utility.E12GenericUtility;
import ibase.webitm.utility.ITMException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
public class GenLogFile {

	public void writeLog(String fileName, String msgString) throws Exception
	{
		String errCode = null, writeString;
		//GenericUtility genericUtility = GenericUtility.getInstance();
		E12GenericUtility genericUtility= new  E12GenericUtility();

		java.io.File logFile = null;
		java.io.FileWriter logFileWtr = null;
		String filePath = CommonConstants.JBOSSHOME + File.separator +"ES3log";
		SimpleDateFormat sdf1 = new SimpleDateFormat(genericUtility.getDBDateFormat());
		String currTime = null;
		Calendar calendar = Calendar.getInstance();

		try
		{
			
			logFile = new java.io.File( filePath );
			if(!(logFile.exists()))
			{
				logFile.mkdir();
			}
			filePath = filePath + File.separator+ fileName+".log";
			logFile = new java.io.File( filePath );
			logFileWtr = new java.io.FileWriter( logFile, true );
			currTime = sdf1.format(new Timestamp(System.currentTimeMillis())).toString();
			currTime = currTime.replaceAll("-","");
			System.out.println("Inside log file case 1"+msgString);
			calendar.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
			System.out.println("Inside log file case 2"+msgString);
			if( logFile == null )
			{
				errCode = "VBFILEOPEN";
			System.out.println("VBFILEOPEN");
			}
			else
			{	
				System.out.println("Inside log file case 3"+msgString);
				writeString = "{" + currTime  + " " + calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE) + "}\t" + msgString + "\r\n";
				//logFileWtr.write( writeString );
				System.out.println("Inside log file case 4"+writeString);
				char writeCharArr[] = new char[ writeString.length() ];
				writeString.getChars( 0, writeString.length(), writeCharArr, 0 );
				logFileWtr.write( writeCharArr );				
			}
		}catch( Exception ex )
		{
			errCode = "VFLEIOERR"; 
			ex.printStackTrace();
			throw new ITMException(ex); //Added By Mukesh Chauhan on 07/08/19
		}
		finally{
			try{
				logFileWtr.close();
				logFileWtr = null;
				logFile = null;		
			}catch( Exception e ){ 
				errCode = "VEFCLERR"; 
				e.printStackTrace();
			}
		}
		//return errCode;
	}

}
