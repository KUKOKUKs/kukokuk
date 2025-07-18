package com.kukokuk.mapper;

import com.kukokuk.vo.DictEntry;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    DictEntry getEntryByNo(int entryNo);
    /**
     * 특정 ENTRY_NO를 제외하고 랜덤하게 사전 데이터를 조회한다.
     * @param dictEntries 제외할 정답 ENTRY_NO
     * @return 무작위로 선택된 단어 리스트
     */
    List<DictEntry> getRandomEntriesExclude(@Param("dictEntries") List<DictEntry> dictEntries
                                        , @Param("limit") int limit);

    /**
     * 전체 사전에서 무작위로 단어들을 조회한다.
     *
     * @return 무작위로 선택된 단어 정보 목록
     */
    List<DictEntry> getRandomDictEntries(int count);
}
