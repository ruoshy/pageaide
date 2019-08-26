package cn.ruoshy.pageaide.aide.context;

import cn.ruoshy.pageaide.aide.PageInfo;

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
