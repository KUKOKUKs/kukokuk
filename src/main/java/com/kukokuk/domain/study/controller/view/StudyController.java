package com.kukokuk.domain.study.controller.view;

import com.kukokuk.common.constant.ContentTypeEnum;
import com.kukokuk.common.constant.DailyQuestEnum;
import com.kukokuk.common.constant.PaginationEnum;
import com.kukokuk.common.exception.AppException;
import com.kukokuk.domain.exp.dto.ExpProcessingDto;
import com.kukokuk.domain.exp.service.ExpProcessingService;
import com.kukokuk.domain.quiz.dto.QuizWithLogDto;
import com.kukokuk.domain.study.dto.StudyEssayViewDto;
import com.kukokuk.domain.study.dto.StudyProgressViewDto;
import com.kukokuk.domain.study.service.StudyService;
import com.kukokuk.domain.study.vo.DailyStudy;
import com.kukokuk.domain.study.vo.DailyStudyLog;
import com.kukokuk.domain.study.vo.DailyStudyQuiz;
import com.kukokuk.domain.study.vo.DailyStudyQuizLog;
import com.kukokuk.security.SecurityUser;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

@Log4j2
@Controller
@RequiredArgsConstructor
@SessionAttributes({"studyProgressDto"})
@RequestMapping("/study")
public class StudyController {

    private final ModelMapper modelMapper;
    private final StudyService studyService;
    private final ExpProcessingService expProcessingService;

    // 세션에 바인딩할 폼 객체 초기화
    @ModelAttribute("studyProgressDto")
    final public StudyProgressViewDto initStudyProgress() {
        log.info("studyProgressDto 객체 초기화");
        return new StudyProgressViewDto();
    }

    // 학습 페이지
    @GetMapping
    public String studyMain(Model model,
        @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("StudyController studyMain() 실행");
        
        // 학습 이력 정보
        model.addAttribute(
            "dailyStudyLogs"
            , studyService.getDailyStudyLogs(securityUser.getUser().getUserNo(), PaginationEnum.COMPONENT_ROWS)
        );
        return "study/main";
    }

    /**
     * 학습 진행 시작 페이지(학습 이력 상세 페이지 겸용)
     * @param dailyStudyNo 학습 번호
     * @param studyProgressDto 학습에 대한 모든 데이터가 담겨 있는 dto
     * @param securityUser 사용자 정보
     * @return 학습 진행 템플릿
     */
    @GetMapping("/{dailyStudyNo}")
    public String studyProgress(
        @PathVariable("dailyStudyNo") int dailyStudyNo
        , @RequestParam(value = "progressIndex", defaultValue = "0") int progressIndex
        , @ModelAttribute("studyProgressDto") StudyProgressViewDto studyProgressDto
        , @AuthenticationPrincipal SecurityUser securityUser
        , Model model) {
        log.info("StudyController studyProgress() 실행 dailyStudyNo: {}", dailyStudyNo);
        
        int userNo = securityUser.getUser().getUserNo();

        // 학습 정보 조회
        studyProgressDto.setDailyStudy(studyService.getDailyStudy(dailyStudyNo));
        // 학습 카드 목록 조회
        studyProgressDto.setCards(studyService.getDailyStudyCards(dailyStudyNo));
        // 학습 이력 조회
        studyProgressDto.setLog(studyService.getDailyStudyLog(userNo, dailyStudyNo));
        // 퀴즈 목록 조회
        studyProgressDto.setQuizzes(studyService.getDailyStudyQuiz(dailyStudyNo));
        // 퀴즈 이력 목록 조회
        studyProgressDto.setQuizLogs(studyService.getDailyStudyQuizLog(userNo, dailyStudyNo));

        // 사용자의 퀴즈 이력을 quizNo기준의 Map으로 변환
        Map<Integer, DailyStudyQuizLog> quizLogMap = studyProgressDto.getQuizLogs().stream()
            .collect(Collectors.toMap(DailyStudyQuizLog::getDailyStudyQuizNo, Function.identity()));

        // 퀴즈와 사용자의 이력을 합친 DTO 생성
        List<QuizWithLogDto> quizWithLogDtos = studyProgressDto.getQuizzes().stream()
            .map(quiz -> {
                QuizWithLogDto quizWithLogDto = modelMapper.map(quiz, QuizWithLogDto.class);
                quizWithLogDto.setDailyStudyQuizLog(quizLogMap.get(quiz.getDailyStudyQuizNo()));
                return quizWithLogDto;
            })
            .toList();

        // 퀴즈와 사용자의 이력을 합친 DTO 생성
        studyProgressDto.setQuizWithLogDtos(quizWithLogDtos);
        model.addAttribute("progressIndex", progressIndex);
        return "study/progress";
    }

