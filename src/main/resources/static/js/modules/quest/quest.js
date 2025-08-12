import {getDailyQuestList} from "./quest-api";

$(document).ready(async function () {
    await getDailyQuestList();
});