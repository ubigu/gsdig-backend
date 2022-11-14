package fi.ubigu.gsdig.oapip.model;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ProcessesExceptionHandler extends ResponseEntityExceptionHandler{

    @ExceptionHandler(ProcessesException.class)
    public ResponseEntity<Object> handleCustomException(ProcessesException ce, WebRequest request) {
        return handleExceptionInternal(ce, ce, new HttpHeaders(), HttpStatus.valueOf(ce.getStatus()), request); 
    }

}
