package com.kukokuk.domain.dictation.mapper;

import com.kukokuk.domain.dictation.dto.DictationResultLogDto;
import com.kukokuk.domain.dictation.vo.DictationQuestionLog;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DictationQuestionLogMapper {

  /**
   * 받아쓰기 이력 생성
   * @param dictationQuestionLog 받아쓰기 이력
   */
  void insertDictationQuestionLog(DictationQuestionLog dictationQuestionLog);

  /**
   * 정답 제출 버튼을 눌렀을 때 제출문장, 시도 누적 횟수, 정답 여부 반영
   * @param dictationQuestionLog 받아쓰기 문제 풀이 이력
   */
  void updateDictationQuestionLog(DictationQuestionLog dictationQuestionLog);

  /**
   * 받아쓰기 문제 세트 맞은 문제 개수 조회
   * @param dictationSessionNo 받아쓰기 문제 세트 번호
   * @return 맞은 문제 개수
   */
  int getCountCorrectAnswers(@Param("dictationSessionNo") int dictationSessionNo);

  /**
   * 받아쓰기 문제 세트 사용한 힌트 수 조회
   * @param dictationSessionNo 받아쓰기 문제 세트 번호
   * @return 사용한 힌트 개수
   */
  int getCountHintsUsed(@Param("dictationSessionNo") int dictationSessionNo);

  /**
   * 특정 세트 번호와 문제 번호에 해당하는 받아쓰기 문제 풀이 이력을 조회
   * @param dictationSessionNo 문제 세트 번호
   * @param dictationQuestionNo 문제 번호
   * @return 해당 받아쓰기 세트에 대한 문제 풀이 이력 (없으면 null)
   */
  DictationQuestionLog getLogBySessionAndQuestion(@Param("dictationSessionNo") int dictationSessionNo, @Param("dictationQuestionNo") int dictationQuestionNo);

  /**
   * 받아쓰기 세트 번호에 해당하는 받아쓰기 문제 풀이 이력 조회
   * @param dictationSessionNo 문제 세트 번호
   * @param userNo 사용자
   * @return 받아쓰기 문제 풀이 이력 내용
   */
  List<DictationResultLogDto> getDictationQuestionLogBySessionNo(
      @Param("dictationSessionNo") int dictationSessionNo,
      @Param("userNo") int userNo
  );

  /**
   * 랭킹 절대값 계산에 필요한 총 문항 수 조회
   * @param dictationSessionNo 문제 세트 번호
   * @return 총 문항 수
   */
  int getCountTotalQuestions(@Param("dictationSessionNo") int dictationSessionNo);

  /**
   * 랭킹 절대값 계산에 필요한 총 시도 횟수 조회
   * @param dictationSessionNo 문제 세트 번호
   * @return 총 시도 횟수
   */
  int getCountAllTries(@Param("dictationSessionNo") int dictationSessionNo);
}
