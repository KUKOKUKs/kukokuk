/**
 * username을 전달받아 중복 확인 비동기 요청
 * @param username username
 * @returns boolean true=중복, false=중복아님
 */
export async function checkUsernameDuplicate(username) {
    console.log("checkUsernameDuplicate() api 요청 실행", username);
    const response = await $.ajax({
        method: "GET",
        url: "/api/users/username",
        data: { username },
        dataType: "json",
    });

    return response.data;
}

/**
 * nickname을 전달받아 중복 확인 비동기 요청
 * @param nickname nickname
 * @returns boolean true=중복, false=중복아님
 */
export async function checkNicknameDuplicate(nickname) {
    console.log("checkNicknameDuplicate() api 요청 실행", nickname);
    const response = await $.ajax({
        method: "GET",
        url: "/api/users/nickname",
        data: { nickname },
        dataType: "json",
    });

    return response.data;
}