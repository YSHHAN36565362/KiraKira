package com.kirakira.pilgrimage.exception;

public class FavoriteNotFoundException extends RuntimeException {
    public FavoriteNotFoundException() {
        super("즐겨찾기 내역을 찾을 수 없습니다.");
    }
}
