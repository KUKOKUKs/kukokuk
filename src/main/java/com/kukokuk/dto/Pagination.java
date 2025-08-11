package com.kukokuk.dto;

import lombok.Getter;

@Getter
public class Pagination {

    private int rows = 10;		    // 노출될 데이터 행의 개수
    private int pages = 5;   	    // 한 블럭에 표시될 페이지 개수
    private int currentPage;		// 현재 페이지
    private int offset;			    // 페이지별 데이터를 가져올 첫번째 행의 인덱스
    private int totalRows;		    // 총 데이터 개수
    private int totalPages;		    // 총 페이지 개수
    private int totalBlocks;		// 총 페이지 블럭 개수
    private int currentBlock;	    // 현재 페이지의 블럭
    private int beginPage;		    // 현재 블럭의 시작 페이지
    private int endPage;			// 현재 블럭의 마지막 페이지
    private boolean isFirst;		// 첫번째 페이지 여부
    private boolean isLast;		    // 마지막 페이지 여부
    private int prevPage;		    // 이전 페이지
    private int nextPage;		    // 다음 페이지

    /**
     * 요청한 페이지번호, 총 게시글 개수를 전달 받아
     * 필드 멤버 변수 초기화
     * @param pageNo 요청한 페이지번호
     * @param totalRows 총 데이터 갯수
     */
    public Pagination(int pageNo, int totalRows) {
        this.currentPage = pageNo;
        this.totalRows = totalRows;
        init();
    }

    /**
     * 요청한 페이지번호, 총 게시글 개수, 한 페이지당 표시할 행의 개수를 전달 받아
     * 필드 멤버 변수 초기화
     * @param pageNo 요청한 페이지번호
     * @param totalRows 총 데이터 갯수
     * @param rows 행의 개수
     */
    public Pagination(int pageNo, int totalRows, int rows) {
        this.currentPage = pageNo;
        this.totalRows = totalRows;
        this.rows = rows;
        init();
    }

    /**
     * Pagination 필드 멤버 변수 초기화
     */
    private void init() {
        // 총 페이지 개수 계산하기
        totalPages = ceilDiv(totalRows, rows);

        // 현재 페이지 번호가 1이하인 경우 1로 재설정
        if (currentPage < 1) {
            currentPage = 1;
        }

        // 현재 페이지 번호가 전체 페이지 수 보다 클 경우 = 마지막 페이지보다 클 경우
        // 마지막 페이지 번호로 재설정
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        // 현재 페이지에 맞게 offset 값을 설정.
        offset = (currentPage - 1) * rows;

        // 총 블록 개수 계산
        totalBlocks = ceilDiv(totalPages, pages);

        // 현재 블록 번호 계산
        currentBlock = ceilDiv(currentPage, pages);

        // 현재 페이지의 첫번째 블록 번호
        beginPage = (currentBlock - 1) * pages + 1;

        // 현재 페이지의 마지막 블록 값 설정
        endPage = currentBlock * pages;
        if (currentBlock == totalBlocks) {
            endPage = totalPages;
        }

        // 요청한 페이지가 1 페이지인 경우 true
        isFirst = currentPage == 1;

        // 요청한 페이지가 마지막 페이지인 경우 true
        isLast = currentPage == totalPages;

        // 이전/다음 페이지번호 계산하기
        prevPage = currentPage - 1;
        nextPage = currentPage + 1;
    }

    /**
     * 올림 나눗셈 메서드
     */
    private int ceilDiv(int a, int b) {
        return (a + b - 1) / b;
    }

}
