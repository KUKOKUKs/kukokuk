// 1~3, 1~6  학년 옵션 생성
export const elementarySchool = setGradeOptions(6);
export const middleSchool = setGradeOptions(3);

/**
 * 진도별 학년 옵션 개수를 전달받아 생성한 옵션을 반환 
 * @param count 생성할 학년 옵션 수
 * @returns 생성된 옵션 리스트(string)
 */
export function setGradeOptions(count) {
    let options = '';
    for (let i = 1; i <= count; i++) {
        options += `<option value="${i}">${i}학년</option>`;
    }
    return options;
}