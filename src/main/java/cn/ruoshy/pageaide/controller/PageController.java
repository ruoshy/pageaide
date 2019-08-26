package cn.ruoshy.pageaide.controller;

import cn.ruoshy.pageaide.entity.Store;
import com.alibaba.fastjson.JSON;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.ruoshy.pageaide.mapper.StoreMapper;
import cn.ruoshy.pageaide.aide.PageInfo;
import cn.ruoshy.pageaide.aide.context.PageContext;

import javax.annotation.Resource;

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

    @RequestMapping("/store1")
    public String store1() {
        long start = System.currentTimeMillis();
        Store store = storeMapper.findByName("Apple Store 官方旗舰店");
        System.out.println(System.currentTimeMillis() - start);
        return JSON.toJSONString(store);
    }

}
