package id.co.javara.app.grpc;

import id.co.javara.core.exception.IdempotencyViolationException;
import id.co.javara.core.exception.T24BusinessException;
import id.co.javara.core.exception.T24ConnectionException;
import id.co.javara.core.exception.T24TimeoutException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GrpcExceptionHandler {

    @GrpcExceptionHandler(T24BusinessException.class)
    public Status handleBusiness(T24BusinessException ex, StreamObserver<?> observer) {
        return Status.FAILED_PRECONDITION
            .withDescription(ex.getMessage())
            .augmentDescription("t24_error_code: " + ex.t24ErrorCode());
    }

    @GrpcExceptionHandler(T24ConnectionException.class)
    public Status handleConnection(T24ConnectionException ex, StreamObserver<?> observer) {
        return Status.UNAVAILABLE.withDescription(ex.getMessage())
            .augmentDescription("error_code: " + ex.errorCode());
    }

    @GrpcExceptionHandler(T24TimeoutException.class)
    public Status handleTimeout(T24TimeoutException ex, StreamObserver<?> observer) {
        return Status.DEADLINE_EXCEEDED.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(IdempotencyViolationException.class)
    public Status handleIdempotency(IdempotencyViolationException ex, StreamObserver<?> observer) {
        return Status.ALREADY_EXISTS.withDescription(ex.getMessage());
    }
}
