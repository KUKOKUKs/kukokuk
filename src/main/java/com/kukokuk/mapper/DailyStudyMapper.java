package com.kukokuk.mapper;

import com.kukokuk.vo.DailyStudy;
import com.kukokuk.vo.DailyStudyLog;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyStudyMapper {

    public List<DailyStudyLog> getDailyStudyLogsByUserNo(int userNo);

    public List<DailyStudy> getDailyStudiesByUser();

}
