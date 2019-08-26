package cn.ruoshy.pageaide.aide;

import java.util.List;

public class PageInfo<T> {

    private Integer pageNumber;
    private Integer pageSize;
    private Integer total;
    private List<Object> pageList;
    private String sql;

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<Object> getPageList() {
        return pageList;
    }

    public void setPageList(List<Object> pageList) {
        this.pageList = pageList;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}