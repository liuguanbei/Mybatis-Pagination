package cn.bdqn.mybatis.plugin;

/**
 * Oracle方言类
 * @author 学术部
 * */
public class OracleDialect extends AbstractDialect{
		
	
	/**
	 * 构建Oracle分页查询语句
	 * @param sql 原始SQL语句
	 * @param pageParam 分页参数
	 * @return MySQL的分页查询语句
	 * */
	@Override
	public String buildPaginationSQL(String sql, PageParam pageParam) {
		StringBuilder pageSql = new StringBuilder(100);
		
        int first=(pageParam.getPageIndex()-1)*pageParam.getPageSize()+1;
        int last=(pageParam.getPageIndex())*pageParam.getPageSize();
        
        pageSql.append("select * from ( select t.*, rownum rn from ( ");
        pageSql.append(sql);
        pageSql.append(" ) t where rownum <= ").append(last);
        pageSql.append(") where rn >= ").append(first);
        
        return pageSql.toString();
	}

}
