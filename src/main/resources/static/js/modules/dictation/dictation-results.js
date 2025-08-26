$(document).ready(() => {

    // 한 번 눌러 펼치고, 다시 누르면 닫기. 표의 "그 행 바로 아래"에 전체폭으로 표시.
    $(document).on('click', '.load-logs-btn', function () {
        const $btn = $(this);
        const $tr = $btn.closest('tr');
        const sessionNo = $btn.data('sessionno');

        // 이미 로그 행이 있으면 토글만
        let $logsRow = $tr.next('.logs-row');
        if ($logsRow.length) {
            $logsRow.toggle();
            return;
        }

        // 테이블 컬럼 개수에 맞춰 colspan 설정
        const cols = $tr.closest('table').find('thead th').length;

        // 행 생성(HTML은 그대로 두고, JS로만 추가)
        $logsRow = $(`
    <tr class="logs-row">
      <td colspan="${cols}" style="padding:12px 16px; background:#f9fafb;">
        <div class="logs-wrap" style="border:1px solid #e5e7eb; border-radius:10px; padding:12px 14px; background:#fff;">
          불러오는 중…
        </div>
      </td>
    </tr>
  `);
        $tr.after($logsRow);

        // 데이터 로드
        $.get(`/api/dictation/results/${sessionNo}/logs`, function (res) {
            const rows = (res && res.data) || [];
            const $wrap = $logsRow.find('.logs-wrap');

            if (!rows.length) {
                $wrap.html('<div>이력이 없습니다.</div>');
                return;
            }

            let html = '';
            rows.forEach(log => {
                html += `
        <div style="padding:10px 0; border-bottom:1px dashed #e5e7eb;">
          <div><b>문제 ${log.dictationQuestionNo}</b></div>
          <div style="font-size:12px; color:#6b7280;">제출 시각: ${(log.createdDate ?? '').toString().split(/[T ]/)[0]}</div>
          <div>정답: ${log.correctAnswer ?? ''}</div>
          <div>제출: ${log.userAnswer ?? ''}</div>
          <div>정답 여부: ${((log.isSuccess||'').toString().toUpperCase()==='Y'?'o':((log.isSuccess||'').toString().toUpperCase()==='N'?'x':''))} | 힌트 사용: ${((log.usedHint||'').toString().toUpperCase()==='Y'?'o':((log.usedHint||'').toString().toUpperCase()==='N'?'x':''))} | 시도 횟수: ${log.tryCount ?? ''}</div>
        </div>
      `;
            });
            $wrap.html(html);
        });
    });
});