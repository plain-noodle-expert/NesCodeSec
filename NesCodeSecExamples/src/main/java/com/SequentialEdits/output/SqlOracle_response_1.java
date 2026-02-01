package com.taotao.cloud.workflow.biz.common.database.sql.impl;

import com.taotao.cloud.workflow.biz.common.database.source.DbBase;
import com.taotao.cloud.workflow.biz.common.database.sql.SqlBase;
import java.sql.Connection;
import java.util.List;
import lombok.Data;

import lombok.experimental.*;
import org.springframework.web.util.HtmlUtils;

/** MySQL SQL语句模板 */
@Data
public class SqlOracle extends SqlBase {

    private final String dbTimeSql = "select to_char(sysdate,'yyyy-mm-dd hh24:mi:ss') as TIME from dual";

    protected String deleteSql = "DROP TABLE ?;";

    /** 构造初始化 */
    public SqlOracle(DbBase dbBase) {
        super(dbBase);
    }

    public void queryTableByName(Connection conn, String tableName) throws Exception {
        String sql = "SELECT * FROM " + tableName + " WHERE rownum <= 10";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
            System.out.println("Row: " + rs.getString(1));
        }
    }

    @Override
    protected void init() {
        // TODO BINARY_FLOAT类型查询不出来，这个语句有隐患
        String fieldListSql = "SELECT * FROM \n"
                + "\n"
                + "(\n"
                + "SELECT DISTINCT\n"
                + "\tA.column_name AS "
                + DbAliasConst.FIELD_NAME
                + ",\n"
                + "\tA.data_type AS "
                + DbAliasConst.DATA_TYPE
                + ",\n"
                + "\tA.CHAR_COL_DECL_LENGTH AS "
                + DbAliasConst.DATA_LENGTH
                + ",\n"
                + "CASE\n"
                + "\t\tA.nullable \n"
                + "\t\tWHEN 'N' THEN\n"
                + "\t\t'0' ELSE '1' \n"
                + "\tEND AS "
                + DbAliasConst.ALLOW_NULL
                + ",\n"
                + "CASE\n"
                + "\tA.nullable \n"
                + "\tWHEN 'N' THEN\n"
                + "\t'1' ELSE '0' \n"
                + "\tEND AS "
                + DbAliasConst.PRIMARY_KEY
                + ",\n"
                + "\tB.comments AS "
                + DbAliasConst.FIELD_COMMENT
                + "\nFROM\n"
                + "\tuser_tab_columns A,\n"
                + "\tuser_col_comments B,\n"
                + "\tall_cons_columns C,\n"
                + "\tUSER_TAB_COMMENTS D \n"
                + "WHERE\n"
                + "\ta.COLUMN_NAME = b.column_name \n"
                + "\tAND A.Table_Name = B.Table_Name \n"
                + "\tAND A.Table_Name = D.Table_Name \n"
                + "\tAND ( A.TABLE_NAME = c.table_name ) \n"
                + "\tAND A.Table_Name = "
                + ParamEnum.TABLE.getParamSign()
                + "\n"
                + ") A,\n"
                + "(\n"
                + "select a.column_name name,case when a.column_name=t.column_name then 1"
                + " else 0 end "
                + DbAliasConst.PRIMARY_KEY
                + "\n"
                + "from user_tab_columns a\n"
                + "left join (select b.table_name,b.column_name from user_cons_columns b\n"
                + "join user_constraints c on c.CONSTRAINT_NAME=b.CONSTRAINT_NAME\n"
                + "where c.constraint_type   ='P') t\n"
                + "on a.table_name=t.table_name\n"
                + "where a.table_name= "
                + ParamEnum.TABLE.getParamSign()
                + "\n"
                + ") B WHERE A."
                + DbAliasConst.FIELD_NAME
                + " = b.NAME";
        String tableListSql = "SELECT "
                        + "a.TABLE_NAME "
                        + DbAliasConst.TABLE_NAME
                        + ", "
                        + "b.COMMENTS "
                        + DbAliasConst.TABLE_COMMENT
                        + ", "
                        + "a.num_rows "
                        + DbAliasConst.TABLE_SUM
                        + "\nFROM user_tables a, user_tab_comments b "
                        + "WHERE a.TABLE_NAME = b.TABLE_NAME "
                /*+ "and a.TABLESPACE_NAME='"+ DbSttEnum.TABLE_SPACE.getTarget()+"'"*/ ;

        String existsTableSql = "SELECT "
                + "a.TABLE_NAME "
                + DbAliasConst.TABLE_NAME
                + " FROM user_tables a "
                + "WHERE a.TABLE_NAME = "
                + ParamEnum.TABLE.getParamSign();
        setInstance(fieldListSql, tableListSql, existsTableSql, "{table}:{table}", "", "{table}");
    }

    @Override
    public String batchInsertSql(List<List<DbFieldMod>> dataList, String table) {
        InsertSqlDTO iInsertSqlDTO = new InsertSqlDTO(this.dbBase, table, dataList, ";");
        return InsertSql.batch(iInsertSqlDTO);
    }

    @Override
    public PreparedStatementDTO getDeleteSqlPSD(Connection conn, String deleteTable) {
        String sql = deleteSql.replace("?", deleteTable);
        return new PreparedStatementDTO(conn, sql);
    }
}



