package io.github.xiewinson.smoothrefresh.library.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static io.github.xiewinson.smoothrefresh.library.annotation.PageState.EMPTY;
import static io.github.xiewinson.smoothrefresh.library.annotation.PageState.EMPTY_FOOTER;
import static io.github.xiewinson.smoothrefresh.library.annotation.PageState.ERROR;
import static io.github.xiewinson.smoothrefresh.library.annotation.PageState.ERROR_FOOTER;
import static io.github.xiewinson.smoothrefresh.library.annotation.PageState.LOADING;
import static io.github.xiewinson.smoothrefresh.library.annotation.PageState.LOADING_FOOTER;
import static io.github.xiewinson.smoothrefresh.library.annotation.PageState.NONE;
import static io.github.xiewinson.smoothrefresh.library.annotation.PageState.NO_MORE_FOOTER;


/**
 * Created by winson on 2017/10/6.
 */

@Documented
@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {LOADING, EMPTY, ERROR, LOADING_FOOTER, EMPTY_FOOTER, ERROR_FOOTER, NONE})
public @interface PageState {
    int LOADING = 0;
    int EMPTY = 1;
    int ERROR = 2;

    int LOADING_FOOTER = 3;
    int EMPTY_FOOTER = 4;
    int ERROR_FOOTER = 5;
    int NO_MORE_FOOTER = 6;

    int NONE = -1;

}
