package com.kukokuk.mapper;

import com.kukokuk.vo.DictEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 국어사전 엔트리 관련 Mapper 인터페이스
 */
@Mapper
public interface DictEntryMapper {

  /**
   * 특정 ENTRY_NO에 해당하는 사전 데이터를 조회한다.
   *
   * @param entryNo 단어(표제어)의 고유 번호
   * @return 해당 단어의 사전 정보
   */
  DictEntry selectEntryByNo(@Param("entryNo") int entryNo);

  /**
   * 특정 ENTRY_NO를 제외하고 랜덤하게 사전 데이터를 조회한다.
   *
   * @param excludeEntryNo 제외할 정답 ENTRY_NO
   * @param limit 가져올 단어 수 (3개)
   * @return 무작위로 선택된 단어 리스트
   */
  List<DictEntry> selectRandomEntriesExclude(@Param("excludeEntryNo") int excludeEntryNo,
      @Param("limit") int limit);
}
