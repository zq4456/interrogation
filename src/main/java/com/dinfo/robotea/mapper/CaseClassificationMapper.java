package com.dinfo.robotea.mapper;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CaseClassificationMapper {

    /**
     * 添加案件类别
     * */
    int insertCaseClassification(List<Map<String, Object>> list);


}
