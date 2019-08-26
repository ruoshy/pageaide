package cn.ruoshy.pageaide.aide.intercepts;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;
import cn.ruoshy.pageaide.aide.PageInfo;
import cn.ruoshy.pageaide.aide.context.PageContext;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

@Intercepts(
        {
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
        }
)
@Component
public class QueryInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获得分页信息
        PageInfo pageInfo = PageContext.get();
        if (pageInfo == null) {
            return invocation.proceed();
        }
        Object[] args = invocation.getArgs();
        if (args.length == 2) {
            String querySql = pageInfo.getSql();
            pageInfo.setSql(null);
            int position = querySql.toUpperCase().indexOf("FROM");
            // 查询数据总数
            Connection con = (Connection) args[0];
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) " + querySql.substring(position));
            ResultSet rs = ps.executeQuery();
            rs.next();
            pageInfo.setTotal(rs.getInt(1));
        } else {
            // 分页
            MappedStatement ms = (MappedStatement) args[0];
            Object parameter = args[1];
            RowBounds rowBounds = (RowBounds) args[2];
            ResultHandler resultHandler = (ResultHandler) args[3];
            Executor executor = (Executor) invocation.getTarget();
            BoundSql boundSql = ms.getBoundSql(parameter);
            CacheKey cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
            // 缓存SQL语句
            pageInfo.setSql(boundSql.getSql());
            // 反射获取BoundSql类变量sql的对象并添加limit语句
            Field field = boundSql.getClass().getDeclaredField("sql");
            field.setAccessible(true);
            // 获取页码
            Integer num = pageInfo.getPageNumber();
            // 获取每页数量
            Integer size = pageInfo.getPageSize();
            if (num != null && size != null) {
                field.set(boundSql, boundSql.getSql() + " limit " + num * size + "," + size);
            }
            return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

}
