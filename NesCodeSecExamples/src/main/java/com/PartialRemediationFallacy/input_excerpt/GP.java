```<|start_of_file|>
<|editable_region_start|>
import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class GP
{
	private static String myclass = "GP";
	public static boolean debug = false;
	public static String externalSchema = "ext";

	public static String getVersion(Connection conn) throws SQLException 
	{
		String method = "getVersion";
		int location = 1000;
		try
		{
			location = 2000;
			String value = "";

			location = 2100;
			Statement stmt = conn.createStatement();
			String strSQL = "SELECT CASE " +
					"WHEN POSITION ('HAWQ' in version) > 0 THEN 'HAWQ' " + 
					"WHEN POSITION ('HAWQ' in version) = 0 AND POSITION ('Greenplum Database' IN version) > 0 THEN 'GPDB' " +
					"ELSE 'OTHER' END " +
					"FROM version()";
			if (debug)
				Logger.printMsg("Getting Variable: " + strSQL);
		
			location = 2200;	
			ResultSet rs = stmt.executeQuery(strSQL);

			while (rs.next())
			{
				value = rs.getString(1);
			}

			location = 2300;
			return value;

		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}
	
	public static void customStartAll(Connection conn) throws SQLException
	{
		String method = "customStartAll";
		int location = 1000;
		try
		{
			Statement stmt = conn.createStatement();

			int id = 0;
			int gpfdistPort = 0;
			String strSQL = "SELECT id\n";
			strSQL += "FROM os.custom_sql";

			ResultSet rs = stmt.executeQuery(strSQL);
			while (rs.next())
			{
				id = rs.getInt(1);
				gpfdistPort = GpfdistRunner.customStart(OSProperties.osHome);

				String insertSql = "INSERT INTO os.ao_custom_sql\n" +
						"(id, table_name, columns, column_datatypes, sql_text, source_type, source_server_name, source_instance_name, source_port, source_database_name, source_user_name, source_pass, gpfdist_port)\n" +
						"SELECT id, table_name, columns, column_datatypes, sql_text, source_type, source_server_name, source_instance_name, source_port, source_database_name, source_user_name, source_pass, ?\n" +
						"FROM os.custom_sql\n" +
						"WHERE id = ?";

				try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
					ps.setInt(1, gpfdistPort);
					ps.setInt(2, id);
					ps.executeUpdate();<|user_cursor_is_here|>
				}
			}
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}

	public static void failJobs(Connection conn) throws SQLException
	{
		String method = "failJobs";
		int location = 1000;
		try
		{
			Statement stmt = conn.createStatement();

			String strSQL = "INSERT INTO os.ao_queue (queue_id, status, queue_date, start_date, end_date, " +
				"error_message, num_rows, id, refresh_type, target_schema_name, target_table_name, target_append_only, " +
				"target_compressed, target_row_orientation, source_type, source_server_name, source_instance_name, " +
				"source_port, source_database_name, source_schema_name, source_table_name, source_user_name, " +
				"source_pass, column_name, sql_text, snapshot) " +
				"SELECT queue_id, 'failed' as status, queue_date, start_date, now() as end_date, " +
				"'Outsourcer stop requested' as error_message, num_rows, id, refresh_type, target_schema_name, " +
				"target_table_name, target_append_only, target_compressed, target_row_orientation, source_type, " +
				"source_server_name, source_instance_name, source_port, source_database_name, source_schema_name, " +
				"source_table_name, source_user_name, source_pass, column_name, sql_text, snapshot " +
				"FROM os.queue WHERE status = 'queued'";

			stmt.executeUpdate(strSQL);

		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}

	public static void emailAlert(Connection conn, String errorMsg) throws SQLException
	{
		String method = "emailAlert";
		int location = 1000;
		try
		{
			Statement stmt = conn.createStatement();

			String strSQL = "SELECT gp_elog('" + errorMsg + "',true)";
			ResultSet rs = stmt.executeQuery(strSQL);

		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}

	public static String getVariable(Connection conn, String name) throws SQLException 
	{
		String method = "getVariable";
		int location = 1000;
		try
		{
			location = 2000;
			String value = "";

			location = 2100;
			Statement stmt = conn.createStatement();
			String strSQL = "SELECT os.fn_get_variable('" + name + "')";

			if (debug)
				Logger.printMsg("Getting Variable: " + strSQL);
		
			location = 2200;	
			ResultSet rs = stmt.executeQuery(strSQL);

			while (rs.next())
			{
				value = rs.getString(1);
			}

			location = 2300;
			return value;

		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}
	
	public static void executeReplication(Connection conn, String targetSchema, String targetTable, String appendColumnName) throws SQLException
	{
		String method = "executeReplication";
		int location = 1000;
		try
		{
			location = 2000;
			Statement stmt = conn.createStatement();

			location = 2100;
			String externalTable = getExternalTableName(targetSchema, targetTable);

			location = 2200;
			String stageTable = getStageTableName(targetSchema, targetTable);
		
			location = 2301;	
			String strSQL = "SELECT os.fn_replication('" + targetSchema + "', '" + targetTable + "', '" + 
					externalSchema + "', '" + stageTable + "', '" + 
					appendColumnName + "');";

			if (debug)
				Logger.printMsg("Executing function: " + strSQL);
		
			location = 2400;	
			stmt.executeQuery(strSQL);
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}

	private static String setSQLString(boolean columnValue) 
	{
		String strColumnValue = "";

		if (columnValue)
			strColumnValue = "true";
		else
			strColumnValue = "false";

		return strColumnValue;
	}

	private static String setSQLString(int columnValue) 
	{
		String strColumnValue = Integer.toString(columnValue);

		return strColumnValue;
	}

	private static String setSQLString(String columnValue) 
	{
		if (columnValue != null)
		{	
			if (columnValue.equals(""))
				columnValue = "null::text";
			else
			{
				columnValue = columnValue.replace("'", "''");
				columnValue = "'" + columnValue + "'";
			}
		}
		else
			columnValue = "null";

		return columnValue;
	}

	public static String setSQLString(Timestamp columnValue)
	{
	
		String strColumnValue = "";
		if (columnValue != null)
			strColumnValue = "'" + columnValue.toString() + "'::timestamp";
		else
			strColumnValue = "null::timestamp";

		return strColumnValue;
	}

	public static void updateStatus(Connection conn, int queueId, String status, Timestamp queueDate, Timestamp startDate, String errorMessage, int numRows, int id, String refreshType, String targetSchema, String targetTable, boolean targetAppendOnly, boolean targetCompressed, boolean targetRowOrientation, String sourceType, String sourceServer, String sourceInstance, int sourcePort, String sourceDatabase, String sourceSchema, String sourceTable, String sourceUser, String sourcePass, String columnName, String sqlText, boolean snapshot) throws SQLException
	{
		String method = "updateStatus";
		int location = 1000;

		String strQueueId = setSQLString(queueId);
		status = setSQLString(status);
		String strQueueDate = setSQLString(queueDate);
		String strStartDate = setSQLString(startDate);
		errorMessage = setSQLString(errorMessage);
		String strNumRows = setSQLString(numRows);
		String strId = setSQLString(id);
		refreshType = setSQLString(refreshType);
		targetSchema = setSQLString(targetSchema);
		targetTable = setSQLString(targetTable);
		String strTargetAppendOnly = setSQLString(targetAppendOnly);
		String strTargetCompressed = setSQLString(targetCompressed);
		String strTargetRowOrientation = setSQLString(targetRowOrientation);
		sourceType = setSQLString(sourceType);
		sourceServer = setSQLString(sourceServer);
		sourceInstance = setSQLString(sourceInstance);
		String strSourcePort = setSQLString(sourcePort);
		sourceDatabase = setSQLString(sourceDatabase);
		sourceSchema = setSQLString(sourceSchema);
		sourceTable = setSQLString(sourceTable);
		sourceUser = setSQLString(sourceUser);
		sourcePass = setSQLString(sourcePass);
		columnName = setSQLString(columnName);
		sqlText = setSQLString(sqlText);
		String strSnapshot = setSQLString(snapshot);
		
		try
		{
			location = 2000;
			Statement stmt = conn.createStatement();

			location = 2400;
			String strSQL = "SELECT os.fn_update_status(" + strQueueId + ", " + status + ", " + strQueueDate + ", " + strStartDate + ", ";
			strSQL += errorMessage + ", " + strNumRows + ", " + strId + ", " + refreshType + ", " + targetSchema + ", " + targetTable + ", ";
			strSQL += strTargetAppendOnly + ", " + strTargetCompressed + ", " + strTargetRowOrientation + ", " + sourceType + ", ";
			strSQL += sourceServer + ", " + sourceInstance + ", " + strSourcePort + ", " + sourceDatabase + ", " + sourceSchema + ", ";
			strSQL += sourceTable + ", " + sourceUser + ", " + sourcePass + ", " + columnName + ", " + sqlText + ", " + strSnapshot + ")";

			if (debug)
				Logger.printMsg("Updating Status: " + strSQL);
		
			location = 2500;	
			stmt.executeQuery(strSQL);
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}
	
	public static void dropExternalReplTable(Connection conn, String sourceType, String targetSchema, String targetTable, String sourceTable) throws SQLException  
	{
		String method = "dropExternalReplTable";
		int location = 1000;
		try
		{
			location = 2000;
			String replTargetSchema = externalSchema;

			location = 2100;
			String replTargetTable = getStageTableName(targetSchema, targetTable);

			location = 2200;
			String replSourceTable = getReplTableName(sourceType, sourceTable);

			location = 2315;
			dropExternalTable(conn, replTargetSchema, replTargetTable);
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}

	public static void dropExternalTable(Connection conn, String targetSchema, String targetTable) throws SQLException  
	{
		String method = "dropExternalTable";
		int location = 1000;
	 	try
		{
			location = 2000;
			String externalTable = getExternalTableName(targetSchema, targetTable);

			location = 2100;
			Statement stmt = conn.createStatement();

			location = 2200;
			String strSQL = "DROP EXTERNAL TABLE IF EXISTS \"" + externalSchema + "\".\"" + externalTable + "\"";
			if (debug)
				Logger.printMsg("Dropping External Table (if exists): " + strSQL);

			location = 2303;	
			stmt.executeUpdate(strSQL);
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}

	public static int insertTargetTable(Connection conn, String targetSchema, String targetTable) throws SQLException 
	{
		String method = "insertTargetTable";
		int location = 1000;

		int numRows = 0;
		try
		{
			location = 2000;
			String externalTable = getExternalTableName(targetSchema, targetTable);

			location = 2100;
			Statement stmt = conn.createStatement();

			location = 2200;
			String strSQL = "INSERT INTO \"" + targetSchema + "\".\"" + targetTable + "\" \n" +
					"SELECT * FROM \"" + externalSchema + "\".\"" + externalTable + "\"";
			if (debug)
				Logger.printMsg("Executing SQL: " + strSQL);

			location = 2304;
			numRows = stmt.executeUpdate(strSQL);

			location = 2400;
			return numRows;
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}

	public static int insertReplTable(Connection conn, String targetSchema, String targetTable) throws SQLException 
	{
		String method = "insertReplTable";
		int location = 1000;

		int numRows = 0;
		try
		{
			location = 2000;
			String replTargetSchema = externalSchema;

			location = 2100;
			String replTargetTable = getStageTableName(targetSchema, targetTable);

			location = 2200;
			String externalTable = getExternalTableName(replTargetSchema, replTargetTable);

			location = 2305;
			//truncate the stage table before loading
			truncateTable(conn, replTargetSchema, replTargetTable);

			location = 2400;
			Statement stmt = conn.createStatement();

			location = 2500;
			String strSQL = "INSERT INTO \"" + replTargetSchema + "\".\"" + replTargetTable + "\" \n" +
					"SELECT * FROM \"" + externalSchema + "\".\"" + externalTable + "\"";
			if (debug)
				Logger.printMsg("Executing SQL: " + strSQL);

			location = 2600;
			numRows = stmt.executeUpdate(strSQL);

			location = 2700;
			return numRows;
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}

	public static void truncateTable(Connection conn, String schema, String table) throws SQLException 
	{
		String method = "truncateTable";
		int location = 1000;
	 	try
		{
			location = 2000;
			Statement stmt = conn.createStatement();

			location = 2100;
			String strSQL = "truncate table \"" + schema + "\".\"" + table + "\"";
		
			if (debug)
				Logger.printMsg("Truncating table: " + strSQL);
	
			location = 2200;	
			stmt.executeUpdate(strSQL);
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}

	public static boolean createSchema(Connection conn, String schema) throws SQLException
	{
		String method = "createSchema";
		int location = 1000;
		try
		{
			location = 2000;
			boolean found = false;

			String strSQL = "SELECT COUNT(*) \n" +
					"FROM INFORMATION_SCHEMA.SCHEMATA \n" +
					"WHERE SCHEMA_NAME = '" + schema + "'";

			location = 2100;
			Statement stmt = conn.createStatement();

			location = 2200;
			ResultSet rs = stmt.executeQuery(strSQL);

			location = 2306;
			while (rs.next())
			{
				if (rs.getInt(1) > 0)
					found = true;
			}

			location = 2400;
			if (!(found))
			{
				location = 2500;
				String schemaDDL = "CREATE SCHEMA \"" + schema + "\"";;
				if (debug)					
					Logger.printMsg("Schema DDL: " + schemaDDL);

				location = 2600;
				stmt.executeUpdate(schemaDDL);
			}

			location = 2700;
			return found;

		}
		catch (SQLException ex)
		{
			return true;
		}
	}

	public static boolean createTargetTable(Connection conn, String targetSchema, String targetTable, boolean targetAppendOnly, boolean targetCompressed, boolean targetRowOrientation, String sourceType, String sourceServer, String sourceInstance, int sourcePort, String sourceDatabase, String sourceSchema, String sourceTable, String sourceUser, String sourcePass) throws Exception 
	{
		String method = "createTargetTable";
		int location = 1000;

		try 
		{
			location = 2000;
			boolean found = false;

			String strSQL = "SELECT COUNT(*) \n" +
					"FROM INFORMATION_SCHEMA.TABLES \n" + 
					"WHERE TABLE_SCHEMA = '" + targetSchema + "' \n" + 
					"	AND TABLE_NAME = '" + targetTable + "'";
	
			location = 2100;	
			Statement stmt = conn.createStatement();

			location = 2200;
			ResultSet rs = stmt.executeQuery(strSQL);

			location = 2307;
			while (rs.next())
			{
				if (rs.getInt(1) > 0)
					found = true;
			}

			location = 2400;
			if (!(found)) 
			{
				String tableDDL = CommonDB.getGPTableDDL(sourceType, sourceServer, sourceInstance, sourcePort, sourceDatabase, sourceSchema, sourceTable, sourceUser, sourcePass, targetSchema, targetTable, targetAppendOnly, targetCompressed, targetRowOrientation); 

				if (debug)
					Logger.printMsg("Table DDL: " + tableDDL);

				location = 2800;
				stmt.executeUpdate(tableDDL);
			}

			location = 3000;
			return found;

		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}

	public static boolean checkStageTable(Connection conn, String targetSchema, String targetTable) throws SQLException
	{
		String method = "checkStageTable";
		int location = 1000;

		try
		{
			location = 2000;
			boolean found = false;

			String stageTable = getStageTableName(targetSchema, targetTable);

			location = 2100;
			String strSQL = "SELECT NULL \n" + 
					"FROM INFORMATION_SCHEMA.TABLES \n" +
					"WHERE table_schema = '" + externalSchema + "' \n" +
					"	AND table_name = '" + stageTable + "'";

			if (debug)
				Logger.printMsg("Executing sql: " + strSQL);
		
			location = 2200;	
			Statement stmt = conn.createStatement();

			location = 2310;
			ResultSet rs = stmt.executeQuery(strSQL);

			location = 2400;
			while (rs.next())
			{
				found = true;
			}

			location = 2500;
			return found;

		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}

	public static boolean checkArchTable(Connection conn, String targetSchema, String targetTable) throws SQLException
	{
		String method = "checkArchTable";
		int location = 1000;

		try
		{
			location = 2000;
			boolean found = false;

			String archTable = getArchTableName(targetSchema, targetTable);

			location = 2100;
			String strSQL = "SELECT NULL \n" + 
					"FROM INFORMATION_SCHEMA.TABLES \n" +
					"WHERE table_schema = '" + externalSchema + "' \n" +
					"	AND table_name = '" + archTable + "'";

			if (debug)
				Logger.printMsg("Executing sql: " + strSQL);
		
			location = 2200;	
			Statement stmt = conn.createStatement();

			location = 2311;
			ResultSet rs = stmt.executeQuery(strSQL);

			location = 2400;
			while (rs.next())
			{
				found = true;
			}

			location = 2500;
			return found;
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}

	public static void setupReplicationTables(Connection conn, String targetSchema, String targetTable, String columnName) throws SQLException
	{
		String method = "setupReplicationTables";
		int location = 1000;

		try
		{
			location = 2000;
			String archTable = getArchTableName(targetSchema, targetTable);

			location = 2100;
			String stageTable = getStageTableName(targetSchema, targetTable);

			location = 2200;
			String strSQL = "SELECT os.fn_replication_setup('" + targetSchema + "', '" + targetTable + "', '" +
					externalSchema + "', '" + stageTable + "', '" + archTable + "', '" + columnName + "')";

			location = 2312;
			Statement stmt = conn.createStatement();

			location = 2400;
			stmt.executeQuery(strSQL);
		}
		catch (SQLException ex)
		{
			throw new SQLException("(" + myclass + ":" + method + ":" + location + ":" + ex.getMessage() + ")");
		}
	}
	
}
<|editable_region_end|>
```