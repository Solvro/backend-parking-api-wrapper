# Method return values Documentation
## Overview

The `Result` class is a generic wrapper designed to encapsulate either a successful
value or an error. It provides a consistent structure for handling success and failure
states in your application, minimizing the need for exception throwing. This class
supports two static factory methods for creating Result instances:

- `Success`: Used to generate a successful result.
- `Failure`: Used to generate a failure result.

The `ErrorResult` class is a simple data container designed to storage error type and 
description.
It is a part of the Result class.

### Package
`pl.wrapper.parking.result`

## Fields
### Result fields
- `private T result`: The value of the successful result.
- `private boolean success`: Indicates whether the operation was successful.
- `private ErrorResult error`: Contains details about the error, if any.

### ErrorResult fields
- `private String errorMessage`: The value of the error description.
- `private String errorCode`: Contains the error code marked as a error name.

## Methods
### Result methods
- `public static <T> Result<T> Success(T result)`: Generate the `Result` instance 
representing success with fields: `this.result = result`, `this.success = true` and
`this.error = null`.
- `public static <T> Result<T> Failure(Class<? extends Exception> exception, String message)`:
Generate the `Result` instance representing failure with fields: `this.result = null`, 
`this.success = false` and `this.error = new ErrorResult(message,exception.getName())`.
- Getters and Setters

## Usage 
1. Generate `Result`
- Correct value
set `Result` with correct value examples:
```java
Result<Integer> res = Result.Success(45);
Result<ParkingResponse> res = Result.Success(new ParkingResponse());
```
- Incorrect value
set `Result` with error value examples:
```java
Result<Object> result = Result.Failure(IllegalArgumentException.class, "value < -1");
Result<Object> result = Result.Failure(PwrApiNotRespondingException.class, "404");
```
2. Check result status
```java
result.isSuccess();
```
3. Get `Result` data
- get correct `Result` result value
```java
result.getResult();
```
- get error for incorrect `Result`
```java
result.getError();                      //get ErrorResult instance
result.getError().getErrorCode();       //get error code
result.getError().getErrorMessage();    //get error description
```