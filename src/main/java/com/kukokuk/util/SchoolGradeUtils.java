package com.kukokuk.util;

import org.modelmapper.internal.Pair;

public class SchoolGradeUtils {

  /**
   * 다음 학년 정보 반환
   * @param school 현재 학교
   * @param grade 현재 학년
   * @return 현재학교/학년의 다음 학교/학년 Pair
   *          다음 학년이 없으면 null 반환
   */
  public static Pair<String, Integer> getNextSchoolGrade(String school, int grade){
    if ("초등".equals(school)) {
      if (grade < 6) return Pair.of("초등", grade + 1);
      else return Pair.of("중등", 1);
    } else if ("중등".equals(school)) {
      if (grade < 3) return Pair.of("중등", grade + 1);
      else return null; // 더 이상 학년 없음
    }
    return null;
  }
}