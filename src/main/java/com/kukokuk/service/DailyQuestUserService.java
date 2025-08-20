package com.kukokuk.service;

import com.kukokuk.dto.DailyQuestUserBatchDto;
import com.kukokuk.exception.AppException;
import com.kukokuk.mapper.DailyQuestUserMapper;
import com.kukokuk.vo.DailyQuestUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor // 초기화 되지않은 final 필드나, @NonNull 이 붙은 필드에 대해 생성자를 생성
public class DailyQuestUserService {

    private final ModelMapper modelMapper;
    private final DailyQuestUserMapper dailyQuestUserMapper;

    private final UserService userService;

    /**
     * 사용자의 일일 도전과제 완료로 도전과제의 일괄 보상 획득 처리
     * @param dailyQuestUserNos 보상 획득 처리할 식별자 값 리스트
     * @param userNo 사용자 번호
     * @return 업데이트된 행의 수
     */
    public int updateDailyQuestUserBatch(List<Integer> dailyQuestUserNos, int userNo) {
        log.info("updateDailyQuestUserBatch() 서비스 실행");

        // 일괄 처리를 위한 DTO 빌더
        DailyQuestUserBatchDto dailyQuestUserBatchDto = DailyQuestUserBatchDto.builder()
            .dailyQuestUserNos(dailyQuestUserNos)
            .userNo(userNo)
            .isObtained("Y")
            .build();

        // 일괄 업데이트 및 업데이트된 행의 수 요청
        int updatedCount = dailyQuestUserMapper.updateDailyQuestUserBatch(dailyQuestUserBatchDto);

        if (updatedCount == 0) {
            throw new AppException("수령할 수 있는 보상이 없습니다.");
        }
        
        // 사용자 힌트 개수 증가
        userService.updateUserHintCountPlus(updatedCount, userNo);

        return updatedCount;
    }


    /**
     * 사용자의 일일 도전과제 완료로 해당 일일 도전과제의 보상 획득 처리
     * @param dailyQuestUserNo 완료된 일일 도전과제 번호
     * @param userNo 사용자 번호
     * @return 업데이트 후 사용자 힌트 개수
     */
    @Transactional
    public int updateDailyQuestUserObtained(int dailyQuestUserNo, int userNo) {
        log.info("updateDailyQuestUserObtained() 서비스 실행");

        // 퀘스트 완료 내역 번호로 사용자의 오늘 퀘스트 완료 내역 가져오기
        DailyQuestUser savedDailyQuestUser = dailyQuestUserMapper
                                                .getDailyQuestUserByDailyQuestUserNo(
                                                    dailyQuestUserNo, userNo
                                                );

        if (savedDailyQuestUser == null) {
            // 잘 못된 요청에 의한 내역이 없을 경우
            throw new AppException("오늘은 이 도전과제를 완료하지 않았습니다.");
        } else if ("Y".equals(savedDailyQuestUser.getIsObtained())) {
            // 중복된 요청일 경우
            throw new AppException("해당 보상은 이미 수령하였습니다.");
        }

        // 해당 완료된 일일 도전과제 보상 수령 처리
        savedDailyQuestUser.setIsObtained("Y");
        dailyQuestUserMapper.updateDailyQuestUserObtained(savedDailyQuestUser);

        // 해당 완료된 일일 도전과제 보상 수령으로 사용자 힌트 개수 증가 처리
        userService.updateUserHintCountPlus(userNo);

        // 업데이트 후 힌트 개수 반환
        return userService.getCurrentUser().getHintCount();
    }

    /**
     * 사용자 번호와 퀘스트 번호로 오늘 퀘스트 완료 내역 조회
     * @param dailyQuestNo 퀘스트 번호
     * @param userNo 사용자 번호
     * @return 오늘 퀘스트 완료 내역 정보
     */
    public DailyQuestUser getDailyQuestUserByQuestNoAndUserNo(int dailyQuestNo, int userNo) {
        log.info("getDailyQuestUserByQuestNoAndUserNo() 서비스 실행");
        return dailyQuestUserMapper.getDailyQuestUserByQuestNoAndUserNo(dailyQuestNo, userNo);
    }

    /**
     * 사용자의 일일도전과제 완료된 목록 조회
     * @param userNo 사용자 번호
     * @return 사용자의 일일도전과제 완료된 목록
     */
    public List<DailyQuestUser> getDailyQuestUserByUserNo(int userNo) {
        log.info("getDailyQuestUserByUserNo() 서비스 실행");
        return dailyQuestUserMapper.getDailyQuestUserByUserNo(userNo);
    }

    /**
     * 퀘스트 완료 내역 등록
     * @param dailyQuestUser 퀘스트 완료 내역
     */
    public void insertDailyQuestUser(DailyQuestUser dailyQuestUser) {
        log.info("insertDailyQuestUser() 서비스 실행");
        dailyQuestUserMapper.insertDailyQuestUser(dailyQuestUser);
    }

}
