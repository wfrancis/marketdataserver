package com.proto.core.event;

/**
 * Define result of an operations. We should use this as return value of all
 * operations within the event interface.
 *
 * @author wfrancis
 */
public interface ReturnValue {

    /**
     * A simple result for succeed operation
     */
    public static final ReturnValue OK = new ReturnValue() {
        public boolean isSuccessful() {
            return true;
        }

        public String getErrorMessage() {
            return "";
        }

        public void setErrorMessage(String errMsg) {
        }
    };

    /**
     * A simple result for failed operation, i.e. the current state may be invalid as the attempted operation caused unexpected errors.
     */
    public static final ReturnValue FAILED = new ReturnValue() {
        public boolean isSuccessful() {
            return false;
        }

        public String getErrorMessage() {
            return "";
        }

        public void setErrorMessage(String errMsg) {
        }
    };


    /**
     * A simple result for ignored operation, i.e. the current state is same as the state previous to the attempted operation.
     */
    public static final ReturnValue IGNORED = new ReturnValue() {
        String errMsg;

        public boolean isSuccessful() {
            return false;
        }

        public void setErrorMessage(String errMsg) {
            this.errMsg = errMsg;
        }

        public String getErrorMessage() {
            if (errMsg == null)
                return "";
            return errMsg;
        }
    };

    /**
     * @return whether the operation succeed.
     */
    boolean isSuccessful();

    /**
      */
    String getErrorMessage();

    /**
     */
    void setErrorMessage(String errMsg);
}
