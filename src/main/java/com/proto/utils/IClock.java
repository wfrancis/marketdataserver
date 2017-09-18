
package com.proto.utils;

import java.util.Date;

public interface IClock {
    Date currentTime();

    long currentTimeMillis();
}