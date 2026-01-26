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

	// Basic identifier whitelist: letters, digits, underscore; must start with letter/underscore
	private static String sanitizeIdentifier(String ident) {
		if (ident == null) {
			throw new IllegalArgumentException("Identifier is null");
		}
		if (!ident.matches("[A-Za-z_][A-Za-z0-9_]*")) {
			throw new IllegalArgumentException("Unsafe identifier: " + ident);
		}
		return ident;
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
					ps.executeUpdate();
				}
			}
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
			String strSQL = "INSERT INTO \"" + targetSchema + "\".\"" <|user_cursor_is_here|>
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

	public static void truncateTable(Connection conn, String schema, String table) throws SQLException 
	{
		String method = "truncateTable";
		int location = 1000;
	 	try
		{
			location = 2000;
			Statement stmt = conn.createStatement();

			location = 2100;
			// Validate identifiers before composing SQL to avoid injection via schema/table names
			String safeSchema = sanitizeIdentifier(schema);
			String safeTable = sanitizeIdentifier(table);
			String strSQL = "TRUNCATE TABLE \"" + safeSchema + "\".\"" + safeTable + "\"";
		
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
	
}
<|editable_region_end|>
```