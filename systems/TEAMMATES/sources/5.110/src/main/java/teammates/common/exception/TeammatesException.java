package teammates.common.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import teammates.common.util.Const;

@SuppressWarnings("serial")
public class TeammatesException extends Exception {
    public String errorCode;

    public TeammatesException() {
        super();
    }

    public TeammatesException(String message) {
        super(message);
    }

    public TeammatesException(String errorcode, String message) {
        super(message);
        errorCode = errorcode;
    }

    public TeammatesException(Throwable cause) {
        super(cause);
    }

    public static String toStringWithStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return Const.EOL + sw.toString();
    }
}
