package com.kirakira.pilgrimage.exception;

public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException() {
        super("이미 이 장소에 리뷰를 작성했습니다. 기존 리뷰를 수정해 주세요.");
    }
}
