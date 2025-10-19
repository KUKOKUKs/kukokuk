/**
 * 랭크 목록, 목록이 추가될 상위 요소를 전달 받아 리스트 세팅
 * <p>
 *     speedQuizRanks, levelQuizRanks, dictationQuizRanks
 * @param {Object} ranksResponseDtos - 서버에서 전달받은 Map<String, RanksResponseDto> 구조 JSON
 * @param {jQuery} $rankContainer - 랭크 컴포넌트 전체 부모 컨테이너
 */
export function setRankList(ranksResponseDtos, $rankContainer) {
    console.log("setRankList() 실행");
    console.log("setRankList() ranksResponseDtos: ", ranksResponseDtos);
    console.log("setRankList() $rankContainer: ", $rankContainer);

    if (!$rankContainer.length) return;

    // 예: key = "speedQuizRanks", ranksDto = { contentType: "SPEED", ranks: [...], hasUserRank: "true" }
    for (const [key, ranksDto] of Object.entries(ranksResponseDtos)) {
        const contentType = (ranksDto.contentType || '').toLowerCase(); // 컨텐츠명 소문자로 변환
        const $rankComponent = $rankContainer.find(`#${contentType}-ranks-component`); // 컨텐츠명으로 요소 찾기
        const $rankListComponent = $rankComponent.find(".rank_list_component"); // 해당 요소 리스트 요소 찾기
        console.log("setRankList() $rankComponent: ", $rankComponent);
        console.log("setRankList() $rankListComponent: ", $rankListComponent);

        // 랭크 목록이 비었을 경우
        if (!ranksDto.ranks.length) {
            $rankListComponent.html(`
                <div class="component_info tiny">
                    <div class="list_info tac">
                        조회된 순위가 없습니다.
                    </div>
                </div>
            `);
            continue;
        }

        let content = "";
        const requestUserNo = ranksDto.userNo; // 요청한 사용자 번호

        // 랭크 배열을 순회하며 목록 생성
        for (const rank of ranksDto.ranks) {
            const isMyRank = rank.userNo === requestUserNo; // 랭크 userNo와 요청자 userNo가 같으면 사용자 랭크로 판단
            const rankNum = rank.userRank; // 랭크
            const isTop3 = rankNum < 4; // 1~3 순위인 경우
            const isTop4to5 = rankNum > 3 && rankNum < 6; // 4~5 순위인 경우
            const isMyOutOfTop = isMyRank && rankNum > 5; // 사용자 순위가 순위권 밖인 경우

            content += `
                <div class="component_info tiny ${isMyRank ? 'selected' : ''}">
                    <div class="index_list w_100" style="--cols: 12; --indexCols: 1;">
                        <div class="rank ${isTop3 ? 'medal' : ''}">
                            ${isTop3 
                            ? `<iconify-icon icon="fluent-emoji-flat:${rankNum === 1 ? '1st' : rankNum === 2 ? '2nd' : '3rd'}-place-medal"></iconify-icon>`
                            : isTop4to5
                                ? `<iconify-icon class="color_gray" icon="ri:number-${rankNum}"></iconify-icon>`
                                : isMyOutOfTop
                                    ? `<span class="small_font color_gray">${rankNum}위</span>`
                                    : ``
                            }
                        </div>
                        <div class="d_flex align_center tiny_gap">
                            <div class="rank_profile icon base">
                                <img src="${rank.profileFileUrl}" alt="프로필 이미지">
                            </div>
                            <p class="rank_level small_font bold">Lv.${rank.level}</p>
                            <p class="rank_nickname small_font bold text_ellipsis">${rank.nickname}</p>
                            <p class="rank_score bold color_primary">${rank.formattedScore}</p>
                        </div>
                    </div>
                </div>
            `;
        }
        
        $rankListComponent.html(content); // 생성한 목록 추가

        // 해당 컨텐츠 랭크에 사용자 랭크 정보가 없을 경우
        if (!ranksDto.hasUserRank) {
            const noMyRankContent = `
                <div class="component_info tiny selected">
                    <div class="list_info tac sub_font color_gray">
                        아직 내 순위가 없습니다.
                    </div>
                </div>
            `;
            $rankListComponent.append(noMyRankContent); // 마지막 리스트에 추가
        }
    }
}