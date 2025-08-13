package com.kukokuk.service;

import com.kukokuk.dto.DailyQuestProgressAggDto;
import com.kukokuk.exception.AppException;
import com.kukokuk.mapper.DailyQuestMapper;
import com.kukokuk.mapper.DailyQuestUserMapper;
import com.kukokuk.response.DailyQuestStatusResponse;
import com.kukokuk.vo.DailyQuest;
import com.kukokuk.vo.DailyQuestUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class DailyQuestService {

    private final DailyQuestUserMapper dailyQuestUserMapper; // 삭제 예정
    private final ModelMapper modelMapper;
    private final DailyQuestMapper dailyQuestMapper;

    public void insertExpLogWithQDailyQuest() {

    }

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
    public List<DailyQuestStatusResponse> getDailyQuestsStatus(int userNo) {
        log.info("getDailyQuestsStatus() 서비스 실행");

        // 1. 모든 일일 도전과제를 조회
        //  - DailyQuest는 KUKOKUK_DAILY_QUESTS 테이블과 1:1 매핑되는 도메인 객체
        //  - 이 리스트를 기준으로 사용자별 진행도/보상여부를 뒤에서 결합
        List<DailyQuest> dailyQuests = dailyQuestMapper.getDailyQuests();

        // 2. 오늘 경험치 획득 로그를 콘텐츠 타입(content_type)별로 집계
        //  - DailyQuestProgressAggDto contentType 기준으로 SUM(EXP_GAINED), COUNT(*)를 담는 DTO
        //  - 쿼리에서 날짜 필터(CURDATE() ~ CURDATE()+1)를 적용해 '오늘'만 집계하여 불필요한 후처리 방지
        // 2-1. 집계 결과를 (contentType → DailyQuestProgressAggDto) 맵으로 전환
        //  - 이후 반복 처리에서 contentType 키로 O(1)에 집계를 조회하기 위함(성능/가독성 ↑)
        Map<String, DailyQuestProgressAggDto> progressAggMap = dailyQuestMapper.getDailyQuestProgressAggByUserNo(userNo).stream()
            .collect(Collectors.toMap(DailyQuestProgressAggDto::getContentType, Function.identity()));

        // 3. 사용자의 일일 도전과제 완료된 목록(보상 수령 여부 포함) 조회
        //  - 일일 도전과제 완료 테이블에 외래키로 사용되는 일일 도전과제 번호를 key로 설정
        Map<Integer, DailyQuestUser> completeQuestMap = dailyQuestMapper.getDailyQuestUserByUserNo(userNo).stream()
            .collect(Collectors.toMap(DailyQuestUser::getDailyQuestNo, Function.identity()));

        // 4. 최종 응답 리스트를 생성 (미리 용량을 dailyQuests 크기로 잡아 약간의 메모리/성능 이점)
        List<DailyQuestStatusResponse> result = new ArrayList<>(dailyQuests.size());

        // 5. 모든 일일 도전과제를 하나씩 집계 일일도전과제/목표량/완료 번호/보상 수령 여부를 결합해 응답 DTO 생성
        for (DailyQuest quest : dailyQuests) {
            // 5-1. 현재 퀘스트의 content_type으로 오늘 집계 가져오기
            DailyQuestProgressAggDto agg = progressAggMap.get(quest.getContentType());

            // 5-2. 진행도(progressValue) 계산 규칙:
            //  - EXP형(quest.getPoint() != null): 오늘 획득한 경험치 총합(expSum)을 사용
            //  - COUNT형(quest.getPoint() == null): 오늘 발생한 이력 개수(cnt)를 사용
            //  - 집계가 없거나 null이면 기본값 0
            int progressValue = quest.getPoint() != null
                ? agg != null && agg.getExpSum() != null ? agg.getExpSum() : 0
                : agg != null && agg.getCnt() != null ? agg.getCnt() : 0;

            // 5-3. 일일 도전과제 완료 이력 테이블 번호 및 보상 수령 여부 추출
            DailyQuestUser completeQuest = completeQuestMap.get(quest.getDailyQuestNo());
            Integer dailyQuestUserNo = completeQuest != null ? completeQuest.getDailyQuestUserNo() : null;
            String isObtained = completeQuest != null ? completeQuest.getIsObtained() : null;

            // 5-4. 응답용 DTO를 구성
            //  - DailyQuest + 진행도(progressValue) + 보상여부(isObtained)
            DailyQuestStatusResponse dailyQuestStatusResponse = modelMapper.map(quest, DailyQuestStatusResponse.class);
            dailyQuestStatusResponse.setProgressValue(progressValue);
            dailyQuestStatusResponse.setDailyQuestUserNo(dailyQuestUserNo);
            dailyQuestStatusResponse.setIsObtained(isObtained);

            // 5-5. 최종 응답 리스트에 추가
            result.add(dailyQuestStatusResponse);
        }

        // 6. 모든 퀘스트에 대해 사용자 진행도/보상여부가 결합된 DTO 리스트를 반환
        return result;
    }

    /** 삭제 예정
     * 해당 일일도전과제 수행 정보의 IS_OBTAINED 컬럼을 "Y"로 변경
     * - 로그인한 사용자와 해당 도일일도전과제 수행 정본의 소유자가 일치하는지 확인
     * - 이미 "Y"인 경우 예외 처리
     * @param dailyQuestUserNo 일일도전과제 수행 정보 식별자
     * @param userNo 현재 로그인한 사용자 식별자
     */
    public void updateDailyQuestUser(int dailyQuestUserNo, int userNo) {

        // 일일도전과제 수행 정보 식별자로 정보 조회
        DailyQuestUser dailyQuestUser = dailyQuestUserMapper.getDailyQuestUserByNo(
            dailyQuestUserNo);

        // 로그인한 사용자와 해당 도일일도전과제 수행 정본의 소유자가 일치하는지 확인
        if (dailyQuestUser.getUserNo() != userNo) {
            // 인가 실패이므로 시큐리티가 처리할 수 있도록 AccessDeniedException 발생
            throw new AccessDeniedException("해당 도전과제에 대한 권한이 없습니다.");
        }

        // 이미 IS_OBTAINED가 "Y"인 경우 처리
        if ("Y".equals(dailyQuestUser.getIsObtained())) {
            // RestControllerAdvice에서 AppException을 처리해서 에러 JSON응답 전달
            throw new AppException("이미 획득한 아이템입니다");
        }

        // 해당 일일도전과제 수행 정보의 IS_OBTAINED 컬럼을 "Y"로 변경
        dailyQuestUserMapper.updateIsObtained(dailyQuestUserNo);
    }
}
