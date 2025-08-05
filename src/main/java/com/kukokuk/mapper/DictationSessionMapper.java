package com.kukokuk.mapper;

import com.kukokuk.response.DictationSessionResultResponse;
import com.kukokuk.vo.DictationSession;
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

  /**
   * 사용자 번호로 그 사용자가 풀었던 받아쓰기 세트 결과 내용을 조회
   * @param userNo 사용자 번호
   * @return 받아쓰기 세트 결과 내용
   */
  List<DictationSession> getDictationSessionResultsByUserNo(int userNo);
}
