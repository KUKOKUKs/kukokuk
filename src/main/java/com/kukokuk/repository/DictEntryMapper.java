package com.kukokuk.repository;

import com.kukokuk.vo.DictEntry;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DictEntryMapper {

  // 단어 등록
  void insertDictEntry(DictEntry dictEntry);

  // 특정 단어 조회
  DictEntry selectByWord(String word);

  // 같은 품사의 단어 3개 랜덤 조회
  List<DictEntry> selectRandomByPartOfSpeech(String partOfSpeech);

  // 전체 등록된 단어 개수 조회 (entryNo 계산용)
  int getEntryCount();
}
