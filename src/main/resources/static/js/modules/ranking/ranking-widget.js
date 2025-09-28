/**
 * 랭킹 위젯 JavaScript 모듈
 * API를 통해 랭킹 데이터를 가져와서 동적으로 표시
 */

$(document).ready(async function() {
    console.log('랭킹 위젯 초기화 시작');

    // DOM 요소들
    const $rankingContainer = $('#ranking-widget-container');
    const $rankingPeriod = $('#ranking-period');
    const $speedBtn = $('#speed-ranking-btn');
    const $dictationBtn = $('#dictation-ranking-btn');
    const $mySpeedRank = $('#my-speed-rank');
    const $myDictationRank = $('#my-dictation-rank');
    const $speedList = $('#speed-ranking-list');
    const $dictationList = $('#dictation-ranking-list');

    // 현재 상태
    let currentType = 'SPEED';
    let rankingData = null;

    // 랭킹 위젯이 있는 경우에만 실행
    if (!$rankingContainer.length || $rankingContainer.data('logged-in') === false) {
        console.log('랭킹 위젯 없음 또는 비로그인 상태');
        return;
    }

    // 현재 월 표시
    const currentMonth = new Date().toISOString().slice(0, 7);
    $rankingPeriod.text(currentMonth);

    // 초기 랭킹 데이터 로드
    await loadRankingData();

    // 스피드퀴즈 버튼 클릭
    $speedBtn.click(function() {
        if (currentType !== 'SPEED') {
            switchRankingType('SPEED');
        }
    });

    // 받아쓰기 버튼 클릭
    $dictationBtn.click(function() {
        if (currentType !== 'DICTATION') {
            switchRankingType('DICTATION');
        }
    });

    /**
     * 랭킹 데이터 로드
     */
    async function loadRankingData() {
        try {
            showLoading();

            // API 호출
            const response = await $.ajax({
                url: '/api/ranking/widget/summary',
                method: 'GET',
                dataType: 'json'
            });

            if (response.success && response.data) {
                rankingData = response.data;
                console.log('랭킹 데이터 로드 성공:', rankingData);

                // 데이터 렌더링
                renderRankingData();
                hideLoading();
            } else {
                throw new Error(response.message || '랭킹 데이터 로드 실패');
            }

        } catch (error) {
            console.error('랭킹 데이터 로드 오류:', error);
            showError('랭킹 데이터를 불러올 수 없습니다.');
        }
    }

    /**
     * 랭킹 타입 전환
     */
    function switchRankingType(type) {
        console.log(`랭킹 타입 전환: ${currentType} -> ${type}`);

        currentType = type;

        // 버튼 활성화 상태 변경
        if (type === 'SPEED') {
            $speedBtn.addClass('active');
            $dictationBtn.removeClass('active');
            $('.speed-rank').addClass('active');
            $('.dictation-rank').removeClass('active');
            $speedList.addClass('active');
            $dictationList.removeClass('active');
        } else {
            $dictationBtn.addClass('active');
            $speedBtn.removeClass('active');
            $('.dictation-rank').addClass('active');
            $('.speed-rank').removeClass('active');
            $dictationList.addClass('active');
            $speedList.removeClass('active');
        }
    }

    /**
     * 랭킹 데이터 렌더링
     */
    function renderRankingData() {
        if (!rankingData) {
            console.warn('랭킹 데이터가 없습니다');
            return;
        }

        // 내 순위 표시
        $mySpeedRank.text(rankingData.mySpeedRank ? `${rankingData.mySpeedRank}위` : '순위 없음');
        $myDictationRank.text(rankingData.myDictationRank ? `${rankingData.myDictationRank}위` : '순위 없음');

        // 스피드퀴즈 랭킹 리스트
        renderRankingList($speedList, rankingData.speedRankings, 'SPEED');

        // 받아쓰기 랭킹 리스트
        renderRankingList($dictationList, rankingData.dictationRankings, 'DICTATION');
    }

    /**
     * 랭킹 리스트 렌더링
     */
    function renderRankingList($container, rankings, type) {
        if (!rankings || rankings.length === 0) {
            $container.html(`
                <div class="empty-ranking">
                    <p>아직 ${type === 'SPEED' ? '스피드퀴즈' : '받아쓰기'} 랭킹이 없습니다.</p>
                </div>
            `);
            return;
        }

        const rankingHtml = rankings.map((ranking, index) => {
            const rank = index + 1;
            const rankClass = rank <= 3 ? `rank-${rank}` : 'rank-other';

            return `
                <div class="ranking-item">
                    <div class="rank-number ${rankClass}">${rank}</div>
                    <div class="user-info">
                        ${ranking.profileFilename
                ? `<img src="/images/profiles/${ranking.profileFilename}" class="user-avatar" alt="프로필">`
                : `<div class="user-avatar default">
                                 <iconify-icon icon="mdi:account"></iconify-icon>
                               </div>`
            }
                        <span class="user-name">${escapeHtml(ranking.nickname)}</span>
                    </div>
                    <div class="ranking-score">${formatScore(ranking.totalScore)}</div>
                </div>
            `;
        }).join('');

        $container.html(rankingHtml);
    }

    /**
     * 로딩 상태 표시
     */
    function showLoading() {
        $speedList.html('<div class="loading">로딩 중...</div>');
        $dictationList.html('<div class="loading">로딩 중...</div>');
        $mySpeedRank.text('-');
        $myDictationRank.text('-');
    }

    /**
     * 로딩 상태 숨김
     */
    function hideLoading() {
        // renderRankingData()에서 실제 데이터로 교체됨
    }

    /**
     * 에러 상태 표시
     */
    function showError(message) {
        const errorHtml = `<div class="error">${message}</div>`;
        $speedList.html(errorHtml);
        $dictationList.html(errorHtml);
        $mySpeedRank.text('오류');
        $myDictationRank.text('오류');
    }

    /**
     * 점수 포맷팅
     */
    function formatScore(score) {
        if (!score) return '0.0';
        return parseFloat(score).toFixed(1);
    }

    /**
     * HTML 이스케이프
     */
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * 위젯 새로고침 (외부에서 호출 가능)
     */
    window.refreshRankingWidget = function() {
        console.log('랭킹 위젯 수동 새로고침');
        loadRankingData();
    };

    // 주기적 업데이트 (선택사항 - 5분마다)
    setInterval(function() {
        if (document.visibilityState === 'visible') {
            console.log('랭킹 위젯 자동 새로고침');
            loadRankingData();
        }
    }, 5 * 60 * 1000); // 5분

    console.log('랭킹 위젯 초기화 완료');
});