package com.catijr.backend.playlist;

/**
 * Envolve qualquer falha de banco durante o reorder. Vira 500 com corpo fixo
 * {@code {"error":"reorder failed"}} — nunca expõe SQL, os musicIds ou stack trace.
 */
public class ReorderFailedException extends RuntimeException {

    public ReorderFailedException(Throwable cause) {
        super(cause);
    }
}
