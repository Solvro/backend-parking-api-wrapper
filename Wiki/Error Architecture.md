## Error Handling Architecture

# 1. Result Interface
The Result interface is a core part of the architecture that wraps return values from methods, ensuring safe and unambiguous handling of success and error cases without throwing exceptions.

Components:
        Success<T>: Represents a successful operation and contains the resulting data.
        Failure<T>: Wraps error information using the Error interface.
Methods:
        isSuccess(): Checks whether the operation succeeded.
        getData(): Returns the data if the operation was successful.
        getError(): Returns the error if the operation failed.
Safety:
        Calling getData() on a Failure or getError() on a Success throws an InvalidCallException.

# 2. Returning Values from Services
Instead of throwing exceptions or returning null, services always return a Result object.

Usage Example:
        On success: Result.success(data).
        On failure: Result.failure(new ErrorSubclass()).

This ensures that services explicitly communicate whether the operation succeeded or failed, delegating further handling to controllers.
# 3. Handling Results in Controllers

All controllers extend the abstract class HandleResult, which provides a standardized approach to processing Result objects.

How HandleResult Works:
        The handleResult method:
            If Result is Success, it returns a ResponseEntity with an HTTP success status and the data as JSON.
            If Result is Failure, it maps the error to an ErrorWrapper and returns an HTTP error response with the appropriate status.

Benefits:
        Avoids repetitive code in controllers.
        Ensures standardized API responses (consistent data and error formats).

Parameters of handleResult:
        Result<?> toHandle: The Result object returned by the service.
        HttpStatus onSuccess: The HTTP status to use in case of success.
        String uri: The endpoint URI (used for logging errors).

# 4. Global Exception Handler

Using @ControllerAdvice, the application has a centralized mechanism to handle uncaught exceptions globally.

How It Works:
        Each handled exception type (e.g., InvalidCallException) has a dedicated method in the handler.
        For each exception, an ErrorWrapper is created with:
            The error message.
            The expected HTTP status.
            The URI where the error occurred.
            The actual HTTP status (e.g., 500 or 400).

Examples of Exception Handling:
        InvalidCallException: Returns HTTP 400 (Bad Request) with a message like "Invalid call."
        JsonProcessingException: Returns HTTP 500 (Internal Server Error) with a message like "JSON processing error."

This ensures that even unhandled or unexpected exceptions are communicated in a client-friendly and consistent format.
# 5. ErrorWrapper

ErrorWrapper is a record that encapsulates error information in a standardized way.

Fields:
        errorMessage: A descriptive error message.
        expectedStatus: The expected HTTP status if the operation succeeded.
        uri: The endpoint where the error occurred.
        occurredStatus: The actual HTTP status of the error.

By using ErrorWrapper, clients always receive clear, predictable error responses.
# 6. Example Flow in a Controller

   Success Scenario:
   The service returns Result.success(42L).
   handleResult generates a response with HTTP 200 (OK) and a JSON body containing 42.

   Error Scenario:
   The service returns Result.failure(new ParkingError.ParkingNotFoundById(id)).
   handleResult maps the error to an ErrorWrapper and generates a response with HTTP 400 (Bad Request) and a message like "Invalid Parking ID: {id}".