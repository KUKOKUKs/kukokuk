package com.kukokuk.mapper;

import com.kukokuk.vo.DictationSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DictationSessionMapper {

  /**
   * 받아쓰기 세트 생성
   * @param session 받아쓰기 세트
   */
  void insertDictationSession(DictationSession session);

  /**
   * 세트 번호로 받아쓰기 세트 가져오기
   * @param dictationSessionNo 세트 번호
   * @return 받아쓰기 세트
   */
  DictationSession getDictationSessionByNo(int dictationSessionNo);

  /**
   * 받아쓰기 끝나고 해당 받아쓰기 세트 결과 업데이트
   * @param session 받아쓰기 세트
   */
  void updateDictationSessionResult(DictationSession session);

}
