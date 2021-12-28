package ibase.webitm.ejb.dis.adv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ibase.system.config.ConnDriver;
import ibase.utility.CommonConstants;
import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.DBAccessEJB;
import ibase.webitm.ejb.dis.DistCommon;
import ibase.webitm.utility.ITMException;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
















































@JsonPropertyOrder({"userGstin", "supplyType", "subSupplyType", "docType", "docNo", "docDate", "transType", "fromGstin", "fromTrdName", "fromAddr1", "fromAddr2", "fromPlace", "fromPinCode", "fromStateCode", "actualFromStateCode", "toGstin", "toTrdName", "toAddr1", "toAddr2", "toPlace", "toPincode", "toStateCode", "actualToStateCode", "totalValue", "cgstValue", "sgstValue", "igstValue", "cessValue", "TotNonAdvolVal", "OthValue", "totInvValue", "transMode", "transDistance", "transporterName", "transporterId", "transDocNo", "transDocDate", "vehicleNo", "vehicleType", "mainHsnCode", "itemList"})
public class EwayBillValue
{
  private String userGstin;
  private String supplyType;
  private int subSupplyType;
  private String docType;
  private String docNo;
  private String docDate;
  private int transType;
  private String fromGstin;
  private String fromTrdName;
  private String fromAddr1;
  private String fromAddr2;
  private String fromPlace;
  private int fromPinCode;
  private int fromStateCode;
  private int actualFromStateCode;
  private String toGstin;
  private String toTrdName;
  private String toAddr1;
  private String toAddr2;
  private String toPlace;
  private int toPincode;
  private int toStateCode;
  private int actualToStateCode;
  private double totalValue;
  private double cgstValue;
  private double sgstValue;
  private double igstValue;
  private double cessValue;
  private int TotNonAdvolVal;
  private int OthValue;
  private double totInvValue;
  private int transMode;
  private double transDistance;
  private String transporterName;
  private String transporterId;
  private String transDocNo;
  private String transDocDate;
  private String vehicleNo;
  private String vehicleType;
  private int mainHsnCode;
  private List<ItemDetails> itemList;
  public EwayBillValue() {}
  
  public EwayBillValue(String userGstin, String supplyType, int subSupplyType, String docType, String docNo, String docDate, int transType, String fromGstin, String fromTrdName, String fromAddr1, String fromAddr2, String fromPlace, int frompinCode, int fromstateCode, int actualFromStateCode, String toGstin, String toTrdName, String toAddr1, String toAddr2, String toPlace, int toPincode, int tostateCode, int actualToStateCode, double totalvalue, double cgstValue, double sgstValue, double igstValue, double cessValue, int TotNonAdvolVal, int OthValue, double totInvValue, int transMode, double transDistance, String transporterName, String transporterId, String transDocNo, String transDocDate, String sundryType, String vehicleType, String vehicleNo, int mainHsnCode, List<ItemDetails> itemList, String refSer, String tranId)
  {
    this.userGstin = checkNull(userGstin).trim();
    this.supplyType = checkNull(supplyType).trim();
    this.subSupplyType = subSupplyType;
    this.docType = checkNull(docType).trim();
    this.docNo = checkNull(docNo).trim();
    this.docDate = checkNull(docDate).trim();
    this.transType = transType;
    this.fromGstin = checkNull(fromGstin).trim();
    this.fromTrdName = checkNull(fromTrdName).trim();
    this.fromAddr1 = checkNull(fromAddr1).trim();
    this.fromAddr2 = checkNull(fromAddr2).trim();
    this.fromPlace = checkNull(fromPlace).trim();
    fromPinCode = frompinCode;
    fromStateCode = fromstateCode;
    this.actualFromStateCode = actualFromStateCode;
    this.toGstin = checkNull(toGstin);
    this.toTrdName = checkNull(toTrdName).trim();
    this.toAddr1 = checkNull(toAddr1).trim();
    this.toAddr2 = checkNull(toAddr2).trim();
    this.toPlace = checkNull(toPlace).trim();
    this.toPincode = toPincode;
    toStateCode = tostateCode;
    this.actualToStateCode = actualToStateCode;
    totalValue = totalvalue;
    this.cgstValue = cgstValue;
    this.sgstValue = sgstValue;
    this.igstValue = igstValue;
    this.cessValue = cessValue;
    this.TotNonAdvolVal = TotNonAdvolVal;
    this.OthValue = OthValue;
    this.totInvValue = totInvValue;
    this.transMode = transMode;
    this.transDistance = transDistance;
    this.transporterName = checkNull(transporterName).trim();
    this.transporterId = checkNull(transporterId).trim();
    this.vehicleType = checkNull(vehicleType).trim();
    this.vehicleNo = checkNull(vehicleNo).trim();
    this.transDocDate = checkNull(transDocDate).trim();
    this.transDocNo = checkNull(transDocNo).trim();
    this.itemList = itemList;
    this.mainHsnCode = mainHsnCode;
  
  }
  
