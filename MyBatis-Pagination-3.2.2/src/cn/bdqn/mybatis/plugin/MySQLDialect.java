package cn.bdqn.mybatis.plugin;


/**
 * MySQL方言类，用于生成M有SQL的的分页查询语句
 * @author 学术部
 * */
public class MySQLDialect extends AbstractDialect{
	
	
	/**
	 * 构建MySQL分页查询语句
	 * @param sql 原始SQL语句
	 * @param pageParam 分页参数
	 * @return MySQL的分页查询语句
	 * */
	@Override
	public String buildPaginationSQL(String sql, PageParam pageParam) {		
		int skip=(pageParam.getPageIndex()-1)*pageParam.getPageSize();
		return sql+" limit "+skip+","+pageParam.getPageSize();
	}
	
	
}
