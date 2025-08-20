package com.kukokuk.domain.dictation.mapper;

import com.kukokuk.domain.dictation.vo.DictationSession;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DictationSessionMapper {

  /**
   * 받아쓰기 세트 생성
   * @param dictationSession 받아쓰기 세트
   */
  void insertDictationSession(DictationSession dictationSession);

  /**
   * 받아쓰기 끝나고 해당 받아쓰기 세트 결과 업데이트
   * @param session 받아쓰기 세트
   */
  void updateDictationSessionResult(DictationSession session);

  /**
   * 사용자 번호로 그 사용자가 풀었던 받아쓰기 세트 결과 내용을 조회
   * @param userNo 사용자 번호
   * @return 받아쓰기 세트 결과 내용
   */
  List<DictationSession> getDictationSessionResultsByUserNo(int userNo);


  /**
   * 결과페이지에 조회할 세트 결과 내용 조회
   * @param dictationSessionNo 받아쓰기 문제 세트 번호
   * @return 받아쓰기 문제 세트 결과
   */
  DictationSession getDictationSessionByDictationSessionNo(int dictationSessionNo);
}
