package io.github.xiewinson.smoothrefreshlayout.library.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static io.github.xiewinson.smoothrefreshlayout.library.annotation.RefreshHeaderState.PULL_TO_REFRESH;
import static io.github.xiewinson.smoothrefreshlayout.library.annotation.RefreshHeaderState.REFRESHING;
import static io.github.xiewinson.smoothrefreshlayout.library.annotation.RefreshHeaderState.REFRESH_COMPLETED;
import static io.github.xiewinson.smoothrefreshlayout.library.annotation.RefreshHeaderState.RELEASE_TO_REFRESH;

/**
 * Created by winson on 2017/10/6.
 */

@Documented
@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {PULL_TO_REFRESH, REFRESHING, RELEASE_TO_REFRESH, REFRESH_COMPLETED})
public @interface RefreshHeaderState {
    int PULL_TO_REFRESH = 0;
    int REFRESHING = 1;
    int RELEASE_TO_REFRESH = 2;
    int REFRESH_COMPLETED = 3;
}
