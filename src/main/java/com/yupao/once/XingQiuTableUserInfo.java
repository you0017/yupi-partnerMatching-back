package com.yupao.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class XingQiuTableUserInfo {
    @ExcelProperty("成员编号")
    private String planetCode;

    @ExcelProperty("成员昵称")
    private String username;
}
