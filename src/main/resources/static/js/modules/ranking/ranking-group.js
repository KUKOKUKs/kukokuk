/**
 * 그룹 랭킹 페이지 JavaScript
 * 그룹 특화 기능, 애니메이션, 상호작용 등을 담당
 */

$(document).ready(function() {
    initGroupRankingPage();
});

function initGroupRankingPage() {
    // 기본 랭킹 페이지 기능 초기화
    initRankingTable();
    initResponsive();

    // 그룹 특화 기능 초기화
    initGroupFeatures();
    initGroupAnimations();
    initGroupStats();

    console.log('그룹 랭킹 페이지 초기화 완료');
}

/**
 * 그룹 특화 기능 초기화
 */
function initGroupFeatures() {
    // 내 그룹 순위 강조 효과
    highlightMyGroupRank();

    // 그룹 응원 메시지 인터렙션
    initCheerActions();

    // 그룹 챌린지 진행률 애니메이션
    animateGroupChallenge();
}

/**
 * 내 그룹 순위 강조 효과
 */
function highlightMyGroupRank() {
    const $myRankingCard = $('.my_group_ranking_card');
    const $rankCircle = $('.rank_circle');

    if ($rankCircle.length === 0) return;

    // 순위에 따른 특별 효과
    const rankNumber = parseInt($rankCircle.find('.rank_number').text());

    if (rankNumber === 1) {
        $rankCircle.addClass('rank_first');
        $myRankingCard.addClass('winner_glow');

        // 1위 축하 효과
        setTimeout(function() {
            showCelebrationEffect();
        }, 1000);
    } else if (rankNumber <= 3) {
        $rankCircle.addClass('rank_top');
    }

    // 순위 카운트업 애니메이션
    animateRankNumber();
}

/**
 * 순위 숫자 카운트업 애니메이션
 */
function animateRankNumber() {
    const $rankNumber = $('.rank_number');

    if ($rankNumber.length === 0) return;

    const finalRank = parseInt($rankNumber.text());
    const startRank = Math.max(1, finalRank + 5);

    $rankNumber.text(startRank);

    $({rank: startRank}).animate({rank: finalRank}, {
        duration: 1500,
        easing: 'easeOutBounce',
        step: function() {
            $rankNumber.text(Math.ceil(this.rank));
        },
        complete: function() {
            $rankNumber.text(finalRank);
        }
    });
}

/**
 * 1위 축하 효과
 */
function showCelebrationEffect() {
    const $body = $('body');
    const colors = ['#FFD700', '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4'];

    // 축하 색종이 효과
    for (let i = 0; i < 50; i++) {
        const $confetti = $('<div class="confetti"></div>');
        const color = colors[Math.floor(Math.random() * colors.length)];
        const leftPosition = Math.random() * 100;
        const animationDuration = 3 + Math.random() * 2;
        const size = 5 + Math.random() * 10;

        $confetti.css({
            'position': 'fixed',
            'top': '-10px',
            'left': leftPosition + '%',
            'width': size + 'px',
            'height': size + 'px',
            'background-color': color,
            'z-index': 9999,
            'animation': `confettiFall ${animationDuration}s linear forwards`
        });

        $body.append($confetti);

        // 애니메이션 완료 후 제거
        setTimeout(function() {
            $confetti.remove();
        }, animationDuration * 1000);
    }
}

/**
 * 그룹 응원 액션 초기화
 */
function initCheerActions() {
    const $cheerBtns = $('.cheer_btn');

    $cheerBtns.on('click', function(e) {
        const $btn = $(this);

        // 클릭 파동 효과
        createRippleEffect($btn, e);

        // 약간의 지연 후 페이지 이동 (애니메이션을 위해)
        setTimeout(function() {
            if (!e.isDefaultPrevented()) {
                window.location.href = $btn.attr('href');
            }
        }, 200);

        e.preventDefault();
    });
}

/**
 * 클릭 파동 효과 생성
 */
function createRippleEffect($element, event) {
    const $ripple = $('<div class="ripple_effect"></div>');
    const elementRect = $element[0].getBoundingClientRect();
    const size = Math.max(elementRect.width, elementRect.height);
    const x = event.clientX - elementRect.left - size / 2;
    const y = event.clientY - elementRect.top - size / 2;

    $ripple.css({
        'width': size + 'px',
        'height': size + 'px',
        'left': x + 'px',
        'top': y + 'px'
    });

    $element.css('position', 'relative').append($ripple);

    setTimeout(function() {
        $ripple.remove();
    }, 600);
}

/**
 * 그룹 애니메이션 초기화
 */
function initGroupAnimations() {
    // 스크롤 기반 애니메이션
    initScrollAnimations();

    // 호버 효과 개선
    enhanceHoverEffects();
}

/**
 * 스크롤 기반 애니메이션
 */
