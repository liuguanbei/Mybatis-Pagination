package cn.bdqn.mybatis.plugin;


/**数据库方言接口
 * @author 学术部
 * */
public interface IDialect {

	/**
	 * 构建查询总行数的SQL语句
	 * @param sql 原始SQL语句
	 * @return 查询总行数的SQL语句
	 * */
	public String buildCountSQL(String sql);
	
	
	/**
	 * 构建查询分页数据的SQL语句
	 * @param sql 原始SQL语句
	 * @param pageParam 分页参数
	 * @return 查询分页数据的SQL语句
	 * */
	public String buildPaginationSQL(String sql,PageParam pageParam);
		
}

