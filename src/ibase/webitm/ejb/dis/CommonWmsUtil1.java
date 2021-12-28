package ibase.webitm.ejb.dis;
import ibase.utility.CommonConstants;
import ibase.webitm.utility.ITMException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
public  class CommonWmsUtil1 {
	
        public static  int ALL_PTCN_EX_COUNT;
        
        public CommonWmsUtil1()
    	{
    	}

    	public static CommonWmsUtil1 getInstance()
    	{
    		return new ibase.webitm.ejb.dis.CommonWmsUtil1();
    	}
    	
      //Changed By Pragyan 11-AUG-14 To check and Facility Master implementation.start
    	public boolean isValFacLocation(String locCode,String siteCode,Connection conn) throws ITMException
    	{
    		PreparedStatement pstmt=null;
            ResultSet rs = null;
            double partialQty=0.0;
            double packSize=0.0;
            boolean isValidLocation = false;
            int count = 0;
            String sql="";
            
    		try {
    			//sql ="SELECT FN_CHECK_VALID_LOC(?,?) FROM DUAL";
    			sql ="SELECT FN_CHECK_VALID_LOC('"+siteCode+"','"+locCode+"') FROM DUAL";//For Support DB2 database Change by chandrashekar 0n 14-Apr-2015
    			pstmt =conn.prepareStatement(sql);
    			//pstmt.setString(1, siteCode);
    			//pstmt.setString(2, locCode);

    			rs = pstmt.executeQuery();
    			
    			if(rs.next())
    			{
    				count = rs.getInt(1);
    			}
    			
    			if(count > 0)
    			{
    				isValidLocation = true;
    			}
    			
    			
    			if(rs != null)
    			{
    				rs.close();
    				rs=null;
    			}
    			
    			if(pstmt != null)
    			{
    				pstmt.close();
    				pstmt=null;
    			}
    			
    			
    			
    			
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			throw new ITMException(e); 
    		}
    		
    		return isValidLocation;
    	}
  // Start changed by Pragyan on 19-SEP-14  [W14FSUN003]	
    	public String checkNull( String inputVal )
    	{
    		if ( inputVal == null )
    		{
    			inputVal = "";
    		}
    		return inputVal;
    	}
    	
    	public String getFacilityCode(String siteCode,Connection conn) throws ITMException
    	{
    		PreparedStatement pstmt=null;
            ResultSet rs = null;
            int count = 0;
            String facilityCode = "";
            
    		try {
    			String sql ="SELECT facility_code FROM site where site_code = ?";
    			pstmt =conn.prepareStatement(sql);
    			pstmt.setString(1, siteCode);
    			

    			rs = pstmt.executeQuery();
    			
    			if(rs.next())
    			{
    				facilityCode = checkNull(rs.getString(1));
    			}
    			  			
    			
    			if(rs != null)
    			{
    				rs.close();
    				rs=null;
    			}
    			
    			if(pstmt != null)
    			{
    				pstmt.close();
    				pstmt=null;
    			}
    			
    			
    			
    			
    		} 
    		catch (Exception e) 
    		{
    			// TODO Auto-generated catch block
    			throw new ITMException(e); 
    		}
    		
    		return facilityCode;
    	}
    	//Changed By Pragyan 11-AUG-14 To check and Facility Master implementation.end
    // End changed by Pragyan on 19-SEP-14   [W14FSUN003]	
       
}
