package com.dinfo.robotea.mapper;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 案件类别 DAO
 * */
@Repository
public interface CaseClassificationMapper {

    /**
     * 添加案件类别
     * */
    int insertCaseClassification(List<Map<String, Object>> list);

    /**
     * 案件类别分页查询
     * s
     * from 起始页码
     * to 记录数
     * name  案件类别名称
     * */
    List<Map<String,Object>> getCaseClassificationPage(Map<String,Object> param);

    /**
     * 案件类别 条件查询返回总数
     *
     * name  案件类别名称
     * */
     int getCaseClassificationCounts(Map<String,Object> param);

     /**
      * 案件类别修改
      * */
     int updateCaseClassification(Map<String,Object> param);

     /**
      * 案件类别删除
      * */
     int batchDeleteCaseClassification(Map<String,Object> param);


}
