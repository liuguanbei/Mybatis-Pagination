package cn.bdqn.mybatis.plugin;


/**
 * 抽象方言类
 * @author 学术部
 * */
public abstract class AbstractDialect implements IDialect{
	
	/**用来匹配ORDER BY子句的正则表达式*/
	public static final String SORT_CLAUSE_REGEXP=".*order\\s+by(?i)";
	
	/**
	 * 构建查询总行数的SQL语句
	 * @param sql 原始SQL语句
	 * @return 查询总行数的SQL语句
	 * */
	@Override
	public String buildCountSQL(String sql) {
		if(sql.matches(SORT_CLAUSE_REGEXP)){
			sql=sql.replaceAll(SORT_CLAUSE_REGEXP,"");
		}
		return "SELECT COUNT(*) FROM ("+sql+") t";
	}
	
}
