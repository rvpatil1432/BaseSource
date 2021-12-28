package ibase.webitm.ejb.dis.adv;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ibase.utility.E12GenericUtility;
import ibase.utility.UserInfoBean;
import ibase.webitm.ejb.ActionHandlerEJB;

public class PorderAmtValidation extends ActionHandlerEJB
{
	public String validateTotalAmount(String purcOrder,String userInfo) throws SQLException
	{
		double totalAmount = 0;
		E12GenericUtility e12GenericUtility = new E12GenericUtility();
		
		
		Connection connection = null;
		UserInfoBean userInfoBean;
		
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try 
		{
			userInfoBean = new UserInfoBean(userInfo);
			setUserInfo(userInfoBean);
			connection = getConnection();
			connection.setAutoCommit(false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		try 
		{
			purcOrder = e12GenericUtility.checkNull(purcOrder);
			String sqlQuery = "SELECT TOT_AMT FROM PORDER WHERE PURC_ORDER = ?";
			preparedStatement = connection.prepareStatement(sqlQuery);
			preparedStatement.setString(1, purcOrder);
			resultSet = preparedStatement.executeQuery();
			
			if(resultSet.next())
			{
				totalAmount = resultSet.getDouble("TOT_AMT");
			}
			
			if(totalAmount >= 1)
			{
				return ""+1;//Go to next approver
			}else
			{
				return ""+0;//Call PorderConf:: confirm.
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally
		{
			if(resultSet != null)	{	resultSet.close();		resultSet = null;	}
			if(preparedStatement != null) {	preparedStatement.close();	preparedStatement = null;	}
			if(connection != null)
			{
				connection.close();
				connection = null;
			}
		}
		
		return ""+totalAmount;
	}
}
