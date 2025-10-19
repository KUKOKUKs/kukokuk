package com.kukokuk.domain.quest.service;

import com.kukokuk.domain.exp.dto.ExpAggregateDto;
import com.kukokuk.domain.exp.service.ExpService;
import com.kukokuk.domain.quest.dto.DailyQuestStatusDto;
import com.kukokuk.domain.quest.mapper.DailyQuestMapper;
import com.kukokuk.domain.quest.vo.DailyQuest;
import com.kukokuk.domain.quest.vo.DailyQuestUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor // 초기화 되지않은 final 필드나, @NonNull 이 붙은 필드에 대해 생성자를 생성
public class DailyQuestService {

    private final ModelMapper modelMapper;
    private final DailyQuestMapper dailyQuestMapper;

    private final ExpService expService;
    private final DailyQuestUserService dailyQuestUserService;

    /**
     * 모든 퀘스트 정보 목록 조회
     * @return 모든 퀘스트 정보 목록
     */
    public List<DailyQuest> getDailyQuests() {
        log.info("getDailyQuests() 서비스 실행");
        return dailyQuestMapper.getDailyQuests();
    }

    /**
     * 사용자의 모든 일일도전과제 현황 목록(일일도전과제목록, 진행도, 보상수령여부) 조회
     * @param userNo 사용자 번호
     * @return 모든 일일도전과제 현황 목록
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션: 성능 최적화, 실수로 쓰기 방지
    public List<DailyQuestStatusDto> getDailyQuestsStatus(int userNo) {
        log.info("getDailyQuestsStatus() 서비스 실행");

        // 1. 모든 일일 도전과제를 조회
        //  - DailyQuest는 KUKOKUK_DAILY_QUESTS 테이블과 1:1 매핑되는 도메인 객체
        //  - 이 리스트를 기준으로 사용자별 진행도/보상여부를 뒤에서 결합
        List<DailyQuest> dailyQuests = getDailyQuests();

        // 2. 오늘 경험치 획득 로그를 콘텐츠 타입(content_type)별로 집계
        //  - ExpAggregateDto contentType 기준으로 SUM(EXP_GAINED), COUNT(*)를 담는 DTO
        //  - 쿼리에서 날짜 필터(CURDATE() ~ CURDATE()+1)를 적용해 '오늘'만 집계하여 불필요한 후처리 방지
        // 2-1. 집계 결과를 (contentType → ExpAggregateDto) 맵으로 전환
        //  - 이후 반복 처리에서 contentType 키로 O(1)에 집계를 조회하기 위함(성능/가독성 ↑)
        Map<String, ExpAggregateDto> ExpAggMap = expService.getTodayExpAggregateByUserNo(userNo).stream()
            .collect(Collectors.toMap(ExpAggregateDto::getContentType, Function.identity()));

        // 3. 사용자의 일일 도전과제 완료된 목록(보상 수령 여부 포함) 조회
        //  - 일일 도전과제 완료 테이블에 외래키로 사용되는 일일 도전과제 번호를 key로 설정
        Map<Integer, DailyQuestUser> completeQuestMap = dailyQuestUserService.getDailyQuestUserByUserNo(userNo).stream()
            .collect(Collectors.toMap(DailyQuestUser::getDailyQuestNo, Function.identity()));
        log.info("completeQuestMap: {}", completeQuestMap);

        // 4. 최종 응답 리스트를 생성 (미리 용량을 dailyQuests 크기로 잡아 약간의 메모리/성능 이점)
        List<DailyQuestStatusDto> result = new ArrayList<>(dailyQuests.size());

        // 5. 모든 일일 도전과제를 하나씩 집계 일일도전과제/목표량/완료 번호/보상 수령 여부를 결합해 응답 DTO 생성
        for (DailyQuest quest : dailyQuests) {
            // 5-1. 현재 퀘스트의 content_type으로 오늘 집계 가져오기
            ExpAggregateDto agg = ExpAggMap.get(quest.getContentType());

            // 5-2. 진행도(progressValue) 계산 규칙:
            //  - EXP형(quest.getPoint() != null): 오늘 획득한 경험치 총합(expSum)을 사용
            //  - COUNT형(quest.getPoint() == null): 오늘 발생한 이력 개수(cnt)를 사용
            //  - 집계가 없거나 null이면 기본값 0
            int progressValue = quest.getPoint() != null
                ? agg != null && agg.getExpSum() != null ? agg.getExpSum() : 0
                : agg != null && agg.getCnt() != null ? agg.getCnt() : 0;

            // 5-3. 일일 도전과제 완료 이력 테이블 번호 및 보상 수령 여부 추출
            DailyQuestUser completeQuest = completeQuestMap.get(quest.getDailyQuestNo());
            log.info("completeQuest: {}", completeQuest);
            Integer dailyQuestUserNo = completeQuest != null ? completeQuest.getDailyQuestUserNo() : null;
            String isObtained = completeQuest != null ? completeQuest.getIsObtained() : null;

            // 5-4. 응답용 DTO를 구성
            //  - DailyQuest + 진행도(progressValue) + 보상여부(isObtained)
            DailyQuestStatusDto dailyQuestStatusDto = modelMapper.map(quest, DailyQuestStatusDto.class);
            dailyQuestStatusDto.setProgressValue(progressValue);
            dailyQuestStatusDto.setDailyQuestUserNo(dailyQuestUserNo);
            dailyQuestStatusDto.setIsObtained(isObtained);

            // 5-5. 최종 응답 리스트에 추가
            result.add(dailyQuestStatusDto);
        }

        // 6. 모든 퀘스트에 대해 사용자 진행도/보상여부가 결합된 DTO 리스트를 반환
        return result;
    }

}
