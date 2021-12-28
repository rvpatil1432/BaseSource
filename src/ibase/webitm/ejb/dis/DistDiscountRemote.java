package ibase.webitm.ejb.dis;

import java.rmi.RemoteException;
//import javax.ejb.EJBObject;
import java.sql.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.util.*;

import ibase.webitm.ejb.*;
import ibase.webitm.utility.ITMException;
import javax.ejb.Remote; // added for ejb3


//public interface DistDiscount extends EJBObject
@Remote // added for ejb3 Changed by Nasruddin extends ValidatorRemote
public interface DistDiscountRemote extends ValidatorRemote
{
	public String priceListDiscount (String siteCode, String custCode, Connection connectionObject)throws RemoteException,ITMException;
	public String priceListSite (String siteCode, String custCode, Connection connectionObject)throws RemoteException,ITMException;
	public double getDiscount(String plistDisc, java.util.Date orderDate, String custCode, String siteCode, String itemCode, String unit, double discMerge, java.util.Date plistDate, double qty, Connection connectionObject) throws RemoteException,ITMException;
	public double pickRate(String priceList, java.util.Date tranDate, String itemCode, String lotNo, String type, Connection connectionObject) throws RemoteException,ITMException;
	public double pickRate(String priceList, java.util.Date tranDate, String itemCode, String lotNo, String type, double qty, Connection connectionObject ) throws RemoteException,ITMException;
}