## 概述
&emsp;&emsp;虽然有着PageHelper插件对MyBatis的查询实现物理分页，但是我们也能够简单的自定义一个物理分页的功能使项目更加灵活。

>MyBatis给我们提供了一个拦截器插件Intercepts通过该插件我们能够对以下类创建代理对象。
- StatementHandler （数据库的处理对象，用于执行SQL语句）
- ParameterHandler （处理SQL的参数对象）
- ResultSetHandler （处理SQL的返回结果集）
- Executor （MyBatis的执行器，用于执行增删改查操作）

&emsp;&emsp;拦截 StatementHandler 中的 prepare() 方法获取数据库的连接对象，执行查询操作外的其他操作，如查询数据总数。再拦截 Executor 中的 query 方法，获取要进行查询的 SQL 语句，并使用 JAVA 的反射机制进行修改就能达到物理分页的效果了。

&emsp;&emsp;通过以上方法我们已经能够对查询进行分页了，但是为了能够动态的修改查询数据的页码与每页数量还需要一个能够存储在当前线程的变量 —— java.lang.ThreadLocal

> ThreadLocal 类是 JAVA JDK 中提供的一个类，用来解决线程安全问题。当使用ThreadLocal 维护变量时，ThreadLocal 为每个使用该变量的线程提供独立的变量副本，所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。

建立分页信息的实体类 PageInfo
```java
public class PageInfo<T> {

    private Integer pageNumber;
    private Integer pageSize;
    private Integer total;
    private List<Object> pageList;
    private String sql;

    // 省略 getter/setter
}
```

创建PageContext类管理ThreadLocal中的分页信息
```
public class PageContext {
    private static final ThreadLocal<PageInfo> context = new ThreadLocal<>();

    public static void set(PageInfo pageInfo) {
        context.set(pageInfo);
    }

    public static PageInfo get() {
        return context.get();
    }

    public static void remove() {
        context.remove();
    }
}
```

创建 QueryInterceptor 类实现接口 Interceptor 添加 @Intercepts 注解对查询方法进行拦截
```java
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
```
以上一个简单的针对MyBatis的物理分页就完成了如果有其他情况还可以自行扩展。

创建接口测试
```java
@RestController
public class PageController {

    @Resource
    private StoreMapper storeMapper;

    /**
     * @param num  页码
     * @param size 每页数量
     */
    @RequestMapping("/store")
    public String store(Integer num, Integer size) {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageNumber(num);
        pageInfo.setPageSize(size);
        PageContext.set(pageInfo);
        pageInfo.setPageList(storeMapper.findPageList());
        return JSON.toJSONString(pageInfo);
    }

}
```

![api.png](https://upload-images.jianshu.io/upload_images/18713780-9b431cab99307379.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)