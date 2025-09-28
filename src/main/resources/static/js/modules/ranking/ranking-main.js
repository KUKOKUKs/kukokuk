/**
 * 랭킹 메인 페이지 JavaScript
 * 랭킹 데이터 표시, 차트 애니메이션, 반응형 처리 등을 담당
 */

$(document).ready(function() {
    initRankingPage();
});

function initRankingPage() {
    // 히스토리 차트 애니메이션 초기화
    initHistoryChart();

    // 랭킹 테이블 인터렉션 초기화
    initRankingTable();

    // 반응형 처리 초기화
    initResponsive();

    console.log('랭킹 페이지 초기화 완료');
}

/**
 * 히스토리 차트 애니메이션 초기화
 */
function initHistoryChart() {
    const $historyBars = $('.history_bar');

    if ($historyBars.length === 0) return;

    // 페이지 로드 후 차트 애니메이션 실행
    setTimeout(function() {
        $historyBars.each(function(index) {
            const $bar = $(this);
            const targetHeight = parseFloat($bar.css('height')) || 0;

            // 애니메이션 시작 전 높이를 0으로 설정
            $bar.css('height', '0px');

            // 각 바마다 순차적으로 애니메이션 실행
            setTimeout(function() {
                $bar.animate({
                    height: targetHeight + '%'
                }, {
                    duration: 800,
                    easing: 'easeOutQuart'
                });
            }, index * 150);
        });
    }, 500);
}

/**
 * 랭킹 테이블 인터렉션 초기화
 */
function initRankingTable() {
    const $rankingRows = $('.ranking_row.data');

    // 랭킹 행 클릭 이벤트 (사용자 프로필 보기 등 확장 가능)
    $rankingRows.on('click', function() {
        const $row = $(this);
        const userNo = $row.data('user-no');

        // 현재는 클릭 효과만 구현 (추후 사용자 프로필 모달 등 추가 가능)
        $row.addClass('clicked');
        setTimeout(function() {
            $row.removeClass('clicked');
        }, 200);

        console.log('사용자 클릭:', userNo);
    });

    // 랭킹 행 호버 효과 개선
    $rankingRows.on('mouseenter', function() {
        $(this).find('.profile_image').addClass('hover_effect');
    }).on('mouseleave', function() {
        $(this).find('.profile_image').removeClass('hover_effect');
    });
}

/**
 * 반응형 처리 초기화
 */
function initResponsive() {
    handleResponsiveTable();

    // 윈도우 리사이즈 시 반응형 처리
    $(window).on('resize', debounce(handleResponsiveTable, 250));
}

/**
 * 반응형 테이블 처리
 */
function handleResponsiveTable() {
    const $table = $('.ranking_table');
    const $container = $('.ranking_table_container');

    if ($table.length === 0) return;

    const containerWidth = $container.width();
    const isMobile = containerWidth < 768;

    if (isMobile) {
        // 모바일에서는 그룹 컬럼 숨기기
        $table.addClass('mobile_view');

        // 스크롤 가능 표시
        if ($table[0].scrollWidth > containerWidth) {
            $container.addClass('scrollable');
        }
    } else {
        $table.removeClass('mobile_view');
        $container.removeClass('scrollable');
    }
}

/**
 * 점수 애니메이션 효과
 */
function animateScores() {
    const $scoreValues = $('.score_value');

    $scoreValues.each(function() {
        const $score = $(this);
        const targetValue = parseFloat($score.text());

        if (isNaN(targetValue)) return;

        // 0부터 목표값까지 애니메이션
        $({value: 0}).animate({value: targetValue}, {
            duration: 1000,
            easing: 'easeOutQuart',
            step: function() {
                $score.text(this.value.toFixed(1));
            },
            complete: function() {
                $score.text(targetValue.toFixed(1));
            }
        });
    });
}

/**
 * 메달 애니메이션 효과
 */
function animateMedals() {
    const $medals = $('.medal');

    $medals.each(function(index) {
        const $medal = $(this);

        setTimeout(function() {
            $medal.addClass('bounce_animation');
        }, index * 200);
    });
}

/**
 * 컨텐츠 타입 변경 시 로딩 효과
 */
function showContentTypeLoading() {
    const $rankingTable = $('.ranking_table');
    const $loadingSpinner = $('<div class="ranking_loading"><div class="spinner"></div><p>랭킹 데이터 로딩중...</p></div>');

    $rankingTable.fadeOut(300, function() {
        $rankingTable.parent().append($loadingSpinner);

        // 실제로는 페이지가 새로고침되므로 이 효과는 짧게만 보임
        setTimeout(function() {
            $loadingSpinner.remove();
        }, 1000);
    });
}

/**
 * 월 변경 시 슬라이드 효과
 */
function slideMonthContent(direction) {
    const $rankingSection = $('.ranking_section');
    const slideClass = direction === 'next' ? 'slide_left' : 'slide_right';

    $rankingSection.addClass(slideClass);

    setTimeout(function() {
        $rankingSection.removeClass(slideClass);
    }, 300);
}

/**
 * 내 랭킹 카드 펄스 효과
 */
function pulseMyRankingCard() {
    const $myRankingCard = $('.my_ranking_card');

    if ($myRankingCard.length > 0) {
        $myRankingCard.addClass('pulse_effect');

        setTimeout(function() {
            $myRankingCard.removeClass('pulse_effect');
        }, 2000);
    }
}

/**
 * 디바운스 함수
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = function() {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

/**
 * 스크롤 시 헤더 고정 효과
 */
function initStickyHeader() {
    const $header = $('.ranking_row.header');
    const $container = $('.ranking_table_container');

    if ($header.length === 0) return;

    $container.on('scroll', function() {
        const scrollTop = $(this).scrollTop();

        if (scrollTop > 0) {
            $header.addClass('sticky_shadow');
        } else {
            $header.removeClass('sticky_shadow');
        }
    });
}

/**
 * 초기 애니메이션 시퀀스 실행
 */
function runInitialAnimations() {
    // 페이지 로드 후 순차적으로 애니메이션 실행
    setTimeout(animateScores, 500);
    setTimeout(animateMedals, 800);
    setTimeout(pulseMyRankingCard, 1200);
}

// 컨텐츠 타입 버튼 클릭 시 로딩 효과
$(document).on('click', '.content_type_btn:not(.active)', function() {
    showContentTypeLoading();
});

// 월 네비게이션 버튼 클릭 시 슬라이드 효과
$(document).on('click', '.month_nav_btn', function() {
    const isNext = $(this).find('iconify-icon[icon*="right"]').length > 0;
    slideMonthContent(isNext ? 'next' : 'prev');
});

// 페이지 로드 완료 후 초기 애니메이션 실행
$(window).on('load', function() {
    runInitialAnimations();
    initStickyHeader();
});