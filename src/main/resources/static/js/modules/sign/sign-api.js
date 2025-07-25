/**
 * username을 전달받아 중복 확인 비동기 요청
 * @param username username
 * @returns boolean true=중복, false=중복아님
 */
export async function checkUsernameDuplicate(username) {
    const response = await $.ajax({
        method: "GET",
        url: "/api/users/username",
        data: { username },
        dataType: "json",
    });

    return response.data;
}