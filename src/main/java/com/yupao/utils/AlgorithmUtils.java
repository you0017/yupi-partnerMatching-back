package com.yupao.utils;

import java.util.List;

public class AlgorithmUtils {
    // 计算两个字符串的编辑距离
    public static long CalculateDistance(List<String> source, List<String> target)
    {
        int n = source.size();
        int m = target.size();
        int[][] dp = new int[n + 1][m + 1];

        // 初始化第一行和第一列
        for (int i = 0; i <= n; i++)
            dp[i][0] = i;
        for (int j = 0; j <= m; j++)
            dp[0][j] = j;

        // 计算编辑距离
        for (int i = 1; i <= n; i++)
        {
            for (int j = 1; j <= m; j++)
            {
                // 如果当前字符相等，不需要进行操作，直接继承前一个子问题的解
                if (source.get(i - 1).equals(target.get(j - 1)))
                    dp[i][j] = dp[i - 1][j - 1];
             else
                {
                    // 否则，选择插入、删除、替换操作中的一种，并取最小值作为当前子问题的解
                    dp[i][j] = Math.min(dp[i - 1][j] + 1,         // 删除操作
                        Math.min(dp[i][j - 1] + 1,   // 插入操作
                        dp[i - 1][j - 1] + 1   // 替换操作
                                     ));
                }
            }
        }

        // 返回最终的编辑距离
        return dp[n][m];
    }
}