function initScrollAnimations() {
    const $animElements = $('.feature_card, .cheer_card');

    function checkScroll() {
        $animElements.each(function() {
            const $element = $(this);
            const elementTop = $element.offset().top;
            const windowBottom = $(window).scrollTop() + $(window).height();

            if (elementTop < windowBottom - 100 && !$element.hasClass('animated')) {
                $element.addClass('animated slide_in_up');
            }
        });
    }

    $(window).on('scroll', debounce(checkScroll, 100));
    checkScroll(); // 초기 체크
}

/**
 * 호버 효과 개선
 */
function enhanceHoverEffects() {
    // 그룹 테이블 행 호버 효과
    $('.group_row').on('mouseenter', function() {
        $(this).find('.profile_image').addClass('group_hover');
        $(this).find('.status_badge').addClass('badge_hover');
    }).on('mouseleave', function() {
        $(this).find('.profile_image').removeClass('group_hover');
        $(this).find('.status_badge').removeClass('badge_hover');
    });

    // 기능 카드 호버 효과
    $('.feature_card').on('mouseenter', function() {
        $(this).addClass('card_lift');
    }).on('mouseleave', function() {
        $(this).removeClass('card_lift');
    });
}

/**
 * 그룹 통계 초기화
 */
function initGroupStats() {
    // 통계 숫자 카운트업 애니메이션
    animateStatNumbers();

    // 진행률 바 애니메이션
    animateProgressBars();
}

/**
 * 통계 숫자 애니메이션
 */
function animateStatNumbers() {
    const $statValues = $('.stat_value');

    $statValues.each(function() {
        const $stat = $(this);
        const text = $stat.text();
        const number = parseFloat(text);

        if (isNaN(number)) return;

        $stat.text('0' + text.replace(/[\d.]/g, ''));

        $({value: 0}).animate({value: number}, {
            duration: 2000,
            easing: 'easeOutQuart',
            step: function() {
                const current = Math.ceil(this.value * 10) / 10;
                $stat.text(current + text.replace(/[\d.]/g, ''));
            },
            complete: function() {
                $stat.text(text);
            }
        });
    });
}

/**
 * 진행률 바 애니메이션
 */
function animateProgressBars() {
    const $progressBars = $('.gauge');

    $progressBars.each(function() {
        const $bar = $(this);
        const targetWidth = $bar.css('width');

        $bar.css('width', '0%');

        setTimeout(function() {
            $bar.animate({
                width: targetWidth
            }, {
                duration: 1500,
                easing: 'easeOutQuart'
            });
        }, 800);
    });
}

/**
 * 그룹 챌린지 애니메이션
 */
function animateGroupChallenge() {
    const $challengeProgress = $('.challenge_progress');

    if ($challengeProgress.length === 0) return;

    // 챌린지 달성 여부에 따른 효과
    const progressScore = parseFloat($('.progress_score').text());

    if (progressScore >= 80) {
        $challengeProgress.addClass('challenge_excellent');

        // 우수 그룹 표시 효과
        setTimeout(function() {
            const $badge = $('<div class="excellence_badge">우수 그룹! 🏆</div>');
            $challengeProgress.append($badge);

            setTimeout(function() {
                $badge.addClass('badge_show');
            }, 100);
        }, 2000);
    } else if (progressScore >= 60) {
        $challengeProgress.addClass('challenge_good');
    }
}

/**
 * 디바운스 함수 (중복 방지)
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
 * 기본 랭킹 테이블 기능 (main에서 가져옴)
 */
function initRankingTable() {
    const $rankingRows = $('.ranking_row.data');

    $rankingRows.on('click', function() {
        const $row = $(this);
        const userNo = $row.data('user-no');

        $row.addClass('clicked');
        setTimeout(function() {
            $row.removeClass('clicked');
        }, 200);

        console.log('그룹원 클릭:', userNo);
    });
}

/**
 * 반응형 처리 (main에서 가져옴)
 */
function initResponsive() {
    handleResponsiveTable();
    $(window).on('resize', debounce(handleResponsiveTable, 250));
}

function handleResponsiveTable() {
    const $table = $('.ranking_table');
    const $container = $('.ranking_table_container');

    if ($table.length === 0) return;

    const containerWidth = $container.width();
    const isMobile = containerWidth < 768;

    if (isMobile) {
        $table.addClass('mobile_view');

        if ($table[0].scrollWidth > containerWidth) {
            $container.addClass('scrollable');
        }
    } else {
        $table.removeClass('mobile_view');
        $container.removeClass('scrollable');
    }
}

// 페이지 로드 완료 후 초기 애니메이션 실행
$(window).on('load', function() {
    setTimeout(function() {
        $('.my_group_ranking_card').addClass('pulse_effect');
    }, 500);
});