package cn.ruoshy.pageaide.mapper;

import cn.ruoshy.pageaide.entity.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StoreMapper {

    @Select("SELECT * FROM Store")
    List<Store> findPageList();

    @Select("SELECT * FROM Store WHERE Store_Name=#{name}")
    Store findByName(String name);
}
