package com.mind.open.yolo;

/**
 * create by Rui on 2023-10-17
 * desc:菜品识别信息
 */
public class FoodRegInfo {
    public double score;  // 分数
    public String label;  // 标签


    public FoodRegInfo(double score, String label) {
        this.label = label;
        this.score = score;
    }
}
