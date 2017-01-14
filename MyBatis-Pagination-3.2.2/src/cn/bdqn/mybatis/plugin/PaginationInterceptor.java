package cn.bdqn.mybatis.plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;



import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.RowBounds;
/**
 * 分页拦截器
 * @author 学术部
 * */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class})})
public class PaginationInterceptor implements Interceptor{

	private static final Log logger = LogFactory.getLog(PaginationInterceptor.class);
	private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
   
    private IDialect dialect;
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
    	//获得拦截器链底层目标StatementHandler
        StatementHandler statementHandler=getStatementHandler(invocation);
        MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY,DEFAULT_OBJECT_WRAPPER_FACTORY);
   
        BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");
        Object parameterObject = boundSql.getParameterObject();
        MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue("delegate.mappedStatement");
        
        if(parameterObject!=null){
        	MetaObject metaParam=MetaObject.forObject(parameterObject, DEFAULT_OBJECT_FACTORY,DEFAULT_OBJECT_WRAPPER_FACTORY);
        	if(metaParam.hasGetter("pageParam")){
        		PageParam pageParam = (PageParam) metaParam.getValue("pageParam");
    	        if(pageParam!=null){
    	        	String sql = boundSql.getSql();
    	            // 重写sql
    	            String pageSql = dialect.buildPaginationSQL(sql,pageParam);
    	            metaStatementHandler.setValue("delegate.boundSql.sql", pageSql);
    	            // 采用物理分页后，就不需要mybatis的内存分页了，所以重置下面的两个参数
    	            metaStatementHandler.setValue("delegate.rowBounds.offset", RowBounds.NO_ROW_OFFSET);
    	            metaStatementHandler.setValue("delegate.rowBounds.limit", RowBounds.NO_ROW_LIMIT);
    	            Connection connection = (Connection) invocation.getArgs()[0];
    	            // 重设分页参数里的总页数等
    	            setPageParameter(sql, connection, mappedStatement, boundSql, pageParam);            
    	        }
        	        
        	}
        }                       
        // 将执行权交给下一个拦截器
        return invocation.proceed();
    }

    /**
     * 从数据库里查询总的记录数并计算总页数，回写进分页参数<code>PageParameter</code>,这样调用者就可用通过 分页参数 获得相关信息。
     */
    private void setPageParameter(String sql, Connection connection, MappedStatement mappedStatement,
            BoundSql boundSql, PageParam pageParam) {
        // 记录总记录数
        String countSql = dialect.buildCountSQL(sql);
        PreparedStatement countStmt = null;
        ResultSet rs = null;
        try {
            countStmt = connection.prepareStatement(countSql);
            
            setParameters(countStmt, mappedStatement, boundSql, boundSql.getParameterObject());
            rs = countStmt.executeQuery();
            int rowCount = 0;
            if (rs.next()) {
            	rowCount = rs.getInt(1);
            }
            pageParam.setRowCount(rowCount);
            int pageCount = (rowCount%pageParam.getPageSize()==0)?(rowCount/pageParam.getPageSize()):(rowCount/pageParam.getPageSize()+1);
            pageParam.setPageCount(pageCount);

        } catch (SQLException e) {
            logger.error("Ignore this exception", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("Ignore this exception", e);
            }
            try {
                countStmt.close();
            } catch (SQLException e) {
                logger.error("Ignore this exception", e);
            }
        }
    }

    /**
     * 对SQL参数(?)设值
     * 
     * @param ps
     * @param mappedStatement
     * @param boundSql
     * @param parameterObject
     * @throws SQLException
     */
    private void setParameters(PreparedStatement ps, MappedStatement mappedStatement, BoundSql boundSql,
            Object parameterObject) throws SQLException {
        ParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
        parameterHandler.setParameters(ps);
    }

    

    @Override
    public Object plugin(Object target) {
        // 当目标类是StatementHandler类型时，才包装目标类，否者直接返回目标本身,减少目标被代理的次数
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {
    	try {
    		String dialectClass=(String)properties.getProperty("dialectClass");
        	//创建方言对象
            dialect=(IDialect)Class.forName(dialectClass).newInstance();
		} catch (Exception e) {
			logger.error("Can't create dialect object!",e);
		}
    	       
    }

	//辅助方法，用来从拦截器链中分离出底层目标类
    public StatementHandler getStatementHandler(Invocation invocation){
    	 StatementHandler statementHandler = (StatementHandler) invocation.getTarget();        MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY,DEFAULT_OBJECT_WRAPPER_FACTORY);    	 
         // 分离代理对象链(由于目标类可能被多个拦截器拦截，从而形成多次代理，通过下面的两次循环可以分离出最原始的的目标类)
         while (metaStatementHandler.hasGetter("h")) {
             Object object = metaStatementHandler.getValue("h");
             metaStatementHandler = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);
         }
         // 分离最后一个代理对象的目标类
         while (metaStatementHandler.hasGetter("target")) {
             Object object = metaStatementHandler.getValue("target");
             metaStatementHandler = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);
         }
         return statementHandler;
    }
	
}