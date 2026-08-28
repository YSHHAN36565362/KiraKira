package com.kirakira.pilgrimage.exception;

public class FavoriteAlreadyExistsException extends RuntimeException {
    public FavoriteAlreadyExistsException() {
        super("이미 즐겨찾기에 등록된 장소입니다.");
    }
}
