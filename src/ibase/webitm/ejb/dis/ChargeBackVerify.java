	/*
	This EJB is invoked from ActionHandlerService 
*/
package ibase.webitm.ejb.dis;

import ibase.webitm.utility.*;
import ibase.utility.*;
import ibase.webitm.ejb.*;
import ibase.system.config.*;
import java.rmi.RemoteException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.*;
import java.util.*;
import java.math.*;
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import java.io.*;
import java.io.File;
import java.sql.*;
import java.io.*;
import java.lang.*;
import javax.ejb.Stateless; // added for ejb3

//public class  ChargeBackVerifyEJB  extends ActionHandlerEJB implements SessionBean // commented for ejb3
@Stateless // added for ejb3
public class  ChargeBackVerify  extends ActionHandlerEJB implements ChargeBackVerifyLocal, ChargeBackVerifyRemote // added for ejb3
{
	/* commented for ejb3
	public void ejbCreate() throws RemoteException, CreateException 
	{
		try
		{
			System.out.println("Entering into ChargeBackVerifyEJB.............");
		}
		catch (Exception e)
		{
			System.out.println("Exception :ChargeBackEJB :ejbCreate :==>"+e);
			throw new CreateException();
		}
	}
	public void ejbRemove()
	{
	}
	public void ejbActivate() 
	{
	}
	public void ejbPassivate() 
	{
	}*/
	public String confirm(String tranID,String xtraParams, String forcedFlag) throws RemoteException,ITMException
	{
		VerifyAll verifyAll = null;
		String result = "";
		try
		{
			//pavan R 20/jul/18 changed the lookup to creating instance of the class using new keyword.
			verifyAll = new VerifyAll();
			System.out.println("Varifying charge back.......");			
			result =  verifyAll.verify(tranID);
			verifyAll = null;
		}
		catch(Exception e)
		{
			System.out.println("Exception [01]::"+e.getMessage());
			throw new ITMException(e);
		}
		System.out.println();
		System.out.println("Returning Result of Varification....::"+result);
		return result;
	}

	// for ejb3
		
}//class
									
	