    /**
     * 학습 진행 화면에서의 요청(다음, 이전 페이지 학습 정보 담기)
     * <p>
     *     조건에 따른 학습 이력 등록/수정
     * <p>
     *     조건에 따른 경험치 등록
     * <p>
     *     조건에 따른 학습 퀴즈 이력 등록/수정
     * @param direction 이전/다음 요청(prev, next)
     * @param dailyStudyQuizNo 학습 퀴즈 번호
     * @param selectedChoice 학습 퀴즈 사용자가 선택한 번호
     * @param studyProgressDto 학습 진행에 필요한 데이터를 담은 객체
     * @param securityUser 사용자 번호
     * @return 학습 진행 화면 템플릿
     */
    @PostMapping("/progress")
    public String studyProgressing(
        @RequestParam("direction") String direction
        , @RequestParam(required = false) Integer dailyStudyQuizNo
        , @RequestParam(required = false) Integer selectedChoice
        , @RequestParam(value = "progressIndex", defaultValue = "0") int progressIndex // 새로 고침 시 처음부터
        , @ModelAttribute("studyProgressDto") StudyProgressViewDto studyProgressDto
        , @AuthenticationPrincipal SecurityUser securityUser
        , Model model) {
        int totalProgressNum = studyProgressDto.getDailyStudy().getCardCount() + studyProgressDto.getQuizzes().size();

        log.info("StudyController studyProgressing() 실행 progressIndex: {}, totalProgressNum: {}"
            , progressIndex, totalProgressNum);

        DailyStudy dailyStudy = studyProgressDto.getDailyStudy(); // 학습 자료 정보
        DailyStudyLog dailyStudyLog = studyProgressDto.getLog(); // 학습 이력 정보
        List<DailyStudyQuizLog> dailyStudyQuizLogs = studyProgressDto.getQuizLogs(); // 학습 퀴즈 이력 정보 목록
        List<QuizWithLogDto> quizWithLogDtos = studyProgressDto.getQuizWithLogDtos(); // 학습 퀴즈와 이력 합본 정보 목록
        int userNo = securityUser.getUser().getUserNo(); // 사용자 번호
        int studyCardCount = dailyStudy.getCardCount(); // 학습 카드 수

        // 현재 학습 진행 상태 완료 여부
        // 다음 버튼으로 요청이 된 이후 progressIndex + 1(인덱스 값으로 0부터 시작)의 값이 totalProgressNum 보다 크거나 같을 경우 완료로 판단됨
        boolean isAllCompleted = progressIndex + 1 >= totalProgressNum;
        log.info("마지막 학습 완료 여부 isAllCompleted: {}", isAllCompleted);
        
        // 완료되었을 경우 오늘의 학습 페이지로
        if (isAllCompleted) return "redirect:/study";
        
        // 현재 진행 상태가 card인지 quiz인지 파악
        // progressIndex는 index값으로 0부터 시작하여 현재 요청에서 학습 카드 수 보다 같거나 클 경우
        // 이미 card에 대한 이력 로직은 실행이 된 이후로
        // 학습 퀴즈에 대한 로직이 실행어야 함
        boolean isQuizProgress = progressIndex >= studyCardCount;
        log.info("현재 진행 상태가 card인지 quiz인지 파악 isQuizProgress: {}", isQuizProgress);
        
        // 이전 요청인 경우 아무 작업 없이 인덱스 값만 감소
        if ("prev".equals(direction)) {
            model.addAttribute("progressIndex", progressIndex < 1 ? 0 : progressIndex - 1);
            return "study/progress";
        }

        // 학습 퀴즈 진행 상태가 아닌 경우
        if (!isQuizProgress) {
            progressIndex++;

            if (dailyStudyLog == null) {
                // 등록된 이력이 없으면 등록 후 적용
                studyProgressDto.setLog(studyService.createDailyStudyLog(dailyStudy.getDailyStudyNo(), userNo));
                model.addAttribute("progressIndex", progressIndex);
                return "study/progress";
            }

            // 기존 학습 이력이 학습완료 상태인 이력인지 여부(복습 여부)
            boolean alreadyCompleted = "COMPLETED".equals(dailyStudyLog.getStatus());

            // 기존 학습 이력이 학습완료 상태가 아닌 경우
            if (!alreadyCompleted) {
                int studiedCardCount = dailyStudyLog.getStudiedCardCount();
                dailyStudyLog.setStudiedCardCount(Math.max(progressIndex, studiedCardCount)); // 학습 진도 세팅

                // 학습 이력 업데이트 요청
                studyService.updateDailyStudyLog(dailyStudyLog);
            }

            model.addAttribute("progressIndex", progressIndex);
            return "study/progress";
        }
        
        // 학습 퀴즈 진행 상태라면 cards는 이미 완료된 상태로 판단
        // 기존 학습 이력이 완료 상태가 아닐 경우 처음으로 이 학습 자료를 완료한 상태
        boolean isCardFirstComplete = !"COMPLETED".equals(dailyStudyLog.getStatus());

        if (isCardFirstComplete) {
            // 학습 이력 완료 상태로 변경
            dailyStudyLog.setStatus("COMPLETED");
            // 학습 이력 업데이트 요청
            studyService.updateDailyStudyLog(dailyStudyLog);
            
            // 경험치 추가
            ExpProcessingDto expProcessingDto = ExpProcessingDto.builder()
                .userNo(userNo)
                .contentType(ContentTypeEnum.STUDY.name())
                .contentNo(dailyStudyLog.getDailyStudyLogNo())
                .expGained(20)
                .dailyQuestNo(DailyQuestEnum.STUDY_LEARNING.getDailyQuestNo())
                .build();
            expProcessingService.expProcessing(expProcessingDto);
        }

        // 학습 퀴즈 진행 상태인 경우savedQuizLog
        if (selectedChoice != null) {
            // 사지선다 퀴즈 보기 선택을 했을 경우(미선택시 아무작업하지 않음)
            // 해당하는 퀴즈 번호로 퀴즈 가져오기
            DailyStudyQuiz dailyStudyQuiz = studyProgressDto.getQuizzes().stream()
                .filter(quiz -> quiz.getDailyStudyQuizNo() == dailyStudyQuizNo)
                .findFirst()
                .orElseThrow(() -> new AppException("해당 퀴즈 번호로 퀴즈를 찾을 수 없습니다.: " + dailyStudyQuizNo));
            int successAnswer = dailyStudyQuiz.getSuccessAnswer(); // 해당 학습 퀴즈의 정답

            // 해당하는 퀴즈 번호로 기존 학습 퀴즈 이력 찾기
            DailyStudyQuizLog savedQuizLog = dailyStudyQuizLogs.stream()
                .filter(quizLog -> quizLog.getDailyStudyQuizNo() == dailyStudyQuizNo) 
                .findFirst()
                .orElse(null); // 없으면 null

            if (savedQuizLog == null) {
                // 학습 퀴즈 이력이 없으면 생성
                DailyStudyQuizLog newDailyStudyQuizLog = DailyStudyQuizLog.builder()
                    .dailyStudyQuizNo(dailyStudyQuizNo)
                    .userNo(userNo)
                    .selectedChoice(selectedChoice)
                    .isSuccess(successAnswer == selectedChoice ? "Y" : "N")
                    .build();
                
                // 등록 후 dailyStudyQuizLogs에 적용
                DailyStudyQuizLog createdStudyQuizLog = studyService.createStudyQuizLog(newDailyStudyQuizLog);
                studyProgressDto.getQuizLogs().add(createdStudyQuizLog); // 퀴즈 이력 적용
                studyProgressDto.getQuizWithLogDtos().stream() // quizWithLogDto 갱신
                    .filter(quizWithLogDto -> quizWithLogDto.getDailyStudyQuizNo() == dailyStudyQuizNo)
                    .findFirst()
                    .ifPresent(quizWithLogDto -> {
                        // quizWithLogDto 내부에 DailyStudyQuizLog를 갱신
                        quizWithLogDto.setDailyStudyQuizLog(createdStudyQuizLog);
                    });
            } else {
                // 학습 퀴즈 이력이 있으면 세션어트리뷰트의 값을 변경하고 업데이트 요청
                savedQuizLog.setSelectedChoice(selectedChoice);
                savedQuizLog.setIsSuccess(successAnswer == selectedChoice ? "Y" : "N");
                studyService.updateStudyQuizLog(savedQuizLog);

                studyProgressDto.getQuizWithLogDtos().stream() // quizWithLogDto 갱신
                    .filter(quizWithLogDto -> quizWithLogDto.getDailyStudyQuizNo() == dailyStudyQuizNo)
                    .findFirst()
                    .ifPresent(quizWithLogDto -> {
                        // quizWithLogDto 내부에 DailyStudyQuizLog를 갱신
                        quizWithLogDto.setDailyStudyQuizLog(savedQuizLog);
                    });
            }
        }

        progressIndex++;
        model.addAttribute("progressIndex", progressIndex);
        return "study/progress";
    }

//    @GetMapping("/{dailyStudyNo}/complete")
//    public String studyComplete(
//        @PathVariable("dailyStudyNo") int dailyStudyNo,
//        @AuthenticationPrincipal SecurityUser securityUser,
//        Model model) {
//
//        StudyCompleteViewDto dto = studyService.getStudyCompleteView(dailyStudyNo, securityUser.getUser().getUserNo());
//
//        model.addAttribute("data", dto);
//        model.addAttribute("dailyStudyNo", dailyStudyNo);
//
//        return "study/complete";
//    }

    @GetMapping("/{dailyStudyNo}/essay")
    public String studyEssay(@PathVariable("dailyStudyNo") int dailyStudyNo,
        @AuthenticationPrincipal SecurityUser securityUser,
        Model model) {
        log.info("studyEssay 컨트롤러 실행");

        StudyEssayViewDto dto = studyService.getStudyEssayView(dailyStudyNo, securityUser.getUser().getUserNo());
        model.addAttribute("studyEssayViewDto", dto);

        return "study/essay";
    }

    // 학습 이력 전용 페이지
    @GetMapping("/history")
    public String studyHistory(
        @RequestParam(defaultValue = "1") int page
        , @RequestParam(required = false) Integer rows
        , @AuthenticationPrincipal SecurityUser securityUser
        , Model model) {
        log.info("StudyController studyHistory() 컨트롤러 실행");

        // 조회할 행의 수를 입력하지 않았을 경우 기본 값 10
        if (rows == null) {
            rows = PaginationEnum.DEFAULT_ROWS;
        }

        model.addAttribute("studyLogs", studyService.getStudyLogsDetail(securityUser.getUser().getUserNo(), page, rows));

        return "study/history";
    }

}
