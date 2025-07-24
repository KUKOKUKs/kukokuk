export async function checkUsernameDuplicate(username) {
    const response = await $.ajax({
        method: "GET",
        url: "/api/users/me",
    });
}