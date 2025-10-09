/**
 * 그룹 목록, 목록이 추가될 부모 요소를 전달 받아 리스트 세팅
 * @param groups 그룹 목록
 * @param $groupSearchListComponent 리스트 추가될 부모 요소
 */
export function setGroupList(groups, $groupSearchListComponent) {
    if ($groupSearchListComponent.length) {
        let content = "";

        if (groups.length > 0) {
            for (let group of groups) {
                content += `
                    <div class="group_serach_list">
                        <div class="list_info">
                            <div class="group_info group_title_info">
                                ${group.password != null
                        ? `
                                    <iconify-icon class="icon base_font"
                                        icon="streamline-color:padlock-square-1-flat"></iconify-icon>
                                `
                        : ""}
                                <p class="group_title text_ellipsis">${group.title}</p>
                                <span class="group_users">(${group.memberCountFormatted}명)</span>
                            </div>
    
                            <div class="group_info motto_info">
                                <span class="w_max_content">급훈 [</span>
                                <p class="motto text_ellipsis">${group.motto ?? '-'}</p>
                                <span>]</span>
                            </div>
                        </div>
    
                        <div class="group_info group_botton_info">
                            <div class="group_teacher_profile">
                                <div class="icon title group_profile_img">
                                    ${group.teacher.profileFilename != null
                        ? `<img src="${group.teacher.profileFileUrl}" alt="profile thumbnail"/>`
                        : `<img src="/images/basic_profile_img.jpg" alt="profile thumbnail">`}
                                </div>
                                <span class="group_teacher_name text_ellipsis">${group.teacher.nickname}</span>
                            </div>
    
                            <button type="button"
                                    class="btn tiny small_font white group_join_btn"
                                    data-group-no="${group.groupNo}">입반 신청</button>
                        </div>
                    </div>
                `;
            }
        } else {
            content += `
                <div class="group_serach_list tac">조회된 결과가 없습니다.</div>
            `;
        }

        $groupSearchListComponent.html(content);
    }
}