  public String getUserGstin() { return userGstin; }
  
  public void setUserGstin(String userGstin) {
    this.userGstin = userGstin;
  }
  
  public String getSupplyType() { return supplyType; }
  
  public void setSupplyType(String supplyType) {
    this.supplyType = supplyType;
  }
  
  public int getSubSupplyType() { return subSupplyType; }
  
  public void setSubSupplyType(int subSupplyType) {
    this.subSupplyType = subSupplyType;
  }
  
  public String getDocType() { return docType; }
  
  public void setDocType(String docType) {
    this.docType = docType;
  }
  
  public String getDocNo() { return docNo; }
  
  public void setDocNo(String docNo) {
    this.docNo = docNo;
  }
  
  public String getDocDate() { return docDate; }
  
  public void setDocDate(String docDate) {
    this.docDate = docDate;
  }
  
  @JsonProperty("transType")
  public int gettransType() { return transType; }
  
  @JsonProperty("transType")
  public void settransType(int transType) {
    this.transType = transType;
  }
  
  public String getFromGstin() { return fromGstin; }
  
  public void setFromGstin(String fromGstin) {
    this.fromGstin = fromGstin;
  }
  
  public String getFromTrdName() { return fromTrdName; }
  
  public void setFromTrdName(String fromTrdName) {
    this.fromTrdName = fromTrdName;
  }
  
  public String getFromAddr1() { return fromAddr1; }
  
  public void setFromAddr1(String fromAddr1) {
    this.fromAddr1 = fromAddr1;
  }
  
  public String getFromAddr2() { return fromAddr2; }
  
  public void setFromAddr2(String fromAddr2) {
    this.fromAddr2 = fromAddr2;
  }
  
  public String getFromPlace() { return fromPlace; }
  
  public void setFromPlace(String fromPlace) {
    this.fromPlace = fromPlace;
  }
  
  @JsonProperty("fromPinCode")
  public int getfromPinCode() {
    return fromPinCode;
  }
  
  @JsonProperty("fromPinCode")
  public void setfromPinCode(int fromPinCode) {
    this.fromPinCode = fromPinCode;
  }
  
  @JsonProperty("fromStateCode")
  public int getfromStateCode() {
    return fromStateCode;
  }
  
  @JsonProperty("fromStateCode")
  public void setfromStateCode(int fromStateCode) {
    this.fromStateCode = fromStateCode;
  }
  
  public int getActualFromStateCode() {
    return actualFromStateCode;
  }
  
  public void setActualFromStateCode(int actualFromStateCode) {
    this.actualFromStateCode = actualFromStateCode;
  }
  
  public String getToGstin() { return toGstin; }
  
  public void setToGstin(String toGstin) {
    this.toGstin = toGstin;
  }
  
  public String getToTrdName() { return toTrdName; }
  
  public void setToTrdName(String toTrdName) {
    this.toTrdName = toTrdName;
  }
  
  public String getToAddr1() { return toAddr1; }
  
  public void setToAddr1(String toAddr1) {
    this.toAddr1 = toAddr1;
  }
  
  public String getToAddr2() { return toAddr2; }
  
  public void setToAddr2(String toAddr2) {
    this.toAddr2 = toAddr2;
  }
  
  public String getToPlace() { return toPlace; }
  
  public void setToPlace(String toPlace) {
    this.toPlace = toPlace;
  }
  
  public int getToPincode() { return toPincode; }
  
  public void setToPincode(int toPincode) {
    this.toPincode = toPincode;
  }
  
  @JsonProperty("toStateCode")
  public int getTostateCode() {
    return toStateCode;
  }
  
