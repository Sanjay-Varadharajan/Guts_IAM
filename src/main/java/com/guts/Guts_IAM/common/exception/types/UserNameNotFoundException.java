package com.guts.Guts_IAM.common.exception.types;

import org.springframework.http.HttpStatus;

public class UserNameNotFoundException extends RuntimeException {
  private String errorCode;

  private HttpStatus status;


  public UserNameNotFoundException(String message, String errorCode, HttpStatus status) {
    super(message);
    this.errorCode = errorCode;
    this.status = status;
  }
}
