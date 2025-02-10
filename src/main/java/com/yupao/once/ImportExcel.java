package com.yupao.once;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 导入excel数据
 */
@Slf4j
public class ImportExcel {

    public static void main(String[] args) {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        String fileName = "F:\\2.xlsx";
        Gson gson = new Gson();
        // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
        // 具体需要返回多少行可以在`PageReadListener`的构造函数设置
        List<XingQiuTableUserInfo> userInfoList = EasyExcel.read(fileName, XingQiuTableUserInfo.class, new PageReadListener<XingQiuTableUserInfo>(dataList -> {
            for (XingQiuTableUserInfo demoData : dataList) {
                log.info("读取到一条数据{}", gson.toJson(demoData));
            }
        })).sheet().doReadSync();
    }
}