  @JsonProperty("toStateCode")
  public void setTostateCode(int toStateCode) {
    this.toStateCode = toStateCode;
  }
  
  public int getActualToStateCode() { return actualToStateCode; }
  
  public void setActualToStateCode(int actualToStateCode) {
    this.actualToStateCode = actualToStateCode;
  }
  
  public double getCgstValue() { return cgstValue; }
  
  public void setCgstValue(double cgstValue) {
    this.cgstValue = cgstValue;
  }
  
  public double getSgstValue() { return sgstValue; }
  
  public void setSgstValue(double sgstValue) {
    this.sgstValue = sgstValue;
  }
  
  public double getIgstValue() { return igstValue; }
  
  public void setIgstValue(double igstValue) {
    this.igstValue = igstValue;
  }
  
  public double getCessValue() { return cessValue; }
  
  public void setCessValue(double cessValue) {
    this.cessValue = cessValue;
  }
  
  @JsonProperty("TotNonAdvolVal")
  public int getTotNonAdvolVal() {
    return TotNonAdvolVal;
  }
  
  @JsonProperty("TotNonAdvolVal")
  public void setTotNonAdvolVal(int TotNonAdvolVal) {
    this.TotNonAdvolVal = TotNonAdvolVal;
  }
  
  @JsonProperty("OthValue")
  public int getOthValue() {
    return OthValue;
  }
  
  @JsonProperty("OthValue")
  public void setOthValue(int OthValue) {
    this.OthValue = OthValue;
  }
  
  public double getTotalValue() {
    return totalValue;
  }
  
  public void setTotalValue(double totalValue) { this.totalValue = totalValue; }
  

  public int getTransMode()
  {
    return transMode;
  }
  
  public void setTransMode(int transMode) {
    this.transMode = transMode;
  }
  
  public String getTransporterName()
  {
    return transporterName;
  }
  
  public void setTransporterName(String transporterName) {
    this.transporterName = transporterName;
  }
  
  public String getTransporterId()
  {
    return transporterId;
  }
  
  public void setTransporterId(String transporterId) {
    this.transporterId = transporterId;
  }
  
  public String getVehicleType() {
    return vehicleType;
  }
  
  public void setVehicleType(String vehicleType) {
    this.vehicleType = vehicleType;
  }
  
  public String getVehicleNo() {
    return vehicleNo;
  }
  
  public void setVehicleNo(String vehicleNo) {
    this.vehicleNo = vehicleNo;
  }
  
  public String getTransDocNo() {
    return transDocNo;
  }
  
  public void setTransDocNo(String transDocNo) {
    this.transDocNo = transDocNo;
  }
  
  public String getTransDocDate() {
    return transDocDate;
  }
  
  public void setTransDocDate(String transDocDate) {
    this.transDocDate = transDocDate;
  }
  
  public int getMainHsnCode() {
    return mainHsnCode;
  }
  
  public void setMainHsnCode(int mainHsnCode) {
    this.mainHsnCode = mainHsnCode;
  }
  
  public double getTotInvValue() {
    return totInvValue;
  }
  
  public void setTotInvValue(double totInvValue) {
    this.totInvValue = totInvValue;
  }
  
  public double getTransDistance() {
    return transDistance;
  }
  
  public void setTransDistance(double transDistance) {
    this.transDistance = transDistance;
  }
  
  public List<ItemDetails> getItemList()
  {
    return itemList;
  }
  
