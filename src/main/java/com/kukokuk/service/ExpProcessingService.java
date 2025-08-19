package com.kukokuk.service;

import com.kukokuk.dto.DailyQuestEnum;
import com.kukokuk.dto.ExpProcessingDto;
import com.kukokuk.vo.DailyQuestUser;
import com.kukokuk.vo.ExpLog;
import com.kukokuk.vo.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
    경험치, 퀘스트, 사용자 관련 복합 로직 수행에 필요한 서비스
 */
@Log4j2
@Service
@RequiredArgsConstructor // 초기화 되지않은 final 필드나, @NonNull 이 붙은 필드에 대해 생성자를 생성
public class ExpProcessingService {

    private final ModelMapper modelMapper;

    private final ExpService expService;
    private final DailyQuestUserService dailyQuestUserService;
    private final UserService userService;

    /**
     * 컨텐츠별 경험치 획득 처리
     * 경험치 획득 이력 추가,
     * 사용자 누적 경험치, 조건에 따라 레벨 업데이트
     * 일일 도전과제 관련 컨텐츠 일 경우 추가 로직 수행
     * @param expProcessingDto 컨텐츠별 경험치 획득 정보
     */
    @Transactional
    public void expProcessing(ExpProcessingDto expProcessingDto) {
        log.info("expProcessing() 서비스 실행");

        int userNo = expProcessingDto.getUserNo(); // 사용자 번호
        String contentType = expProcessingDto.getContentType(); // 컨텐츠 타입

        // 경헙치 획득 정보 등록
        expService.insertExpLog(modelMapper.map(expProcessingDto, ExpLog.class));

        // 사용자 누적 경험치 증가 및 레벨업(조건 확인)
        User securityUser = userService.getCurrentUser(); // 현재 로그인된 사용자 정보
        int updateExperiencePoints = securityUser.getExperiencePoints() + expProcessingDto.getExpGained();
        Integer updateLevel = updateExperiencePoints >= securityUser.getMaxExp() ? securityUser.getLevel() + 1 : null;

        User updateUser = User.builder()
            .userNo(userNo)
            .experiencePoints(updateExperiencePoints)
            .level(updateLevel) // 레벨업 조건 미달성 시 null
            .build();

        userService.updateUser(updateUser);

        // 일일 도전과제 컨텐츠 타입 체크
        DailyQuestEnum dailyQuestEnum = DailyQuestEnum.getByType(contentType);
        if (dailyQuestEnum == null) return; // 일일 도전과제가 아닌 경우 종료

        // 일일 도전과제 컨텐츠 일 경우
        int questNo = dailyQuestEnum.getDailyQuestNo(); // 퀘스트 번호
            
        // 일일 도전과제 완료 내역 조회
        DailyQuestUser savedDailyQuestUser = dailyQuestUserService
                                            .getDailyQuestUserByQuestNoAndUserNo(questNo, userNo);
        if (savedDailyQuestUser != null) return; // 일일 도전과제 완료 내역이 이미 있을 경우 종료

        // 일일 도전과제 완료 내역이 없을 경우
        int questTargetValue = dailyQuestEnum.getTargetValue(); // 퀘스트 완료 조건

        // 일일 도전과제 완료 조건 부합 여부
        boolean isQuestCompleted = switch (dailyQuestEnum.getProgressType()) {
            case EXP -> expService.getTodayTotalExpByTypeWithUserNo(contentType, userNo) >= questTargetValue;
            case COUNT ->  expService.getTodayCountExpByTypeWithUserNo(contentType, userNo) >= questTargetValue;
            default -> false;
        };

        // 일일 도전과제 완료 조건에 부합했을 경우 완료 내역 등록
        if (isQuestCompleted) {
            DailyQuestUser dailyQuestUser = DailyQuestUser.builder()
                .dailyQuestNo(questNo)
                .userNo(userNo)
                .build();
            dailyQuestUserService.insertDailyQuestUser(dailyQuestUser);
        }
    }
}
