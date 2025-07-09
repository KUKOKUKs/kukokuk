package com.kukokuk.service;

import com.kukokuk.dto.MainStudyViewDto;
import com.kukokuk.mapper.StudyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudyService {
  @Autowired
  private StudyMapper studyMapper;

  /*
    메인 화면에 필요한 데이터를 반환한다
   */
  public  MainStudyViewDto getMainStudyView() {
    return null;
  }
}