  public void setItemList(List<ItemDetails> itemList) {
    this.itemList = itemList;
  }
  
 
  public boolean validateFields(String refSer, String tranId, String siteCode,String logPath)//logPath added by nandkumar on 05/12/19
  {
    boolean flag = true;
    try
    {
      System.out.println("Inside Validation Fields:" + docType);
      
      if (userGstin.trim().length() <= 0) {
        String[] errorArray = getErrorMessageDescription("VTEW017").split(",");
        
        if (errorArray[0].equals("E")) {
          printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW017-" + errorArray[1] + "'", siteCode,logPath);
          flag = false;
        }
      }
      if (docType.trim().length() <= 0) {
        String[] errorArray = getErrorMessageDescription("VTEW001").split(",");
        
        if (errorArray[0].equals("E")) {
          printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW001-" + errorArray[1] + "'", siteCode,logPath);
          flag = false;
        }
      }
      







      if (fromGstin.trim().length() <= 0) {
        String[] errorArray = getErrorMessageDescription("VTEW003").split(",");
        
        if (errorArray[0].equals("E")) {
          printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW003-" + errorArray[1] + "'", siteCode,logPath);
          flag = false;
        }
      }
      if (fromPinCode == 0) {
        String[] errorArray = getErrorMessageDescription("VTEW004").split(",");
        
        if (errorArray[0].equals("E")) {
          printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW004-" + errorArray[1] + "'", siteCode,logPath);
          flag = false;
        }
      }
      if (fromStateCode == 0) {
        String[] errorArray = getErrorMessageDescription("VTEW005").split(",");
        
        if (errorArray[0].equals("E")) {
          printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW005-" + errorArray[1] + "'", siteCode,logPath);
          flag = false;
        }
      }
      if (toStateCode == 0) {
        String[] errorArray = getErrorMessageDescription("VTEW006").split(",");
        
        if (errorArray[0].equals("E")) {
          printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW006-" + errorArray[1] + "'", siteCode,logPath);
          flag = false;
        }
      }
      if (toGstin.trim().length() <= 0) {
        String[] errorArray = getErrorMessageDescription("VTEW007").split(",");
        
        if (errorArray[0].equals("E")) {
          printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW007-" + errorArray[1] + "'", siteCode,logPath);
          flag = false;
        }
      }
      if (toPincode == 0) {
        String[] errorArray = getErrorMessageDescription("VTEW008").split(",");
        
        if (errorArray[0].equals("E")) {
          printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW008-" + errorArray[1] + "'", siteCode,logPath);
          flag = false;
        }
      }
      if ((transMode == 1) && 
        (transporterId.trim().length() <= 0) && (vehicleNo.trim().length() <= 0)) {
        String[] errorArray = getErrorMessageDescription("VTEW009").split(",");
        
        if (errorArray[0].equals("E")) {
          printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW009-" + errorArray[1] + "'", siteCode,logPath);
          flag = false;
        }
        
        errorArray = getErrorMessageDescription("VTEW011").split(",");
        
        if (errorArray[0].equals("E")) {
          printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW011-" + errorArray[1] + "'", siteCode,logPath);
          flag = false;
        }
      }
      

      if (transMode != 1) {
        if (transDocDate.trim().length() <= 0) {
          String[] errorArray = getErrorMessageDescription("VTEW010").split(",");
          
          if (errorArray[0].equals("E")) {
            printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW010-" + errorArray[1] + "'", siteCode,logPath);
            flag = false;
          }
        }
        
        if (docNo.trim().length() <= 0) {
          String[] errorArray = getErrorMessageDescription("VTEW002").split(",");
          
          if (errorArray[0].equals("E")) {
            printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW002-" + errorArray[1] + "'", siteCode,logPath);
            flag = false;
          }
        }
      }
      








      if (transMode == 0) {
        String[] errorArray = getErrorMessageDescription("VTEW014").split(",");
        
        if (errorArray[0].equals("E")) {
          printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW014-" + errorArray[1] + "'", siteCode,logPath);
          flag = false;
        }
      }
      if (transDistance == 0.0D) {
        String[] errorArray = getErrorMessageDescription("VTEW015").split(",");
        
        if (errorArray[0].equals("E")) {
          printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW015-" + errorArray[1] + "'", siteCode,logPath);
          flag = false;
        }
      }
      if (transDistance > 4000.0D) {
        String[] errorArray = getErrorMessageDescription("VTEW016").split(",");
        
        if (errorArray[0].equals("E")) {
          printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW016-" + errorArray[1] + "'", siteCode,logPath);
          flag = false;
        }
      }
      


      Iterator<ItemDetails> itemListIterator = itemList.iterator();
      while (itemListIterator.hasNext()) {
        ItemDetails item = (ItemDetails)itemListIterator.next();
        
        int value = item.getHsnCode();
        System.out.println("hsn value:" + value);
        

        if (value == 0) {
          String[] errorArray = getErrorMessageDescription("VTEW012").split(",");
          
          if (errorArray[0].equals("E")) {
            printLog("EWAY Bill failed", "'REF_SER-" + refSer + "','Tran-Id-" + tranId + "','Error-Code-VTEW012-" + errorArray[1] + "'", siteCode,logPath);
            flag = false;


          }
          


        }
        


      }
      



    }
    catch (Exception e)
    {


      printLog("EWAY Bill failed", e.getMessage(), siteCode,logPath);
    }
    return flag;
  }
  



  private void printLog(String title, String msg, String siteCode,String logPath)//logPath added by nandkumar gadkari on 05/12/19
  {
    String logFile = "";
    String logDir = "";
    File logFileDir = null;
    FileWriter fileWriter = null;
    //String logPath = "";
    DistCommon distCommon = new DistCommon();
    Connection conn = null;
    String loginCode="",transDB="";
    try
    {
    	/*E12GenericUtility genericUtility= new  E12GenericUtility();
    	loginCode=genericUtility.getValueFromXTRA_PARAMS(xtraParams, "loginCode");
    	DBAccessEJB dbAccess = new DBAccessEJB();//added by nandkumar gadkari on 05/12/19
    	UserInfoBean userInfo = dbAccess.createUserInfo(loginCode);//added by nandkumar gadkari on 05/12/19
    	 transDB       = userInfo.getTransDB();
		 ConnDriver connDriver = new ConnDriver();
      conn = connDriver.getConnectDB(transDB);*/
      String logFileName = "";
      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
      logFileName = "EWAYBILL_" + siteCode + "_" + sdf.format(new Date());
      
      System.out.println("Inside the print log method value");
      
    //  logPath = distCommon.getDisparams("999999", "EWAY_LOG_PATH", conn);
      logDir = CommonConstants.JBOSSHOME +File.separator+logPath + File.separator + File.separator + "EWAYBILL";
      System.out.println("Log direction: " + logDir);
      
      logFileDir = new File(logDir);
      
      if (!logFileDir.exists())
      {
        logFileDir.mkdirs();
      }
      
      logFile = logDir + File.separator + logFileName + ".log";
      
      SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      Date now = new Date();
      String strDate = sdfDate.format(now);
      
      fileWriter = new FileWriter(logFile, true);
      
      fileWriter.write("\r\n");
      fileWriter.write(strDate + " " + "[ERROR] " + msg);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      
      printLog("STDOUT", "Inside Exception [getLog]>>" + ex.toString(), siteCode,logPath);
      


     /* try
      {
        conn.close();
        conn = null;
      }
      catch (Exception localException1) {}*/
      try
      {
        if (fileWriter != null)
        {
          fileWriter.flush();
          fileWriter.close();
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    finally
    {
      /*try
      {
        conn.close();
        conn = null;
      }
      catch (Exception localException2) {}*/
      try
      {
        if (fileWriter != null)
        {
          fileWriter.flush();
          fileWriter.close();
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }
  
  String checkNull(String value) {
    if (value == null) {
      value = "";
    }
    return value;
  }
  

  public String getErrorMessageDescription(String errorCode)
    throws RemoteException, ITMException
  {
    Statement stmt = null;
    ResultSet rs = null;
    Connection conn = null;
    String msgType = "";
    String msgString = "";
    try
    {
      ConnDriver connDriver = new ConnDriver();
      conn = connDriver.getConnectDB();
      
      String sqlQuery = "SELECT MSG_TYPE,MSG_STR,MSG_DESCR,MSG_OPT,MSG_TIME,ALARM,ERR_SOURCE FROM MESSAGES WHERE MSG_NO='" + errorCode + "'";
      stmt = conn.createStatement();
      rs = stmt.executeQuery(sqlQuery);
      if (rs.next())
      {
        msgType = rs.getString("MSG_TYPE");
        msgString = rs.getString("MSG_STR");
      }
      else
      {
        msgString = "Message Not Defined >>";
        msgType = "E";
        msgString = "";
      }
      stmt.close();
      stmt = null;
      rs.close();
      rs = null;

    }
    catch (SQLException e)
    {
      System.out.println("Exception :EwaybillPrc :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
      throw new ITMException(e);
    }
    catch (Exception e)
    {
      System.out.println("Exception :EwaybillPrc :getData(String xmlString, String xmlString2, String windowName, String xtraParams):" + e.getMessage() + ":");
      throw new ITMException(e);
    }
    finally
    {
      try
      {
        conn.close();
        conn = null;
      }
      catch (Exception localException1) {}
    }
    return msgType + "," + msgString;
  }
}